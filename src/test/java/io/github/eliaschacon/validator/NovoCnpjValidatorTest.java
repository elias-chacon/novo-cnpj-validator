package io.github.eliaschacon.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NovoCnpjValidatorTest {

    private NovoCnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NovoCnpjValidator();
    }

    // ========== TESTES PARA CNPJs VÁLIDOS ==========

    @ParameterizedTest
    @CsvSource({
            "00.000.000/0001-91",
            "00000000000191",
            "12.ABC.345/01AB-77",
            "12ABC34501AB77",
            "12.abc.345/01ab-77",
            "04.252.011/0001-10",
    })
    void testValidCnpj(String cnpj) {
        assertTrue(validator.isValid(cnpj, null),
                "CNPJ deveria ser válido: " + cnpj);
    }

    // ========== TESTES PARA CNPJs INVÁLIDOS ==========

    @ParameterizedTest
    @CsvSource({
            // Dígitos verificadores incorretos
            "00.000.000/0001-92",
            "12.ABC.345/01AB-36",
            "A1B2C3D4E5F699",
            "12ABC34501AB36",
            "00000000000192",

            // Tamanho incorreto
            "12.ABC.345/01AB-7",
            "12.ABC.345/01AB-777",
            "1234567890123",
            "123456789012345",

            // Caractere inválido que impede a limpeza correta? Na verdade qualquer caractere não alfanumérico é removido.
            // Então para forçar invalidade, use tamanho errado ou dígitos errados.
            // Exemplo: adicionar caractere não alfanumérico no meio sem alterar dígitos pode continuar válido.
            // Por isso, não usamos esses casos.
    })
    void testInvalidCnpj(String cnpj) {
        assertFalse(validator.isValid(cnpj, null),
                "CNPJ deveria ser inválido: " + cnpj);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000000",
            "11111111111111",
            "22222222222222",
            "33333333333333",
            "44444444444444",
            "55555555555555",
            "66666666666666",
            "77777777777777",
            "88888888888888",
            "99999999999999",
            "AAAAAAAAAAAAAA",
            "BBBBBBBBBBBBBB",
            "ZZZZZZZZZZZZZZ"
    })
    void testUniformSequences(String cnpj) {
        assertFalse(validator.isValid(cnpj, null),
                "Sequência uniforme deveria ser rejeitada: " + cnpj);
    }

    @Test
    void testNullIsInvalid() {
        assertFalse(validator.isValid(null, null));
    }
}