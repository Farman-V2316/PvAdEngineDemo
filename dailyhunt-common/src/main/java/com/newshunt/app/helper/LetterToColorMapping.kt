/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */


package com.newshunt.app.helper

import com.newshunt.common.helper.common.Constants

/**
 * @author anshul.jain
 * A class for keeping mapping between colors and alphabets.
 */
class LetterToColorMapping {

    companion object {

        private val colorCodeForHash = "#356AC4"

        private val letterToColorMap = mapOf(
                "A" to "#405DE6",
                "B" to "#5851DB",
                "C" to "#833AB4",
                "D" to "#C13584",
                "E" to "#E1306C",
                "F" to "#FD1D1D",
                "G" to "#F56040",
                "H" to "#F77737",
                "I" to "#FCAF45",
                "J" to "#B1DF14",
                "K" to "#3EA11A",
                "L" to "#26BE6A",
                "M" to "#0ADD61",
                "N" to "#13E5C1",
                "O" to "#405DE6",
                "P" to "#5851DB",
                "Q" to "#833AB4",
                "R" to "#C13584",
                "S" to "#E1306C",
                "T" to "#FD1D1D",
                "U" to "#F56040",
                "V" to "#F77737",
                "W" to "#FCAF45",
                "X" to "#B1DF14",
                "Y" to "#3EA11A",
                "Z" to "#26BE6A",
                "#" to colorCodeForHash)

        fun getCharacterAndColor(key: String): Pair<String, String> {
            val character = if (letterToColorMap.keys.contains(key)) key else Constants.HASH_CHARACTER
            return Pair(character, letterToColorMap.get(character) ?: colorCodeForHash)
        }
    }
}