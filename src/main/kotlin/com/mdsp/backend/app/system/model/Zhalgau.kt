package com.mdsp.backend.app.system.model

class Zhalgau {
    companion object {

        private const val alphabet = "аәбвгғдеёжзийкқлмнңоөпрстуұүфхһцчшщъыіьэюя"

        private fun isCorrectName(str: String): Boolean {
            for (ch in str) {
                if (!alphabet.contains(ch, true)) throw Exception("WrongName")
            }
            return true
        }

        fun septik(tubir: String, newinwi: Int): String {
            isCorrectName(tubir)
            val last = lastV(tubir)
            val lastV = last[0]
            val a = tubir.length - 1
            val lastC = if (newinwi == 3) last[1] else tubir[a]
            return when (newinwi) {
                1 -> tubir
                2 -> tubir + zhalgauSeptik(lastC, lastV, 1) + if (typeVowel(lastV) >= 1) "ың" else "ің"
                3 -> tubir + zhalgauSeptik(lastC, lastV, 2)
                4 -> tubir + zhalgauSeptik(lastC, lastV, 3) + if (typeVowel(lastV) >= 1) "ы" else "і"
                5 -> tubir + zhalgauSeptik(lastC, lastV, 4) + if (typeVowel(lastV) >= 1) "а" else "е"
                6 -> tubir + zhalgauSeptik(lastC, lastV, 5) + if (typeVowel(lastV) >= 1) "ан" else "ен"
                7 -> tubir + zhalgauSeptik(lastC, lastV, 6) + "ен"
                else -> "wrong"
            }
        }

        private fun lastV(tubir: String): String {
            val len = tubir.length
            var a = ' '
            var b = tubir[len - 1]
            when {
                typeConsonant(b) > 2 -> {
                    for (i in (len - 2) downTo 0) {
                        if (typeConsonant(tubir[i]) <= 2) {
                            a = tubir[i]
                            break
                        }
                    }
                    return "" + a + b
                }
                typeConsonant(b) == 2 -> {
                    for (i in (len - 2) downTo 0) {
                        if (typeConsonant(tubir[i]) <= 2) {
                            a = tubir[i]
                            break
                        }
                    }
                    return if (typeConsonant(tubir[len - 2]) >= 3) {
                        "" + a + tubir[len - 2]
                    } else {
                        "" + tubir[len - 2] + b
                    }
                }
                else -> {
                    for (i in (len - 1) downTo 0) {
                        if (typeConsonant(tubir[i]) > 3) {
                            b = tubir[i]
                            break
                        }
                    }
                    return "" + tubir[len - 1] + b
                }
            }
        }

        private fun zhalgauSeptik(lastC: Char, lastV: Char, type: Int): String {
            val typeC = typeConsonant(lastC)
            when (type) {
                1, 5 -> {
                    val zhalgau = when {
                        typeC < 2 && type == 1 -> "н"
                        typeC <= 4 -> "д"
                        typeC == 5 -> "н"
                        typeC == 6 -> "т"
                        else -> ""
                    }
                    return zhalgau
                }
                2 -> {
                    val zhalgau = if ("пкқтсфхцчшщһ".contains(lastC)) {
                        if (typeVowel(lastV) >= 1) "қа" else "ке"
                    } else {
                        if (typeVowel(lastV) >= 1) "ға" else "ге"
                    }
                    return zhalgau
                }
                3, 4 -> {
                    val zhalgau = when {
                        typeC < 2 && type == 3 -> return "н"
                        typeC <= 5 -> return "д"
                        typeC == 6 -> return "т"
                        else -> ""
                    }
                    return zhalgau
                }
                6 -> {
                    val zhalgau = when {
                        "рлйумнң".contains(lastC) || typeC < 2 -> return "м"
                        "бвгғджз".contains(lastC) -> return "б"
                        "пфкқтсшщхцчһ".contains(lastC) -> return "п"
                        else -> ""
                    }
                    return zhalgau
                }
            }
            return ""
        }

        private fun typeConsonant(ch: Char): Int {
            val t = "пкқтсфхцчшщһ" + "бвгғд"
            val n = "млнңл"
            val d = "жз" + "рй"
            val dibisEmes = "ъь"
            return when {
                t.contains(ch) -> 6
                n.contains(ch) -> 5
                d.contains(ch) -> 4
                dibisEmes.contains(ch) -> 3
                else -> typeVowel(ch)
            }
        }


        private fun typeVowel(ch: Char): Int {
            val vowels = "аоұыяю"
            val u = 'у'
            return if (u === ch) 2 else if (vowels.contains(ch)) 1 else 0
        }


    }
}