# Extended Validation for graphql-java

This library provides extended validation for [graphql-java](https://github.com/graphql-java/graphql-java)


# Status

This code is current under construction.  There is no release and not all parts of it are ready.

But the project welcomes all feedback and input on code design and validation requirements. 

# Using the code

TODO

# SDL @Directive validation

This library a series of directives that can be applied to field arguments and input type fields which will constrain the values

These names and semantics are inspired from the javax.validation annotations

https://javaee.github.io/javaee-spec/javadocs/javax/validation/constraints/package-summary.html

## SDL directives

You can add these onto arguments or input types in your graphql SDL.

For example

```graphql

    #
    # this declares the directive as being possible on arguments and input fields
    #
    directive @Size(min : Int = 0, max : Int = 2147483647, message : String = "graphql.validation.Size.message")
                        on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION

    input Application {
        name : String @Size(min : 3, max : 100)
    }

    type Query {
        hired (applications : [Application!] @Size(max : 10)) : [Boolean]
    }
```

In the example above, we have a `applications` argument that takes at most 10 applications and within each `Application` input object
the `name` field must be at least 3 characters long and no more than 100 characters long to be considered valid. 


## The library supplied directives

### AssertFalse

The boolean value must be false.

- SDL :  `directive @AssertFalse(message : String = "graphql.validation.AssertFalse.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Boolean`

### AssertTrue

The boolean value must be true.

- SDL :  `directive @AssertTrue(message : String = "graphql.validation.AssertTrue.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Boolean`

### DecimalMax

The element must be a number whose value must be less than or equal to the specified maximum.

- SDL :  `directive @DecimalMax(value : String!, inclusive : Boolean! = true, message : String = "graphql.validation.DecimalMax.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### DecimalMin

The element must be a number whose value must be greater than or equal to the specified minimum.

- SDL :  `directive @DecimalMin(value : String!, inclusive : Boolean! = true, message : String = "graphql.validation.DecimalMin.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### Digits

The element must be a number inside the specified `integer` and `fraction` range.

- SDL :  `directive @Digits(integer : Int!, fraction : Int!, message : String = "graphql.validation.Digits.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### Max

The element must be a number whose value must be less than or equal to the specified maximum.

- SDL :  `directive @Max(value : Int! = 2147483647, message : String = "graphql.validation.Max.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### Min

The element must be a number whose value must be greater than or equal to the specified minimum.

- SDL :  `directive @Min(value : Int! = 0, message : String = "graphql.validation.Min.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### Negative

The element must be a negative number.

- SDL :  `directive @Negative(message : String = "graphql.validation.Negative.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### NegativeOrZero

The element must be a negative number or zero.

- SDL :  `directive @NegativeOrZero(message : String = "graphql.validation.NegativeOrZero.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### NotBlank

The String must contain at least one non-whitespace character, according to Java's Character.isWhitespace().

- SDL :  `directive @NotBlank(message : String = "graphql.validation.NotBlank.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `String`

### NotEmpty

The element must have a non zero size.

- SDL :  `directive @NotEmpty(message : String = "graphql.validation.NotEmpty.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `String`, `Lists`, `Input Objects`

### Pattern

The String must match the specified regular expression, which follows the Java regular expression conventions.

- SDL :  `directive @Pattern(pattern : String! =".*", message : String = "graphql.validation.Pattern.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `String`

### Positive

The element must be a positive number.

- SDL :  `directive @Positive(message : String = "graphql.validation.Positive.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### PositiveOrZero

The element must be a positive number or zero.

- SDL :  `directive @PositiveOrZero(message : String = "graphql.validation.PositiveOrZero.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

### Size

The element size must be between the specified `min` and `max` boundaries (inclusive).

- SDL :  `directive @Size(min : Int = 0, max : Int = 2147483647, message : String = "graphql.validation.Size.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`
- Applies to :  `String`, `Lists`, `Input Objects`