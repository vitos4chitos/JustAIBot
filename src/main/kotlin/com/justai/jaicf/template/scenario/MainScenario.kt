package com.justai.jaicf.template.scenario

import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.template.data.GameContext
import com.justai.jaicf.template.data.Word
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Files
import java.nio.file.Paths

val mainScenario = Scenario {
    state("start") {
        activators {
            regex("/start")
            intent("Hello")
        }
        action {
            reactions.run {
                sayRandom(
                    "Привет!",
                    "Привет, сыграем?"
                )
            }
        }
    }

    state("bye") {
        activators {
            intent("Bye")
        }

        action {
            reactions.sayRandom(
                "Пока-Пока",
                "Ещё увидимся!"
            )
        }
    }

    state("game") {
        activators {
            regex("/game")
            intent("Game")
        }

        action {
            val message = request.telegram?.message
            val username = message?.chat?.username
            reactions.say("Сейчас подберу для вас слово, $username")
            val reader = Files.newBufferedReader(
                Paths
                    .get("C:\\Users\\angry\\OneDrive\\Рабочий стол\\Work\\JustAIBot\\src\\main\\resources\\hangmanGameData.csv")
            )
            val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(';'))
            val numOfWord = (0..1239).random()
            val word = csvParser.records[numOfWord].get(1)
            csvParser.close()
            reader.close()
            reactions.say("Угадай слово из ${word.length} букв и получи приз!")
            reactions.say("Напоминаю, чтобы закончить игру заранее, пропишите /stop")
            context.client["game"] = GameContext(Word(word), 6, "")
        }
        state("stop_game"){
            activators{
                regex("/stop")
            }
            action{
                reactions.go("/end")
            }
        }
        state("game_context") {
            activators {
                catchAll()
            }

            action {
                val game = context.client["game"] as? GameContext
                val word = request.input.toLowerCase()
                if (game != null) {
                    if (word.length == 1) {
                        if (word[0].isLetter()) {
                            if (game.getLetters().contains(word)) {
                                reactions.run {
                                    sayRandom(
                                        "Ты уже вводил что-то подобное. Вот список: ${game.getLetters()}",
                                        "Это уже было. Вот список: ${game.getLetters()}",
                                        "Проверь ещё раз, это было: ${game.getLetters()}"
                                    )
                                    changeStateBack()
                                }
                            }
                            else {
                                if(game.setLetters(word[0])) {
                                    reactions.sayRandom("Вы угадали букву!", "Браво, правильная буква!")
                                    if(!game.getWordWithMisses().contains('_')) {
                                        reactions.run {
                                            sayRandom(
                                                "Ура, победа! Это действительно слово ${game.getWord()}",
                                                "Вы угадали слово ${game.getWord()}, поздравляю"
                                            )
                                            go("/end")
                                        }
                                    }
                                    else {
                                        context.client["game"] = game
                                        reactions.run {
                                            sayRandom(
                                                "Рано раслябляться! Вот что пока вам известно: ${game.getWordWithMisses()}",
                                                "${game.getWordWithMisses()} , продолжаем игру!"
                                            )
                                            changeStateBack()
                                        }
                                    }
                                }
                                else {
                                    game.setHearts()
                                    context.client["game"] = game
                                    reactions.run {
                                        if(game.getHearts() > 0) {
                                            sayRandom(
                                                "Такой буквы нет, угадывайте дальше.",
                                                "Неверно, попробуйте ещё!"
                                            )
                                            if(game.getHearts() < 3) {
                                                sayRandom(
                                                    "Попытки почти кончились. Осталось ${game.getHearts()} из 6 попыток!",
                                                    "Вы почти проиграли! Осталось ${game.getHearts()} из 6 попыток!"
                                                )
                                            }
                                            changeStateBack()
                                        }
                                        else {
                                            sayRandom(
                                                "Такой буквы нет, но угадывать дальше вы не можете. Вы проиграли.",
                                                "Неверно, но игра кончилась. Вы потратили все свои попытки."
                                            )
                                            go("/end")
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            reactions.run {
                                sayRandom(
                                    "Это не буква!",
                                    "Так, это же не буква... Исправляйся!",
                                    "Я различаю только слова и буквы..."
                                )
                                changeStateBack()
                            }
                        }
                    }
                    else {
                        if (word.equals(game.getWord())) {
                            reactions.run {
                                sayRandom(
                                    "Ура, победа! Это действительно слово $word",
                                    "Вы угадали слово $word, поздравляю"
                                )
                                go("/end")
                            }
                        }
                        else {
                            game.setHearts()
                            context.client["game"] = game
                            reactions.run {
                                if(game.getHearts() > 0) {
                                    sayRandom(
                                        "Это не то слово! Попробуйте ещё",
                                        "Неверно, попробуйте ещё!"
                                    )
                                    if(game.getHearts() < 3) {
                                        sayRandom(
                                            "Попытки почти кончились. Осталось ${game.getHearts()} из 6 попыток!",
                                            "Вы почти проиграли! Осталось ${game.getHearts()} из 6 попыток!"
                                        )
                                    }
                                    changeStateBack()
                                }
                                else {
                                    sayRandom(
                                        "Такой буквы нет, но угадывать дальше вы не можете. Вы проиграли. Это было слово ${game.getWord()}",
                                        "Неверно, но игра кончилась. Вы потратили все свои попытки. А загаданное слово было: ${game.getWord()}"
                                    )
                                    go("/end")
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    state("end") {
        activators{
        }
        action {
            context.client.clear()
            reactions.sayRandom("Не переживай. В следующий раз обязательно выиграешь!",
                "Обязательно сыграем ещё! Уверен, в следующий раз победа будет за тобой!")
        }
    }

    state("help"){
        activators {
            regex("/help")
            intent("Help")
        }
        action{
            reactions.say("Я бот, который всегда с тобой может сыграть в виселицу. Более того, я умею здороваться и прощаться " +
                    "\t Чтобы поиграть, напиши играть или /game \t" +
                    "Чтобы узнать правила, напиши правила или /rules \t" +
                    "Чтобы закончить игру заранее, напиши /stop в игре \t" +
                    "Приятной игры!")
        }
    }

    state("rules"){
        activators {
            regex("/rules")
            intent("Rules")
        }
        action{
            reactions.say("Вот правила игры: \t" +
                    "Я загадываю слово, твоя задача отгадать его. Все просто? Но у тебя есть всего 6 прав на ошибку." +
                    "Я обязательно сообщу, когда останется 2...1...0. \t"
                    + "Ты всегда можешь угадывать по буквам, отсылая их мне. Либо попробовать угадать слово целиком. Но" +
                    " за каждую ошибку я буду отбирать у тебя право на ошибку. \t" +
                    "Удачи и приятной игры! Помни, что все загаданные слова на русском языке, и я не прощаю ошибок.")
        }
    }
    fallback {
        reactions.sayRandom(
            "Простите но я не понимаю вас. Введите /help, чтобы узнать о моем функционале. ",
            "Я ещё не обучился отвечать на это, поэтому введите /help, чтобы разобраться в моем функционале"
        )
    }
}

