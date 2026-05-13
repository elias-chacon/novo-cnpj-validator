package io.github.eliaschacon.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NovoCnpjValidator implements ConstraintValidator<NovoCnpj, String> {

    private static final int[] WEIGHTS_FIRST_DIGIT = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_SECOND_DIGIT = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public void initialize(NovoCnpj constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        if (cnpj == null) {
            return false; // use @NotNull separadamente se quiser permitir nulo
        }

        // 1. Limpeza: remove caracteres não alfanuméricos e converte para maiúsculas
        String cleaned = cnpj.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        // 2. Deve ter exatamente 14 caracteres alfanuméricos (A-Z0-9)
        if (cleaned.length() != 14) {
            return false;
        }

        // 3. Rejeita sequências uniformes (ex.: "00000000000000", "AAAAAAAAAAAAAA")
        if (cleaned.matches("^([A-Z0-9])\\1{13}$")) {
            return false;
        }

        // 4. Extrai a base (12 primeiros) e os dois dígitos informados
        String base = cleaned.substring(0, 12);
        char expectedFirstDigit = calculateCheckDigit(base, WEIGHTS_FIRST_DIGIT);
        char expectedSecondDigit = calculateCheckDigit(base + expectedFirstDigit, WEIGHTS_SECOND_DIGIT);

        // 5. Compara com os dígitos fornecidos
        return cleaned.charAt(12) == expectedFirstDigit && cleaned.charAt(13) == expectedSecondDigit;
    }

    /**
     * Calcula um dígito verificador com base na string de entrada e nos pesos definidos.
     * <p>
     * Regra: cada caractere é convertido para seu valor numérico:
     * - '0' a '9' → 0 a 9
     * - 'A' a 'Z' → código ASCII - 48 (A=17, B=18, ..., Z=42)
     * </p>
     *
     * @param baseString string de entrada (ex.: 12 primeiros caracteres, ou base+primeiro dígito)
     * @param weights    array de pesos (tamanho igual ao da baseString)
     * @return caractere ('0'..'9') representando o dígito verificador calculado
     */
    private char calculateCheckDigit(String baseString, int[] weights) {
        int sum = 0;
        for (int i = 0; i < baseString.length(); i++) {
            char c = baseString.charAt(i);
            int value = charToValue(c);
            sum += value * weights[i];
        }
        int remainder = sum % 11;
        int digit = (remainder < 2) ? 0 : 11 - remainder;
        return (char) (digit + '0');
    }

    /**
     * Converte caractere alfanumérico (0-9, A-Z) para seu valor numérico conforme especificação da Receita Federal.
     *
     * @param c caractere (já em maiúsculo)
     * @return valor numérico (0-9 para dígitos, 17-42 para letras)
     * @throws IllegalArgumentException se o caractere não for alfanumérico (não deve ocorrer)
     */
    private int charToValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'A' && c <= 'Z') {
            return c - '0'; // em ASCII, 'A' = 65, '0' = 48, diferença = 17
        } else {
            throw new IllegalArgumentException("Caractere inválido para CNPJ: " + c);
        }
    }

}
