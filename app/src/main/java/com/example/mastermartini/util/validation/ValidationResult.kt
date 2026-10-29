package com.example.mastermartini.util.validation

/** Outcome of validating one field. [Invalid.message] is shown under the field. */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

val ValidationResult.errorOrNull: String?
    get() = (this as? ValidationResult.Invalid)?.message
