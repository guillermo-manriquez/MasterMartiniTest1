package com.example.mastermartini.util.validation

import com.example.mastermartini.util.validation.ValidationResult.Valid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidatorsTest {

    private fun assertValid(result: ValidationResult) = assertEquals(Valid, result)

    private fun assertInvalid(result: ValidationResult, expectedMessage: String? = null) {
        assertTrue("Expected Invalid but was $result", result is ValidationResult.Invalid)
        if (expectedMessage != null) assertEquals(expectedMessage, result.errorOrNull)
    }

    // --- name ---

    @Test fun name_acceptsLettersSpacesAndAccents() {
        assertValid(Validators.name("Ana"))
        assertValid(Validators.name("José Muñoz Pérez"))
        assertValid(Validators.name("  Ana  "))
    }

    @Test fun name_rejectsEmptyShortAndNonLetters() {
        assertInvalid(Validators.name(""), "Ingresa tu nombre")
        assertInvalid(Validators.name("   "), "Ingresa tu nombre")
        assertInvalid(Validators.name("Al"), "Debe tener al menos 3 letras")
        assertInvalid(Validators.name("Ana3"), "Usa solo letras y espacios")
        assertInvalid(Validators.name("Ana-María"), "Usa solo letras y espacios")
    }

    // --- email ---

    @Test fun email_acceptsValidAddresses() {
        assertValid(Validators.email("ana@empresa.cl"))
        assertValid(Validators.email("  ana.perez+compras@mail.empresa.com "))
    }

    @Test fun email_rejectsInvalidAddresses() {
        assertInvalid(Validators.email(""), "Ingresa tu correo")
        listOf("ana", "ana@", "ana@empresa", "@empresa.cl", "ana empresa@x.cl", "ana@empresa.c").forEach {
            assertInvalid(Validators.email(it), "Correo no válido (ej: nombre@empresa.cl)")
        }
    }

    // --- passwords ---

    @Test fun password_boundariesAndComposition() {
        assertValid(Validators.password("abcdefg1"))          // exactly 8
        assertInvalid(Validators.password(""), "Ingresa una contraseña")
        assertInvalid(Validators.password("abcdef1"), "Debe tener al menos 8 caracteres") // 7
        assertInvalid(Validators.password("12345678"), "Debe incluir al menos una letra")
        assertInvalid(Validators.password("abcdefgh"), "Debe incluir al menos un número")
    }

    @Test fun loginPassword_onlyRequiresAValue() {
        assertValid(Validators.loginPassword("x"))
        assertInvalid(Validators.loginPassword(""), "Ingresa tu contraseña")
    }

    @Test fun confirmPassword_mustMatch() {
        assertValid(Validators.confirmPassword("clave123", "clave123"))
        assertInvalid(Validators.confirmPassword("clave123", ""), "Repite la contraseña")
        assertInvalid(Validators.confirmPassword("clave123", "Clave123"), "Las contraseñas no coinciden")
    }

    // --- phone ---

    @Test fun phoneCL_acceptsChileanMobileFormats() {
        assertValid(Validators.phoneCL("912345678"))
        assertValid(Validators.phoneCL("+56 9 1234 5678"))
        assertValid(Validators.phoneCL("+56912345678"))
        assertValid(Validators.phoneCL("56 9 1234-5678"))
    }

    @Test fun phoneCL_rejectsOtherNumbers() {
        assertInvalid(Validators.phoneCL(""), "Ingresa tu teléfono")
        listOf("812345678", "91234567", "9123456789", "+56 2 2345 6789", "+54 9 1234 5678", "9abc45678").forEach {
            assertInvalid(Validators.phoneCL(it), "Usa un celular chileno: +56 9 1234 5678")
        }
    }

    // --- RUT ---

    @Test fun rutCheckDigit_computesModulus11IncludingKAndZero() {
        assertEquals('5', Validators.rutCheckDigit("12345678"))
        assertEquals('1', Validators.rutCheckDigit("11111111"))
        assertEquals('K', Validators.rutCheckDigit("10000013"))
        assertEquals('0', Validators.rutCheckDigit("10000004"))
    }

    @Test fun rut_acceptsBothFormatsAndLowercaseK() {
        assertValid(Validators.rut("12.345.678-5"))
        assertValid(Validators.rut("12345678-5"))
        assertValid(Validators.rut("10.000.013-K"))
        assertValid(Validators.rut("10000013-k"))
        assertValid(Validators.rut("10.000.004-0"))
        assertValid(Validators.rut("1.000.005-K"))   // 7-digit body
        assertValid(Validators.rut("1000005-K"))
    }

    @Test fun rut_rejectsWrongCheckDigitAndBadFormats() {
        assertInvalid(Validators.rut(""), "Ingresa el RUT de la empresa")
        assertInvalid(Validators.rut("12.345.678-9"), "El dígito verificador no es correcto")
        assertInvalid(Validators.rut("10000013-0"), "El dígito verificador no es correcto")
        listOf("12345678", "12.345.6785", "12.34.5678-5", "123456789-5", "12345678-X", "abc").forEach {
            assertInvalid(Validators.rut(it), "Formato no válido (ej: 12.345.678-5)")
        }
    }

    // --- quote form ---

    @Test fun businessName_requiresThreeCharacters() {
        assertValid(Validators.businessName("Sol"))
        assertValid(Validators.businessName("Panadería El Sol SpA"))
        assertInvalid(Validators.businessName(" "), "Ingresa el nombre de la empresa")
        assertInvalid(Validators.businessName("So"), "Debe tener al menos 3 caracteres")
    }

    @Test fun deliveryDate_fromTomorrowAndNotSunday() {
        val today = LocalDate.of(2026, 10, 29) // Thursday
        assertValid(Validators.deliveryDate(today.plusDays(1), today))              // Friday
        assertValid(Validators.deliveryDate(LocalDate.of(2026, 10, 31), today))     // Saturday
        assertValid(Validators.deliveryDate(LocalDate.of(2026, 11, 2), today))      // Monday
        assertInvalid(Validators.deliveryDate(null, today), "Selecciona una fecha de entrega")
        assertInvalid(Validators.deliveryDate(today, today), "La fecha debe ser a partir de mañana")
        assertInvalid(Validators.deliveryDate(today.minusDays(1), today), "La fecha debe ser a partir de mañana")
        assertInvalid(Validators.deliveryDate(LocalDate.of(2026, 11, 1), today), "No hay despachos los domingos")
    }

    @Test fun address_requiresFiveCharacters() {
        assertValid(Validators.address("Av. 1"))
        assertInvalid(Validators.address(""), "Ingresa la dirección de despacho")
        assertInvalid(Validators.address(" Av1 "), "Debe tener al menos 5 caracteres")
    }

    @Test fun quantity_boundaries() {
        assertValid(Validators.quantity("1"))
        assertValid(Validators.quantity("999"))
        assertInvalid(Validators.quantity(""), "Ingresa una cantidad")
        assertInvalid(Validators.quantity("0"), "Entre 1 y 999")
        assertInvalid(Validators.quantity("1000"), "Entre 1 y 999")
        assertInvalid(Validators.quantity("-3"), "Entre 1 y 999")
        assertInvalid(Validators.quantity("2,5"), "Usa solo números")
        assertInvalid(Validators.quantity("diez"), "Usa solo números")
    }

    // --- contact ---

    @Test fun message_lengthBoundaries() {
        assertValid(Validators.message("a".repeat(10)))
        assertValid(Validators.message("a".repeat(500)))
        assertInvalid(Validators.message("   "), "Escribe tu mensaje")
        assertInvalid(Validators.message("a".repeat(9)), "Debe tener al menos 10 caracteres")
        assertInvalid(Validators.message("a".repeat(501)), "Máximo 500 caracteres (501/500)")
    }

    // --- FieldState ---

    @Test fun fieldState_showsErrorOnlyAfterTouched() {
        val initial = FieldState.initial(validate = Validators::name)
        assertFalse(initial.isValid)
        assertFalse(initial.showError)

        val typed = initial.onValueChange("Al", Validators::name)
        assertEquals("Debe tener al menos 3 letras", typed.error)
        assertFalse(typed.showError)

        val touched = typed.touch()
        assertTrue(touched.showError)

        val fixed = touched.onValueChange("Ana", Validators::name)
        assertTrue(fixed.touched)
        assertNull(fixed.error)
        assertTrue(fixed.isValid)
        assertFalse(fixed.showError)
    }
}
