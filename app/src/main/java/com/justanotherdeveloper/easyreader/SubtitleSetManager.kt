package com.justanotherdeveloper.easyreader

import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList

class SubtitleSetManager(activity: AppCompatActivity) {

    private val tinyDB = TinyDB(activity)
    private var ids = tinyDB.getListInt(idsFilename)

    class SubtitleSet(
        var textColor: Int,
        var fontStyle: Int,
        var speed: String,
        var backgroundColor: Int,
        var textToSpeechEnabled: Boolean,
        var maxWordsPerSubtitle: Int,
        var sourceLink: String,
        var enteredTitle: String,
        var enteredText: String,
        var subtitles: ArrayList<String>,
        var savedSubtitleIndex: Int,
        var isStarred: Boolean,
        var dateString: String,
        val id: Int)

    // generates subtitle set and saves content into local database
    fun saveAddedSubtitleSet(textColor: Int, fontStyle: Int, speed: String,
                             backgroundColor: Int, textToSpeechEnabled: Boolean,
                             maxWordsPerSubtitle: Int, sourceLink: String,
                             enteredTitle: String, enteredText: String): Int {

        val id = generateId()
        ids.add(id)

        val subtitles = generateSubtitles(enteredText, maxWordsPerSubtitle)
        val subtitleSet = SubtitleSet(textColor, fontStyle, speed, backgroundColor,
            textToSpeechEnabled, maxWordsPerSubtitle, sourceLink, enteredTitle, enteredText,
            subtitles, -1,false, getDateString(), id)

        tinyDB.putListInt(idsFilename, ids)
        tinyDB.putObject(id.toString(), subtitleSet)

        return id
    }

    fun deleteSubtitleSet(id: Int) {
        ids.remove(id)
        tinyDB.remove(id.toString())
        tinyDB.putListInt(idsFilename, ids)
    }

    fun getIds(): ArrayList<Int> {
        ids = tinyDB.getListInt(idsFilename)
        return ids
    }

    fun getSubtitleSet(subtitleSetId: String): SubtitleSet {
        return tinyDB.getObject(subtitleSetId, SubtitleSet::class.java)
    }

    fun updateSubtitleSet(subtitleSet: SubtitleSet) {
        subtitleSet.dateString = getDateString()
        tinyDB.putObject(subtitleSet.id.toString(), subtitleSet)
    }

    fun updateSubtitles(subtitleSet: SubtitleSet, subtitles: ArrayList<String>) {
        var enteredText = ""
        for (subtitle in subtitles)
            enteredText += subtitle
        subtitleSet.enteredText = enteredText
        subtitleSet.subtitles = subtitles
        updateSubtitleSet(subtitleSet)
    }

    fun generateSubtitles(original: String, maxWords: Int): ArrayList<String> {
        val subtitles = ArrayList<String>()

        for(paragraph in getParagraphs(original))
            parseParagraph(subtitles, paragraph, maxWords, maxCharPerSubtitle)

        for((i, subtitle) in subtitles.withIndex())
            subtitles[i] = subtitle.standardizeWhiteSpaces()

        return subtitles
    }

    private fun String.standardizeWhiteSpaces(): String {
        var newStr = ""
        for(ch in this)
            newStr += when {
                ch == '\n' -> ch
                ch == '\t' -> ch
                ch == '\r' -> ch
                ch.isWhitespace() -> ' '
                else -> ch
            }
        return newStr
    }

    @Suppress("SameParameterValue")
    private fun parseParagraph(subtitles: ArrayList<String>, paragraph: String,
                               maxWords: Int, charLimit: Int) {
        val chars = arrayListOf('.', ';', '!', '?', '\n')
        val sentenceLists = ArrayList<ArrayList<String>>()
        sentenceLists.add(ArrayList())
        val words = getWords(paragraph)
        for((index, word) in words.withIndex()) {
            sentenceLists.last().add(word)
            if(index == words.lastIndex) break
            if(containsAnyChar(word, chars))
                sentenceLists.add(ArrayList())
        }
        parseSentences(subtitles, sentenceLists, maxWords, charLimit)
    }

    private fun parseSentences(subtitles: ArrayList<String>,
                       sentenceLists: ArrayList<ArrayList<String>>,
                       maxWords: Int, charLimit: Int) {
        val nextSubtitleList = ArrayList<String>()
        for(sentenceList in sentenceLists) {

            fun manageSentence() {
                if (exceedsLimits(sentenceList, nextSubtitleList, maxWords, charLimit)) {
                    if (nextSubtitleList.isEmpty())
                        addSentenceWords(sentenceList, nextSubtitleList, subtitles,
                            maxWords, charLimit)
                    else {
                        subtitles.add(nextSubtitleList.joinToString(""))
                        nextSubtitleList.clear()
                        manageSentence()
                    }
                } else nextSubtitleList.addAll(sentenceList)
            }

            manageSentence()
        }

        if(nextSubtitleList.isNotEmpty())
            subtitles.add(nextSubtitleList.joinToString(""))
    }

    private fun addSentenceWords(sentenceList: ArrayList<String>,
                         nextSubtitleList: ArrayList<String>,
                         subtitles: ArrayList<String>,
                         maxWords: Int, charLimit: Int) {
        for(word in sentenceList) {
            var current = word

            fun manageWord() {
                if(exceedsLimits(current, nextSubtitleList, maxWords, charLimit)) {
                    if(nextSubtitleList.isEmpty()) {
                        current = parseLongWord(current, subtitles, charLimit)
                        manageWord()
                    } else {
                        subtitles.add(nextSubtitleList.joinToString(""))
                        nextSubtitleList.clear()
                        manageWord()
                    }
                } else nextSubtitleList.add(current)
            }

            manageWord()
        }
    }

    private fun parseLongWord(longWord: String,
                      subtitles: ArrayList<String>,
                      charLimit: Int): String {
        subtitles.add(longWord.substring(0, charLimit))
        return longWord.substring(charLimit, longWord.length)
    }

    private fun exceedsLimits(word: String,
                      nextSubtitleList: ArrayList<String>,
                      maxWords: Int, charLimit: Int): Boolean {
        val currCharCount = countTotalChars(nextSubtitleList)
        val currWordCount = nextSubtitleList.size

        val totalCharCount = currCharCount + word.length
        val totalWordCount = currWordCount + 1

        return !(totalCharCount <= charLimit && totalWordCount <= maxWords)
    }

    private fun exceedsLimits(sentenceList: ArrayList<String>,
                      nextSubtitleList: ArrayList<String>,
                      maxWords: Int, charLimit: Int): Boolean {
        val currCharCount = countTotalChars(nextSubtitleList)
        val currWordCount = nextSubtitleList.size

        val charCount = countTotalChars(sentenceList)
        val wordCount = sentenceList.size

        val totalCharCount = currCharCount + charCount
        val totalWordCount = currWordCount + wordCount

        return !(totalCharCount <= charLimit && totalWordCount <= maxWords)
    }

    private fun countTotalChars(strings: ArrayList<String>): Int {
        return strings.sumOf { it.length }
    }

    private fun containsAnyChar(text: String, chars: ArrayList<Char>): Boolean {
        return text.any { it in chars }
    }

    private fun getParagraphs(original: String): ArrayList<String> {
        val paragraphs = ArrayList<String>()
        val regex = Regex(""".*?(?:\n\n|$)""", RegexOption.DOT_MATCHES_ALL)

        val matches = regex.findAll(original)
        for (match in matches) {
            val paragraph = match.value
            if (paragraph.isNotEmpty()) {
                paragraphs.add(paragraph)
            }
        }

        return paragraphs
    }

    private fun getWords(original: String): ArrayList<String> {
        val words = ArrayList<String>()
        val regex = Regex("""\S+\s*""") // Matches a non-whitespace sequence (a word) + any trailing whitespace

        val matches = regex.findAll(original)
        for (match in matches) {
            words.add(match.value)
        }

        return words
    }

    private fun generateId(): Int {
        val startRandomID = 100000
        val endRandomID = 999999
        val id = Random().nextInt((endRandomID + 1) - startRandomID) + startRandomID
        return if(ids.contains(id)) generateId() else id
    }

    fun getDateString(): String {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val year = Calendar.getInstance().get(Calendar.YEAR)

        return "$month:$day:$year"
    }
}