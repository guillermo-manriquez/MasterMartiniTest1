package com.example.mastermartini.util.validation

/**
 * State of one form field inside a screen's UiState.
 *
 * [error] is recalculated on every change, but the UI only shows it once the
 * field has been [touched] (lost focus once, or the form was submitted).
 */
data class FieldState(
    val value: String = "",
    val error: String? = null,
    val touched: Boolean = false,
) {
    val isValid: Boolean get() = error == null

    val showError: Boolean get() = touched && error != null

    /** New value plus its validation result; keeps [touched] as it was. */
    fun onValueChange(newValue: String, validate: (String) -> ValidationResult): FieldState =
        copy(value = newValue, error = validate(newValue).errorOrNull)

    fun touch(): FieldState = copy(touched = true)

    companion object {
        /** An untouched field already validated, so Submit knows it is empty/invalid. */
        fun initial(value: String = "", validate: (String) -> ValidationResult): FieldState =
            FieldState(value = value, error = validate(value).errorOrNull)
    }
}
