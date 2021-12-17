package com.justai.jaicf.template.data

public class Word(private val word: String) {
    private var guessedLetters = Array<Boolean>(word.length,{false})

    fun getWordWithMisses(): String {
        var toPrint = "";
        for(i in guessedLetters.indices){
            toPrint += if(guessedLetters[i]){
                word[i]
            } else{
                '_'
            }
        }
        return toPrint
    }

    fun changeState(letter: Char): Boolean {
        return if(word.contains(letter)){
            for(i in guessedLetters.indices){
                if(word[i] == letter){
                    guessedLetters[i] = true
                }
            }
            true
        } else{
            false
        }
    }

    fun getWord(): String{
        return word
    }
}