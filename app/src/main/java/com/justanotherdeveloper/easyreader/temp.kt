package com.justanotherdeveloper.easyreader

/**

// generates the subtitle set with the entered text
fun generateSubtitles(original: String, maxWords: Int): ArrayList<String> {
    val subtitles = ArrayList<String>()

    for(paragraph in getParagraphs(original))
        parseParagraph(subtitles, paragraph, maxWords, maxCharPerSubtitle)

    for((i, subtitle) in subtitles.withIndex())
        subtitles[i] = subtitle.standardizeWhiteSpaces()

    return subtitles
}

// converts a paragraph string into a 2D-array of words in separated sentences
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

// iterates through each sentence to add words to the next subtitle
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

// */