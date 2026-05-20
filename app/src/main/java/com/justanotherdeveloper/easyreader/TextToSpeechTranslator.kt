package com.justanotherdeveloper.easyreader


private val translations = HashMap<String, String>()
private val translationsFilename = "TRANSLATIONS_FILE"

private var translationsSaved = false
private val translationsSavedRef = "TRANSLATIONS_SAVED"

private var matchCaseCurrent = false
private var matchCaseRef = "MATCH_CASE_REF"

fun addTranslation(orig: String, translation: String, local: TinyDB) {
    translations[orig] = translation
    saveTranslations(local)
}

fun removeTranslation(orig: String, local: TinyDB) {
    translations.remove(orig)
    saveTranslations(local)
}

fun updateMatchCase(local: TinyDB) {
    matchCaseCurrent = local.getBoolean(matchCaseRef)
}

fun setMatchCase(matchCase: Boolean, local: TinyDB) {
    matchCaseCurrent = matchCase
    local.putBoolean(matchCaseRef, matchCase)
}

fun matchCase(): Boolean {
    return matchCaseCurrent
}

private fun saveTranslations(local: TinyDB) {
    local.putObject(translationsFilename, translations)
    if(translationsSaved) return
    translationsSaved = true
    local.putBoolean(translationsSavedRef, true)
}

fun updateTranslations(local: TinyDB) {
    if(!local.getBoolean(translationsSavedRef)) return
    translationsSaved = true
    val savedTranslations = local.getObject(translationsFilename, HashMap::class.java)
    for(orig in savedTranslations.keys)
        translations[orig.toString()] = savedTranslations[orig.toString()].toString()
}

fun getTranslations(): HashMap<String, String> {
    return translations
}

fun String.translate(): String {
    var newStr = this
    for(orig in translations.keys)
        newStr = newStr.replaceWord(orig, translations[orig]!!, !matchCaseCurrent)
    return newStr
}

private fun String.replaceWord(oldWord: String, newWord: String, ignoreCase: Boolean = false): String {
    if(!this.contains(oldWord, ignoreCase)) return this
    if(this == oldWord) return newWord
    if(ignoreCase && this.lowercase() == oldWord.lowercase())
        return newWord
    val words = getWords(this)
    for((i, word) in words.withIndex()) {
        if(word.contains(oldWord)) {
            val wordContents = word.splitWordFromRest(oldWord)
            for((j, part) in wordContents.withIndex()) {
                if (ignoreCase) {
                    if (oldWord.lowercase() == part.lowercase())
                        wordContents[j] = newWord
                } else {
                    if (oldWord == part)
                        wordContents[j] = newWord
                }
            }
            val wordUpdated = wordContents.joinToString("")
            if(word != wordUpdated) words[i] = wordUpdated
        }
    }
    val updatedStr = words.joinToString("")
    return updatedStr
}

private fun String.splitWordFromRest(oldWord: String): ArrayList<String> {
    val wordElements = ArrayList<String>()
    var nextStr = ""
    var lettersFound = false
    var lettersOver = false
    for(ch in this) {
        if(lettersOver) nextStr += ch
        else if(lettersFound) {
            if(!ch.isWordChar(oldWord)) {
                lettersOver = true
                wordElements.add(nextStr)
                nextStr = ""
            }
            nextStr += ch
        } else {
            if(ch == oldWord.first()) {
                lettersFound = true
                wordElements.add(nextStr)
                nextStr = ""
            }
            nextStr += ch
        }
    }
    wordElements.add(nextStr)
    return wordElements
}

private fun Char.isWordChar(oldWord: String): Boolean {
    val chars = ArrayList<Char>()
    for(ch in oldWord) if(!ch.isLetterOrDigit()) chars.add(ch)
    return isLetterOrDigit() || containsAnyChar(toString(), chars)
}

fun containsAnyChar(text: String, chars: ArrayList<Char>): Boolean {
    return text.any { it in chars }
}

fun getWords(str: String): ArrayList<String> {
    val words = ArrayList<String>()
    val regex = Regex("""\S+\s*""") // Matches a non-whitespace sequence (a word) + any trailing whitespace

    val matches = regex.findAll(str)
    for (match in matches)
        words.add(match.value)

    return words
}