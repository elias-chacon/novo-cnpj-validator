# Novo CNPJ Validator

[![CI](https://github.com/elias-chacon/novo-cnpj-validator/actions/workflows/ci.yml/badge.svg)](https://github.com/elias-chacon/novo-cnpj-validator/actions/workflows/ci.yml)
[![Version](https://img.shields.io/github/v/release/elias-chacon/novo-cnpj-validator?label=Version&sort=semver&style=flat-square)](https://github.com/elias-chacon/novo-cnpj-validator/releases/latest)
[![Open issues](https://img.shields.io/github/issues/elias-chacon/novo-cnpj-validator?label=Open%20issues&style=flat-square)](https://github.com/elias-chacon/novo-cnpj-validator/issues)
[![Last update](https://img.shields.io/github/last-commit/elias-chacon/novo-cnpj-validator?label=Updated&style=flat-square)](https://github.com/elias-chacon/novo-cnpj-validator/commits/main)
[![License](https://img.shields.io/badge/license-GPL-blue.svg)](LICENSE)
[![codecov](https://codecov.io/gh/elias-chacon/novo-cnpj-validator/graph/badge.svg?token=h5HdPC7cEk)](https://codecov.io/gh/elias-chacon/novo-cnpj-validator)

**[Português](README.pt-BR.md)** | English

A Jakarta Validation annotation for validating Brazilian CNPJ identifiers in both the traditional numeric format and the new alphanumeric format defined by **IN RFB No. 2.229/2024**.

## About the alphanumeric CNPJ

The Brazilian Federal Revenue Service introduced alphanumeric CNPJ registrations to expand business registration capacity. Starting in **July 2026**, the first 12 positions may contain uppercase letters (`A-Z`) and digits (`0-9`). The final two check digits remain numeric.

- Existing numeric CNPJs are unchanged.
- Format: `XX.XXX.XXX/XXXX-DD` (14 alphanumeric characters; punctuation is optional).
- Validation algorithm: modulo 11, with letters converted to ASCII value minus 48 (`A=17`, `B=18`, ..., `Z=42`).

## Features

- Accepts masked or unmasked strings.
- Normalizes lowercase letters to uppercase.
- Rejects uniform sequences such as `00000000000000` and `AAAAAAAAAAAAAA`.
- Supports legacy numeric CNPJs and new alphanumeric CNPJs.
- Works with **jakarta.validation** implementations such as Hibernate Validator and Spring Validation.

## Installation

### GitHub Packages (recommended)

Add the GitHub Packages repository and dependency to `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/elias-chacon/novo-cnpj-validator</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.eliaschacon</groupId>
        <artifactId>novo-cnpj-validator</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

Replace `1.0.1` with the version you want to use.

### Maven dependencies

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

## Usage

### 1. Annotate a DTO field

```java
import jakarta.validation.constraints.NotNull;
import io.github.eliaschacon.validator.NovoCnpj;

public class CompanyDTO {

    @NotNull(message = "CNPJ is required")
    @NovoCnpj
    private String cnpj;

    // getters and setters
}
```

### 2. Run validation

```java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();

Set<ConstraintViolation<CompanyDTO>> violations = validator.validate(dto);
if (!violations.isEmpty()) {
    // handle validation errors
}
```

## Examples

### Valid legacy numeric CNPJs

| Input with mask | Normalized |
|-----------------|------------|
| `00.000.000/0001-91` | `00000000000191` |
| `04.252.011/0001-10` | `04252011000110` |

### Valid alphanumeric CNPJs

| Input with mask | Normalized |
|-----------------|------------|
| `12.ABC.345/01AB-77` | `12ABC34501AB77` |
| `12.abc.345/01ab-77` | `12ABC34501AB77` |

### Invalid CNPJs

| Input | Reason |
|-------|--------|
| `11.222.333/0001-10` | Incorrect check digits |
| `12.ABC.345/01AB-36` | Incorrect second check digit |
| `00000000000000` | Uniform sequence |
| `12.ABC.345/01AB-7` | Incorrect length |
| `123456789012` | Less than 14 characters |

## Tests

The test suite uses JUnit Jupiter parameterized tests.

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

Run:

```bash
mvn test
```

## Validation algorithm

1. **Cleaning:** remove everything except `[A-Za-z0-9]` and convert to uppercase.
2. **Length:** require exactly 14 characters.
3. **Uniform sequence:** reject when all 14 characters are identical.
4. **First check digit:**
   - Base: first 12 characters.
   - Weights: `[5,4,3,2,9,8,7,6,5,4,3,2]`.
   - Conversion: digits `0-9` become `0-9`; letters `A-Z` become `ASCII - 48`.
   - Sum products and calculate `sum % 11`; remainders below 2 produce digit 0, otherwise `11 - remainder`.
5. **Second check digit:**
   - Base: first 12 characters plus the first check digit.
   - Weights: `[6,5,4,3,2,9,8,7,6,5,4,3,2]`.
   - Apply the same remainder rule.
6. **Comparison:** compare both calculated check digits with the final two characters.

## Contributing

Before submitting a change:

```bash
mvn clean verify
```

Open issues and contributions are tracked in [GitHub Issues](https://github.com/elias-chacon/novo-cnpj-validator/issues). There are currently no open issues.

## License

Released under the [GNU General Public License](LICENSE).

## References

- [Normative Instruction RFB No. 2.229/2024](https://www.in.gov.br/en/web/dou/-/instrucao-normativa-rfb-n-2.229-de-15-de-outubro-de-2024-586258831)
- [Brazilian Federal Revenue Service - Alphanumeric CNPJ](https://www.gov.br/receitafederal/pt-br/assuntos/inscricoes/cnpj-alfanumerico)
