package com.fulls_bank.Fulls_Bank.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", "11144477735", "111.444.777-35", "12345678909"})
    void isValid_withCorrectCheckDigits_shouldAccept(String cpf) {
        assertTrue(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "52998224724", "52998224735", "00000000000", "11111111111", "99999999999"})
    void isValid_withWrongCheckDigitsOrRepeatedDigits_shouldReject(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "123", "5299822472", "529982247250", "5299822472a", "529.982.24725",
            "529-982-247.25", " 52998224725", "52998224725 "})
    void isValid_withWrongLengthOrFormat_shouldReject(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @Test
    void isValid_withNull_shouldBeLeftToNotBlank() {
        assertTrue(new CpfValidator().isValid(null, null));
        assertFalse(CpfValidator.isValid(null));
    }

    @Test
    void normalize_shouldKeepOnlyDigits() {
        assertEquals("52998224725", CpfValidator.normalize("529.982.247-25"));
        assertEquals("52998224725", CpfValidator.normalize("52998224725"));
        assertNull(CpfValidator.normalize(null));
    }
}
