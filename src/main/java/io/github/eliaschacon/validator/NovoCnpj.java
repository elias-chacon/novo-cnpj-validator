package io.github.eliaschacon.validator;


import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * Validação de CNPJ Alfanumérico (novo formato da Receita Federal - IN RFB nº 2.229/2024).
 * Aceita CNPJs legados (apenas números) e os novos com letras A-Z nas 12 primeiras posições.
 * <p>
 * A anotação pode ser usada em campos String. Exemplo:
 * <pre>{@code
 * @NovoCnpj
 * private String cnpj;
 * }</pre>
 * </p>
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = NovoCnpjValidator.class)
@Documented
public @interface NovoCnpj {
    String message() default "CNPJ inválido (formato alfanumérico ou dígitos verificadores incorretos)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
