package sk.stuba.pks.library.util

import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import dev.nesk.akkurate.validatables.Validatable
import sk.stuba.pks.library.validators.Answer
import sk.stuba.pks.library.validators.validation.accessors.answer

object Asker {
    fun ask(
        message: String,
        validatorRunner: Validator.Runner<Answer>,
        errorMessage: String? = null,
    ): Answer {
        println(message)
        while (true) {
            when (val res = validatorRunner(Answer(readln()))) {
                is ValidationResult.Success -> {
                    return res.value
                }

                is ValidationResult.Failure -> {
                    println(errorMessage ?: "Invalid input, please try again.")
                }
            }
        }
    }

    fun askWithOptions(
        message: String,
        validatorRunner: Validator.Runner<Answer>,
        options: List<String>,
    ): Answer {
        println(message)
        val validator =
            Validator<Answer> {
                answer.isNotEmpty()
                answer.isMatching("^(${options.joinToString("|")})$".toRegex())
            }
        while (true) {
            when (val res = validatorRunner(Answer(readln()))) {
                is ValidationResult.Success -> {
                    while (true) {
                        when (val res2 = validator(res.value)) {
                            is ValidationResult.Success -> {
                                return res2.value
                            }

                            is ValidationResult.Failure -> {
                                println("Invalid input, please try again.")
                            }
                        }
                    }
                }

                is ValidationResult.Failure -> {
                    println("Invalid input, please try again.")
                }
            }
        }
    }

    fun askWithOptions(
        message: String,
        options: List<String>,
    ): Answer {
        println(message)
        val validator =
            Validator<Answer> {
                answer.isNotEmpty()
                answer.isMatching("^(${options.joinToString("|") { "($it)" }})$".toRegex())
            }
        while (true) {
            when (val res2 = validator(Answer(readln()))) {
                is ValidationResult.Success -> {
                    return res2.value
                }

                is ValidationResult.Failure -> {
                    println("Invalid input, please try again.")
                    println("Available options: $options")
                }
            }
        }
    }

    fun ask(
        message: String,
        errorMessage: String? = null,
        runnerGetter: Validatable<Answer>.() -> Unit,
    ): Answer {
        println(message)
        while (true) {
            val runner =
                Validator<Answer> {
                    runnerGetter()
                }
            when (val res = runner(Answer(readln()))) {
                is ValidationResult.Success -> {
                    return res.value
                }

                is ValidationResult.Failure -> {
                    println(errorMessage ?: "Invalid input, please try again.")
                }
            }
        }
    }
}
