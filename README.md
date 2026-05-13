# @NovoCnpj v1.0.0-SNAPSHOT – Validação de CNPJ Alfanumérico para Java (jakarta.validation)

Anotação customizada para validação de CNPJ no novo formato alfanumérico (IN RFB nº 2.229/2024), compatível com CNPJs numéricos legados.

---

## Sobre o CNPJ Alfanumérico

A Receita Federal publicou a **IN RFB nº 2.229/2024** para expandir a capacidade de registro de empresas. A partir de **julho de 2026**, novas inscrições poderão usar letras maiúsculas (A–Z) e números (0–9) nas posições 1 a 12. Os dois últimos dígitos continuam sendo **apenas numéricos** (0–9).

- CNPJs numéricos já existentes **não serão alterados**.
- Formato: `XX.XXX.XXX/XXXX-DD` (14 caracteres alfanuméricos, máscara opcional).
- Algoritmo de validação: módulo 11 com conversão de letras (`A`=17, `B`=18, …, `Z`=42).

---

## Funcionalidades

- Valida strings com ou sem máscara (pontos, barras, hífens).
- Aceita letras minúsculas (normaliza para maiúsculas).
- Rejeita sequências uniformes (`00000000000000`, `AAAAAAAAAAAAAA`, etc.).
- Compatível com CNPJs numéricos tradicionais.
- Pronto para uso com **jakarta.validation** (Spring, Hibernate Validator, etc.).

---

## Instalação

### Via GitHub Packages (Recomendado)

Adicione o repositório e a dependência ao seu `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/eliaschacon/novocnpjvalidator</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.eliaschacon</groupId>
        <artifactId>novo-cnpj-validator</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

**Nota:** Substitua `1.0.0` pela versão desejada.

---

### Via cópia manual

Copie as classes para o seu projeto:

```
NovoCnpj
NovoCnpjValidator
```

### Dependências (Maven)

```xml
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>2.0.2</version>
</dependency>
<dependency>
   <groupId>org.hibernate.validator</groupId>
   <artifactId>hibernate-validator</artifactId>
   <version>6.2.5.Final</version>
</dependency>
```

---

## Como usar

### 1. Anotar o campo no DTO

```java
import jakarta.validation.constraints.NotNull;
import io.github.eliaschacon.validator.NovoCnpj;

public class EmpresaDTO {

    @NotNull(message = "CNPJ é obrigatório")
    @NovoCnpj
    private String cnpj;

    // getters e setters
}
```

### 2. Executar a validação

```java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();

Set<ConstraintViolation<EmpresaDTO>> violations = validator.validate(dto);
if (!violations.isEmpty()) {
    // lida com erros
}
```

---

## Exemplos

### CNPJs válidos (numéricos legados)

| Entrada (com máscara) | Normalizado |
|-----------------------|-------------|
| `00.000.000/0001-91`  | `00000000000191` |
| `04.252.011/0001-10`  | `04252011000110` |

### CNPJs válidos (alfanuméricos novos)

| Entrada (com máscara) | Normalizado |
|-----------------------|-------------|
| `12.ABC.345/01AB-77`  | `12ABC34501AB77` |
| `12.abc.345/01ab-77`  | `12ABC34501AB77` |

### CNPJs inválidos

| Entrada | Motivo |
|---------|--------|
| `11.222.333/0001-10` | Dígitos verificadores errados (deveria ser 81) |
| `12.ABC.345/01AB-36` | Segundo dígito incorreto |
| `00000000000000` | Sequência uniforme |
| `12.ABC.345/01AB-7` | Tamanho incorreto |
| `123456789012` | Menos de 14 dígitos |

---

## Testes

A classe `NovoCnpjValidatorTest` contém testes parametrizados com **JUnit Jupiter**.

```java
@ParameterizedTest
@CsvSource({
    "00.000.000/0001-91",
    "04.252.011/0001-10",
    "12.ABC.345/01AB-77"
})
void testValidCnpj(String cnpj) {
    assertTrue(validator.isValid(cnpj, null));
}
```

Execute com:

```bash
mvn test
```

---

## Algoritmo de validação

1. **Limpeza**: remove tudo que não for `[A-Za-z0-9]` e converte para maiúsculas.
2. **Tamanho**: deve ter exatamente 14 caracteres.
3. **Sequência uniforme**: rejeita se todos os 14 caracteres forem iguais.
4. **Cálculo do 1º dígito** (posição 13):
   - Base: primeiros 12 caracteres.
   - Pesos: `[5,4,3,2,9,8,7,6,5,4,3,2]`
   - Conversão: `'0'-'9'` → `0-9`; `'A'-'Z'` → `ASCII - 48` (A=17, B=18, ...)
   - Soma produtos → resto % 11. Se resto < 2 → dígito = 0; senão → dígito = 11 - resto.
5. **Cálculo do 2º dígito** (posição 14):
   - Base: primeiros 12 caracteres + 1º dígito.
   - Pesos: `[6,5,4,3,2,9,8,7,6,5,4,3,2]`
   - Mesma regra do resto.
6. **Validação**: compara os dois dígitos calculados com os dois últimos caracteres.

---

## Licença

Código livre. Adapte conforme necessário.
[GNU General Public License](LICENSE)

---

## Referências

- [Instrução Normativa RFB nº 2.229/2024](https://www.in.gov.br/en/web/dou/-/instrucao-normativa-rfb-n-2.229-de-15-de-outubro-de-2024-586258831)
- [Portal da Receita Federal – CNPJ Alfanumérico](https://www.gov.br/receitafederal/pt-br/assuntos/inscricoes/cnpj-alfanumerico)