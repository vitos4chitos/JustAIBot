package com.justai.jaicf.template.data

public class GameContext(private var word: Word, private var hearts: Int, private var letters: String){

    fun getHearts(): Int{
        return hearts
    }

    fun setHearts() {
        hearts--
    }

    fun getLetters(): String{
        return letters
    }

    fun setLetters(letter: Char): Boolean {
        letters += letter
        return word.changeState(letter)
    }

    fun getWord(): String{
        return word.getWord()
    }

    fun getWordWithMisses(): String {
        return word.getWordWithMisses()
    }

}