# Novo CNPJ Validator

[![CI](https://github.com/elias-chacon/novo-cnpj-validator/actions/workflows/ci.yml/badge.svg)](https://github.com/elias-chacon/novo-cnpj-validator/actions/workflows/ci.yml)
[![Versão](https://img.shields.io/github/v/release/elias-chacon/novo-cnpj-validator?label=Versão&sort=semver&style=flat-square)](https://github.com/elias-chacon/novo-cnpj-validator/releases/latest)
[![Issues abertas](https://img.shields.io/github/issues/elias-chacon/novo-cnpj-validator?label=Issues%20abertas&style=flat-square)](https://github.com/elias-chacon/novo-cnpj-validator/issues)
[![Última atualização](https://img.shields.io/github/last-commit/elias-chacon/novo-cnpj-validator?label=Atualizado&style=flat-square)](https://github.com/elias-chacon/novo-cnpj-validator/commits/main)
[![Licença](https://img.shields.io/badge/licença-GPL-blue.svg)](LICENSE)
[![codecov](https://codecov.io/gh/elias-chacon/novo-cnpj-validator/graph/badge.svg?token=h5HdPC7cEk)](https://codecov.io/gh/elias-chacon/novo-cnpj-validator)

**Português** | [English](README.md)

Uma anotação **Jakarta Validation** para validar CNPJs brasileiros nos formatos numérico tradicional e alfanumérico introduzido pela **IN RFB nº 2.229/2024**.

## Sobre o CNPJ alfanumérico

A Receita Federal passou a admitir CNPJs alfanuméricos para ampliar a capacidade de registro de empresas. A partir de **julho de 2026**, as 12 primeiras posições poderão conter letras maiúsculas (`A-Z`) e números (`0-9`). Os dois últimos dígitos continuam sendo numéricos.

- CNPJs numéricos existentes não serão alterados.
- Formato: `XX.XXX.XXX/XXXX-DD` (14 caracteres alfanuméricos; a máscara é opcional).
- Algoritmo: módulo 11, com letras convertidas para o valor ASCII menos 48 (`A=17`, `B=18`, ..., `Z=42`).

## Funcionalidades

- Aceita strings com ou sem máscara.
- Normaliza letras minúsculas para maiúsculas.
- Rejeita sequências uniformes como `00000000000000` e `AAAAAAAAAAAAAA`.
- Compatível com CNPJs numéricos legados e CNPJs alfanuméricos novos.
- Funciona com implementações **jakarta.validation**, como Hibernate Validator e Spring Validation.

## Instalação

### GitHub Packages (recomendado)

Adicione o repositório e a dependência ao `pom.xml`:

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

Substitua `1.0.1` pela versão desejada.

### Dependências Maven

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

## Como usar

### 1. Anote um campo do DTO

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

### 2. Execute a validação

```java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();

Set<ConstraintViolation<EmpresaDTO>> violations = validator.validate(dto);
if (!violations.isEmpty()) {
    // lide com os erros de validação
}
```

## Exemplos

### CNPJs numéricos válidos

| Entrada com máscara | Normalizado |
|---------------------|-------------|
| `00.000.000/0001-91` | `00000000000191` |
| `04.252.011/0001-10` | `04252011000110` |

### CNPJs alfanuméricos válidos

| Entrada com máscara | Normalizado |
|---------------------|-------------|
| `12.ABC.345/01AB-77` | `12ABC34501AB77` |
| `12.abc.345/01ab-77` | `12ABC34501AB77` |

### CNPJs inválidos

| Entrada | Motivo |
|---------|--------|
| `11.222.333/0001-10` | Dígitos verificadores incorretos |
| `12.ABC.345/01AB-36` | Segundo dígito verificador incorreto |
| `00000000000000` | Sequência uniforme |
| `12.ABC.345/01AB-7` | Tamanho incorreto |
| `123456789012` | Menos de 14 caracteres |

## Testes

A suíte utiliza testes parametrizados com JUnit Jupiter.

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

Execute:

```bash
mvn test
```

## Algoritmo de validação

1. **Limpeza:** remova tudo que não pertença a `[A-Za-z0-9]` e converta para maiúsculas.
2. **Tamanho:** exija exatamente 14 caracteres.
3. **Sequência uniforme:** rejeite quando todos os 14 caracteres forem iguais.
4. **Primeiro dígito:**
   - Base: primeiros 12 caracteres.
   - Pesos: `[5,4,3,2,9,8,7,6,5,4,3,2]`.
   - Conversão: dígitos `0-9` tornam-se `0-9`; letras `A-Z` tornam-se `ASCII - 48`.
   - Some os produtos e calcule `soma % 11`; resto menor que 2 gera dígito 0, caso contrário `11 - resto`.
5. **Segundo dígito:**
   - Base: primeiros 12 caracteres mais o primeiro dígito.
   - Pesos: `[6,5,4,3,2,9,8,7,6,5,4,3,2]`.
   - Aplique a mesma regra do resto.
6. **Comparação:** compare os dois dígitos calculados com os dois últimos caracteres.

## Contribuindo

Antes de enviar uma alteração:

```bash
mvn clean verify
```

Issues abertas e contribuições são acompanhadas no [GitHub Issues](https://github.com/elias-chacon/novo-cnpj-validator/issues). Atualmente não há issues abertas.

## Licença

Distribuído sob a [GNU General Public License](LICENSE).

## Referências

- [Instrução Normativa RFB nº 2.229/2024](https://www.in.gov.br/en/web/dou/-/instrucao-normativa-rfb-n-2.229-de-15-de-outubro-de-2024-586258831)
- [Receita Federal – CNPJ Alfanumérico](https://www.gov.br/receitafederal/pt-br/assuntos/inscricoes/cnpj-alfanumerico)
