package com.justanotherdeveloper.easyreader

import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList

// Management of Subtitle Set:
// - Generates Subtitle Set converting entered text into subtitles
// - Saves, updates, and deletes generated Subtitle Sets
class SubtitleSetManager(activity: AppCompatActivity) {

    private val tinyDB = TinyDB(activity)
    private var ids = tinyDB.getListInt(idsFilename)

    // Object containing generated subtitles, entered text, and other variables
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
        for(subtitle in subtitles)
            enteredText += subtitle
        subtitleSet.enteredText = enteredText
        subtitleSet.subtitles = subtitles
        updateSubtitleSet(subtitleSet)
    }

    // generates the subtitle set with the entered text
    fun generateSubtitles(enteredText: String, maxWordsPerSubtitle: Int): ArrayList<String> {
        val subtitles = ArrayList<String>()

        // subtitle to add when max words is reached
        var currentSubtitle = ""
        var nWordsInCurrSubtitle = 0

        // sentence to add to subtitle when end of sentence is reached
        var currentSentence = ""
        var nWordsInCurrSentence = 0

        // returns the index of the end of the next word
        // determined by a space or a new line
        fun endOfWordIndex(startIndex: Int): Int {
            val indexOfSpace = enteredText.indexOf(' ', startIndex)
            val indexOfLine = enteredText.indexOf('\n', startIndex)

            return if(indexOfLine == -1 || indexOfSpace != -1 && indexOfSpace < indexOfLine)
                indexOfSpace + 1
            else indexOfLine + 1
        }

        // indexes to get next word
        var nextWordStartIndex = 0
        var nextWordEndIndex = endOfWordIndex(0)

        // bool if all words added
        var lastIndexReached = false

        // returns number of '\n' in string
        fun nNewLines(text: String): Int {
            var count = 0
            for(ch in text)
                if(ch == '\n') count++
            return count
        }

        // moves sentence to subtitle string and clears sentence
        fun moveCurrSentenceToCurrSubtitle() {
            currentSubtitle += currentSentence
            nWordsInCurrSubtitle += nWordsInCurrSentence
            nWordsInCurrSentence = 0
            currentSentence = ""
        }

        // adds subtitle to list and clears subtitle string
        fun addCurrSubtitleToSet() {
            subtitles.add(currentSubtitle)
            currentSubtitle = ""
            nWordsInCurrSubtitle = 0
        }

        // returns if word is the end of a sentence
        fun wordIsEndOfSentence(word: String): Boolean {
            return word.contains('.')
                    || word.contains(';')
                    || word.contains('\n')
                    || word.contains('?')
                    || word.contains('!')
        }

        // loops through each word and adds to sentence;
        // adds sentence to subtitle when end of sentence is found &
        // adds subtitle to list when max words is reached.
        while(!lastIndexReached) {

            if(nextWordEndIndex == 0) {
                lastIndexReached = true
                nextWordEndIndex = enteredText.length
            }
            var word = enteredText.substring(nextWordStartIndex, nextWordEndIndex)

//            Log.d("dtag", "subtitle: $currentSubtitle | sentence: $currentSentence | word: $word |" +
//                    "nWordsInSubtitle: $nWordsInCurrSubtitle | nWordsInSentence: $nWordsInCurrSentence")

            nextWordStartIndex = nextWordEndIndex
            nextWordEndIndex = endOfWordIndex(nextWordStartIndex)

            // if word exceeds the max characters
            // the current sentence and subtitle is added to the list
            // and the word is split to have the max characters per section;
            // each section of the word with max characters is added to the list;
            // the new word becomes the last section of the original word.
            if(word.length > maxCharPerSubtitle) {

                if(nWordsInCurrSubtitle > 0) addCurrSubtitleToSet()
                if(nWordsInCurrSentence > 0) {
                    moveCurrSentenceToCurrSubtitle()
                    addCurrSubtitleToSet()
                }

                var longWordStartIndex = 0
                var longWordEndIndex = maxCharPerSubtitle + 1

                var longWordLastIndexReached = false

                while(!longWordLastIndexReached) {
                    currentSubtitle = word.substring(longWordStartIndex, longWordEndIndex)
                    addCurrSubtitleToSet()
                    longWordStartIndex = longWordEndIndex
                    longWordEndIndex += maxCharPerSubtitle

                    if(longWordEndIndex > word.length) {
                        longWordLastIndexReached = true
                        word = word.substring(longWordStartIndex, word.length)
                    }
                }
            }

            // if number of new lines exceeds max new lines, subtitle string is added to list
            if(nNewLines(currentSubtitle) + nNewLines(currentSentence) + nNewLines(word)
                > maxNewLinesPerSubtitle) {
                if(nWordsInCurrSubtitle == 0) moveCurrSentenceToCurrSubtitle()
                addCurrSubtitleToSet()
            }

            // if number of characters exceeds max characters, subtitle string is added to list
            if(currentSubtitle.length + currentSentence.length + word.length
                > maxCharPerSubtitle) {
                if(nWordsInCurrSubtitle == 0) moveCurrSentenceToCurrSubtitle()
                addCurrSubtitleToSet()
            }

            currentSentence += word
            nWordsInCurrSentence++

            // if number of words exceeds max words, subtitle string is added to list
            if(nWordsInCurrSubtitle + nWordsInCurrSentence == maxWordsPerSubtitle) {
                if(nWordsInCurrSubtitle == 0) moveCurrSentenceToCurrSubtitle()
                addCurrSubtitleToSet()
            }

            // if end of sentence is found, sentence is added to subtitle string
            if(wordIsEndOfSentence(word) || lastIndexReached)
                moveCurrSentenceToCurrSubtitle()

            // if last word was found, last subtitle string is added to list
            if(lastIndexReached)
                addCurrSubtitleToSet()
        }
        return subtitles
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