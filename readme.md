# Extended Validation for graphql-java

This library provides extended validation of fields and field arguments for [graphql-java](https://github.com/graphql-java/graphql-java)


# Status

This code is currently under construction.  There is no release artifact and not all parts of it are ready.

But the project welcomes all feedback and input on code design and validation requirements.

It currently passes MOST of its tests - but not all.  This is a matter of attention and time.

It has NOT yet been consumed by a production like project and hence its API usefulness has not been tested 

# Using the code

TODO


# SDL @Directive constraints

This library a series of directives that can be applied to field arguments and input type fields which will constrain their allowable values.

These names and semantics are inspired from the javax.validation annotations

https://javaee.github.io/javaee-spec/javadocs/javax/validation/constraints/package-summary.html

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

In the example above, we have a `applications` argument that takes at most 10 applications and within each `Application` input object,
the `name` field must be at least 3 characters long and no more than 100 characters long to be considered valid. 

# Java Expression Language (Java EL)

The `@Expression` validation directive allows Java EL to be used to help build validation rules.

Java EL is a powerful expression syntax for expressing validation conditions.

Some simple sample Java EL expressions might be : 

| EL Expression                          | Result   |
| -------------                          | -------- |
| `${1> (4/2)} `                         | `false`  |
| `${4.0>= 3} `                          | `true`   |
| `${100.0 == 100}`                      | `true`   |
| `${(10*10) ne 100} `                   | `false`  |
| `${'a' > 'b'}`                         | `false`  |
| `${'hip' lt 'hit'}`                    | `true`   |
| `${4> 3} `                             | `true`   |
| `${1.2E4 + 1.4}  `                     | `12001.4` |
| `${3 div 4}`                           | `0.75`   |
| `${10 mod 4}`                          | `2`      |
| `${((x, y) → x + y)(3, 5.5)}`          | `8.5`    |
| `[1,2,3,4].stream().sum()`             | `10`     |
| `[1,3,5,2].stream().sorted().toList()` | `[1, 2, 3, 5]` |

The following validation variables are made available to you : 

| Name                  | Value |
| -------------         | ----- |
| `validatedValue`      | The value being validated |
| `gqlField`            | The `GraphQLFieldDefinition` being validated |
| `gqlFieldContainer`   | The `GraphQLFieldsContainer` parent type containing that field |
| `gqlArgument`         | The `GraphQLArgument` being validated.  This can be null for field level validation |
| `arguments`           | The map of all argument values for the current field |
| `args`                | A short hand name for the map of all argument values for the current field | 

The Java EL expression MUST evaluate to a boolean value to be useful in the `@Expresion` directive.

See here for [a more complete overview of Java EL](https://javaee.github.io/tutorial/jsf-el001.html)


# Message Interpolation

The validation code uses a `graphql.validation.interpolation.MessageInterpolator` interface to build out error messages.  A default 
`ResourceBundleMessageInterpolator` class is provided to load error messages from Java resource bundles to allow internationalised messages (I18N)

You can use Java EL syntax in the message templates to format even more powerful error messages.

```
   The field ${gqlField.name} has the following invalid value : ${formatter.format('%1$.2f', validatedValue)}
```

If you use directive arguments like `message : String = "graphql.validation.Size.message"` then the `ResourceBundleMessageInterpolator` class
will use that as a resource bundle lookup key.  This too is inspired by the javax.validation annotations and how they work.

Like javax.validation, this library ships with some default error message templates but you can override them. 


# Complex input types

You can put @Directive constraints on complex input types as well as simple field arguments

```graphql
    input ProductItem {
        code : String @Size(max : 5)
        price : String @Size(max : 3)
    }

    type Mutation {
            updateProduct( product : ID,  items : [ProductItem]) : Product
    }   
```

In the example above each `ProductItem` in the list of items is examined for valid values

## The supplied @Directive constraints

<!-- generated by DocHelper on 2019-08-17T11:55:22.933Z -->

### @AssertFalse

The boolean value must be false.

- Example : `updateDriver( isDrunk : Boolean @AssertFalse) : DriverDetails`

- Applies to : `Boolean`

- SDL : `directive @AssertFalse(message : String = "graphql.validation.AssertFalse.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.AssertFalse.message`


### @AssertTrue

The boolean value must be true.

- Example : `driveCar( hasLicence : Boolean @AssertTrue) : DriverDetails`

- Applies to : `Boolean`

- SDL : `directive @AssertTrue(message : String = "graphql.validation.AssertTrue.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.AssertTrue.message`


### @DecimalMax

The element must be a number whose value must be less than or equal to the specified maximum.

- Example : `driveCar( bloodAlcoholLevel : Float @DecimalMax(value : "0.05") : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @DecimalMax(value : String!, inclusive : Boolean! = true, message : String = "graphql.validation.DecimalMax.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.DecimalMax.message`


### @DecimalMin

The element must be a number whose value must be greater than or equal to the specified minimum.

- Example : `driveCar( carHorsePower : Float @DecimalMin(value : "300.50") : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @DecimalMin(value : String!, inclusive : Boolean! = true, message : String = "graphql.validation.DecimalMin.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.DecimalMin.message`


### @Digits

The element must be a number inside the specified `integer` and `fraction` range.

- Example : `buyCar( carCost : Float @Digits(integer : 5, fraction : 2) : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @Digits(integer : Int!, fraction : Int!, message : String = "graphql.validation.Digits.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Digits.message`


### @Expression

The provided expression must evaluate to true.  The expression language is <a href="https://javaee.github.io/tutorial/jsf-el001.html">Java EL</a> and expressions MUST resolve to a boolean value, ie. it is valid or not.

- Example : `drivers( first : Int, after : String!, last : Int, before : String) 
 : DriverConnection @Expression(value : "${args.containsOneOf('first','last') }"`

- Applies to : `All Types and Scalars`

- SDL : `directive @Expression(value : String!, message : String = "graphql.validation.Expression.message") on FIELD_DEFINITION | ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Expression.message`


### @Max

The element must be a number whose value must be less than or equal to the specified maximum.

- Example : `driveCar( horsePower : Float @Max(value : 1000) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @Max(value : Int! = 2147483647, message : String = "graphql.validation.Max.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Max.message`


### @Min

The element must be a number whose value must be greater than or equal to the specified minimum.

- Example : `driveCar( age : Int @Min(value : 18) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @Min(value : Int! = 0, message : String = "graphql.validation.Min.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Min.message`


### @Negative

The element must be a negative number.

- Example : `driveCar( lostLicencePoints : Int @Negative) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @Negative(message : String = "graphql.validation.Negative.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Negative.message`


### @NegativeOrZero

The element must be a negative number or zero.

- Example : `driveCar( lostLicencePoints : Int @NegativeOrZero) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @NegativeOrZero(message : String = "graphql.validation.NegativeOrZero.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.NegativeOrZero.message`


### @NotBlank

The String must contain at least one non-whitespace character, according to Java's Character.isWhitespace().

- Example : `updateAccident( accidentNotes : String @NotBlank) : DriverDetails`

- Applies to : `String`

- SDL : `directive @NotBlank(message : String = "graphql.validation.NotBlank.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.NotBlank.message`


### @NotEmpty

The element must have a non zero size.

- Example : `updateAccident( accidentNotes : [Notes]! @NotEmpty) : DriverDetails`

- Applies to : `String`, `Lists`, `Input Objects`

- SDL : `directive @NotEmpty(message : String = "graphql.validation.NotEmpty.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.NotEmpty.message`


### @Pattern

The String must match the specified regular expression, which follows the Java regular expression conventions.

- Example : `updateDriver( licencePlate : String @Patttern(regex : "[A-Z][A-Z][A-Z]-[0-9][0-9][0-9]") : DriverDetails`

- Applies to : `String`

- SDL : `directive @Pattern(regexp : String! =".*", message : String = "graphql.validation.Pattern.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Pattern.message`


### @Positive

The element must be a positive number.

- Example : `driver( licencePoints : Int @Positive) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @Positive(message : String = "graphql.validation.Positive.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Positive.message`


### @PositiveOrZero

The element must be a positive number or zero.

- Example : `driver( licencePoints : Int @PositiveOrZero) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @PositiveOrZero(message : String = "graphql.validation.PositiveOrZero.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.PositiveOrZero.message`


### @Range

The element range must be between the specified `min` and `max` boundaries (inclusive).  It accepts numbers and strings that represent numerical values.

- Example : `driver( milesTravelled : Int @Range( min : 1000, max : 100000)) : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`

- SDL : `directive @Range(min : Int = 0, max : Int = 2147483647, message : String = "graphql.validation.Range.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Range.message`


### @Size

The element size must be between the specified `min` and `max` boundaries (inclusive).

- Example : `updateDrivingNotes( drivingNote : String @Size( min : 1000, max : 100000)) : DriverDetails`

- Applies to : `String`, `Lists`, `Input Objects`

- SDL : `directive @Size(min : Int = 0, max : Int = 2147483647, message : String = "graphql.validation.Size.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Size.message`

<!-- end -->
