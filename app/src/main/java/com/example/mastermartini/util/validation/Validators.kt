package com.example.mastermartini.util.validation

import com.example.mastermartini.model.QuoteRules
import com.example.mastermartini.util.validation.ValidationResult.Invalid
import com.example.mastermartini.util.validation.ValidationResult.Valid
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Every form rule in the app, in one place and free of Android/UI code so it can be
 * unit-tested. ViewModels call these and put the result into each field's [FieldState].
 * Messages are in Spanish because they are shown directly under the fields.
 */
object Validators {

    const val NAME_MIN = 3
    const val PASSWORD_MIN = 8
    const val BUSINESS_NAME_MIN = 3
    const val ADDRESS_MIN = 5
    const val MESSAGE_MIN = 10
    const val MESSAGE_MAX = 500

    private val NAME_CHARS = Regex("^[\\p{L} ]+$")
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_CL = Regex("^(\\+?56)?9\\d{8}$")
    private val RUT_FORMAT = Regex("^(\\d{1,2}(\\.\\d{3}){2}|\\d{7,8})-[\\dkK]$")
    private val PHONE_SEPARATORS = Regex("[\\s-]")

    fun name(value: String): ValidationResult {
        val text = value.trim()
        return when {
            text.isEmpty() -> Invalid("Ingresa tu nombre")
            !NAME_CHARS.matches(text) -> Invalid("Usa solo letras y espacios")
            text.length < NAME_MIN -> Invalid("Debe tener al menos $NAME_MIN letras")
            else -> Valid
        }
    }

    fun email(value: String): ValidationResult {
        val text = value.trim()
        return when {
            text.isEmpty() -> Invalid("Ingresa tu correo")
            !EMAIL.matches(text) -> Invalid("Correo no válido (ej: nombre@empresa.cl)")
            else -> Valid
        }
    }

    /** Password rules for registration. */
    fun password(value: String): ValidationResult = when {
        value.isEmpty() -> Invalid("Ingresa una contraseña")
        value.length < PASSWORD_MIN -> Invalid("Debe tener al menos $PASSWORD_MIN caracteres")
        value.none { it.isLetter() } -> Invalid("Debe incluir al menos una letra")
        value.none { it.isDigit() } -> Invalid("Debe incluir al menos un número")
        else -> Valid
    }

    /** Login only checks the password was typed; the rules apply when registering. */
    fun loginPassword(value: String): ValidationResult =
        if (value.isEmpty()) Invalid("Ingresa tu contraseña") else Valid

    fun confirmPassword(password: String, confirmation: String): ValidationResult = when {
        confirmation.isEmpty() -> Invalid("Repite la contraseña")
        confirmation != password -> Invalid("Las contraseñas no coinciden")
        else -> Valid
    }

    /** Chilean mobile: +56 9 XXXX XXXX or 9XXXXXXXX (spaces and dashes are ignored). */
    fun phoneCL(value: String): ValidationResult {
        val digits = value.replace(PHONE_SEPARATORS, "")
        return when {
            digits.isEmpty() -> Invalid("Ingresa tu teléfono")
            !PHONE_CL.matches(digits) -> Invalid("Usa un celular chileno: +56 9 1234 5678")
            else -> Valid
        }
    }

    /** RUT as 12.345.678-5 or 12345678-5, with a correct modulus-11 check digit. */
    fun rut(value: String): ValidationResult {
        val text = value.trim()
        if (text.isEmpty()) return Invalid("Ingresa el RUT de la empresa")
        if (!RUT_FORMAT.matches(text)) return Invalid("Formato no válido (ej: 12.345.678-5)")
        val body = text.substringBefore('-').replace(".", "")
        val checkDigit = text.substringAfter('-').uppercase().single()
        return if (rutCheckDigit(body) == checkDigit) Valid else Invalid("El dígito verificador no es correcto")
    }

    /** Modulus 11: weights 2..7 from right to left; 11 → '0', 10 → 'K'. */
    fun rutCheckDigit(body: String): Char {
        var sum = 0
        var weight = 2
        for (digit in body.reversed()) {
            sum += digit.digitToInt() * weight
            weight = if (weight == 7) 2 else weight + 1
        }
        return when (val result = 11 - sum % 11) {
            11 -> '0'
            10 -> 'K'
            else -> result.digitToChar()
        }
    }

    fun businessName(value: String): ValidationResult {
        val text = value.trim()
        return when {
            text.isEmpty() -> Invalid("Ingresa el nombre de la empresa")
            text.length < BUSINESS_NAME_MIN -> Invalid("Debe tener al menos $BUSINESS_NAME_MIN caracteres")
            else -> Valid
        }
    }

    /** Delivery date: required, from tomorrow on, and not on a Sunday. */
    fun deliveryDate(date: LocalDate?, today: LocalDate): ValidationResult = when {
        date == null -> Invalid("Selecciona una fecha de entrega")
        !date.isAfter(today) -> Invalid("La fecha debe ser a partir de mañana")
        date.dayOfWeek == DayOfWeek.SUNDAY -> Invalid("No hay despachos los domingos")
        else -> Valid
    }

    fun address(value: String): ValidationResult {
        val text = value.trim()
        return when {
            text.isEmpty() -> Invalid("Ingresa la dirección de despacho")
            text.length < ADDRESS_MIN -> Invalid("Debe tener al menos $ADDRESS_MIN caracteres")
            else -> Valid
        }
    }

    /** Quantity typed in the Cotización tab, as text. */
    fun quantity(value: String): ValidationResult {
        val text = value.trim()
        val number = text.toIntOrNull()
        return when {
            text.isEmpty() -> Invalid("Ingresa una cantidad")
            number == null -> Invalid("Usa solo números")
            number !in QuoteRules.MIN_QUANTITY..QuoteRules.MAX_QUANTITY ->
                Invalid("Entre ${QuoteRules.MIN_QUANTITY} y ${QuoteRules.MAX_QUANTITY}")
            else -> Valid
        }
    }

    fun message(value: String): ValidationResult {
        val length = value.trim().length
        return when {
            length == 0 -> Invalid("Escribe tu mensaje")
            length < MESSAGE_MIN -> Invalid("Debe tener al menos $MESSAGE_MIN caracteres")
            length > MESSAGE_MAX -> Invalid("Máximo $MESSAGE_MAX caracteres ($length/$MESSAGE_MAX)")
            else -> Valid
        }
    }
}
