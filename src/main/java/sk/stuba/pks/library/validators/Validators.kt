package sk.stuba.pks.library.validators

import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import sk.stuba.pks.library.util.MyValidatable
import sk.stuba.pks.library.validators.validation.accessors.answer

@Validate
data class Answer(
    val answer: String,
) : MyValidatable

val yesNoValidator =
    Validator<Answer> {
        answer.isNotEmpty()
        answer.isMatching("^([yn])$".toRegex())
    }
val numberValidator =
    Validator<Answer> {
        answer.isNotEmpty()
        answer.isMatching("^[0-9]+$".toRegex())
    }
val ipValidator =
    Validator<Answer> {
        answer.isNotEmpty()
        answer.isMatching("^(([0-9]{1,3}\\.){3}[0-9]{1,3})|(localhost)$".toRegex())
    }
