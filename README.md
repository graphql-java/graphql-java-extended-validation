# Extended Validation for graphql-java


[![Build Status](https://github.com/graphql-java/graphql-java-extended-validation/actions/workflows/master.yml/badge.svg)](https://github.com/graphql-java/graphql-java-extended-validation/actions/workflows/master.yml)
[![Latest Release](https://maven-badges.herokuapp.com/maven-central/com.graphql-java/graphql-java-extended-validation/badge.svg?version=19.1&color=blue)](https://maven-badges.herokuapp.com/maven-central/com.graphql-java/graphql-java-extended-validation/)
[![Latest Release - Jakarta EE8](https://img.shields.io/maven-central/v/com.graphql-java/graphql-java-extended-validation?versionSuffix=6.2.0.Final&label=maven-central%20jakarta%20ee8)](https://maven-badges.herokuapp.com/maven-central/com.graphql-java/graphql-java-extended-validation/)
[![Latest Snapshot](https://img.shields.io/maven-central/v/com.graphql-java/graphql-java-extended-validation?label=maven-central%20snapshot)](https://maven-badges.herokuapp.com/maven-central/com.graphql-java/graphql-java-extended-validation/)
[![MIT licensed](https://img.shields.io/badge/license-MIT-green)](https://github.com/graphql-java/graphql-java-extended-validation/blob/master/LICENSE.md)



This library provides extended validation of fields and field arguments for [graphql-java](https://github.com/graphql-java/graphql-java)


# Using


```xml
<dependency>
  <groupId>com.graphql-java</groupId>
  <artifactId>graphql-java-extended-validation</artifactId>
  <version>19.0</version>
</dependency>
```

```groovy
compile 'com.graphql-java:graphql-java-extended-validation:19.0'
```

> Note:
> 
> use 16.0.0 or above for graphql-java 16.x and above
> 
> use 17.0 or above for graphql-java 17.x and above
> 
> use 17.0-hibernate-validator-6.2.0.Final for graphql-java 17.x and SpringBoot 2.x support
>
> use 18.1 or above for graphql-java 18.x and above
>
> use 18.1-hibernate-validator-6.2.0.Final for graphql-java 18.x and SpringBoot 2.x support
>
> use 19.0 or above for graphql-java 19.x and above

It's currently available from Maven central.


# SDL @Directive constraints

This library provides a series of directives that can be applied to field arguments and input type fields which will 
constrain their allowable values.

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

# I18n Locale Support

The validation library aims to offer Internationalisation (18N) of the error messages.  When the validation rules
run they are passed in a `java.util.Locale`. A `ResourceBundleMessageInterpolator` can then be used to build up messages
that come from I18N bundles.

A `Locale` should be created per graphql execution, and can be passed to `ExecutionInput`. More i18n is being added to graphql-java 
and later this library will then be updated to to take advantage of i18n.  

In the meantime you can work around this by having the `context`, `source` or `root` implement `graphql.validation.locale.LocaleProvider` and
the library will extract a `Locale` from that.

# Schema Directive Wiring

If you are using graphql SDL to build your graphql schema then you can use a `ValidationSchemaWiring` class to automatically
change your field data fetchers so that validation rules are called before the field data is fetched.

This allows you to automatically enhance your schema with validation by directives alone.

The following shows how to setup the SDL generation so that the build schema will have validation in place.


```java
        //
        // This contains by default the standard library provided @Directive constraints
        //
        ValidationRules validationRules = ValidationRules.newValidationRules()
                .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
                .build();
        //
        // This will rewrite your data fetchers when rules apply to them so that validation
        ValidationSchemaWiring schemaWiring = new ValidationSchemaWiring(validationRules);
        //
        // we add this schema wiring to the graphql runtime
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring().directiveWiring(schemaWiring).build();
        //
        // then pretty much standard graphql-java code to build a graphql schema
        Reader sdl = buildSDL();
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(sdl);
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
```

Under the covers `ValidationSchemaWiring` asks each possible rule if it applies to a field as they are encountered (at schema build time).

If they do apply then it rewrites the DataFetcher so that it first calls the validation code and produces errors if the field input is not 
considered valid.

The default strategy `OnValidationErrorStrategy.RETURN_NULL` will return null for the field input if it is not considered valid.  You can 
write your own strategy if you want.

## Using the API direct in your own data fetchers

We recommend that you use the SDL schema directive wiring and @directives for the easiest way to get input type validation.

However there can be reasons why you cant use this approach and you have use the API directly in your data fetching code.

```java
 
         //
         // an example of writing your own custom validation rule
         //
         ValidationRule myCustomValidationRule = new ValidationRule() {
             @Override
             public boolean appliesTo(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
                 return fieldDefinition.getName().equals("decide whether this rule applies here");
             }
 
             @Override
             public boolean appliesTo(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
                 return argument.getName().equals("decide whether this rule applies here to an argument");
             }
 
             @Override
             public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
 
                 List<GraphQLError> errors = new ArrayList<>();
                 Map<String, Object> argumentValues = validationEnvironment.getArgumentValues();
                 for (String argName : argumentValues.keySet()) {
                     Object argValue = argumentValues.get(argName);
                     GraphQLError error = runCodeThatValidatesInputHere(validationEnvironment, argName, argValue);
                     if (error != null) {
                         errors.add(error);
                     }
                 }
                 return errors;
             }
         };
 
         DataFetcher dataFetcher = new DataFetcher() {
             @Override
             public Object get(DataFetchingEnvironment env) {
 
                 //
                 // By default the ValidationRule contains the SDL @directive rules, but
                 // you can also add your own as we do here.
                 //
                 ValidationRules validationRules = ValidationRules
                         .newValidationRules()
                         .locale(Locale.getDefault())
                         .addRule(myCustomValidationRule)
                         .build();
 
                 //
                 // The expected strategy is to return null data and the errors if there are any validation
                 // problems
                 //
                 List<GraphQLError> errors = validationRules.runValidationRules(env);
                 if (!errors.isEmpty()) {
                     return DataFetcherResult.newResult().errors(errors).data(null).build();
                 }
                 return normalDataFetchingCodeRunsNow(env);
             }
         };
```

The above code shows a custom validation rule (with nonsense logic for demonstration purposes) and then a data fetcher
that uses the `ValidationRules` API to run validation rules.  


## The supplied @Directive constraints

<!-- generated by DocHelper on 2019-08-17T11:55:22.933Z -->

### @AssertFalse

The boolean value must be false.

- Example : `updateDriver( isDrunk : Boolean @AssertFalse) : DriverDetails`

- Applies to : `Boolean`, `Lists`

- SDL : `directive @AssertFalse(message : String = "graphql.validation.AssertFalse.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.AssertFalse.message`


### @AssertTrue

The boolean value must be true.

- Example : `driveCar( hasLicence : Boolean @AssertTrue) : DriverDetails`

- Applies to : `Boolean`, `Lists`

- SDL : `directive @AssertTrue(message : String = "graphql.validation.AssertTrue.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.AssertTrue.message`


### @DecimalMax

The element must be a number whose value must be less than or equal to the specified maximum.

- Example : `driveCar( bloodAlcoholLevel : Float @DecimalMax(value : "0.05") : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @DecimalMax(value : String!, inclusive : Boolean! = true, message : String = "graphql.validation.DecimalMax.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.DecimalMax.message`


### @DecimalMin

The element must be a number whose value must be greater than or equal to the specified minimum.

- Example : `driveCar( carHorsePower : Float @DecimalMin(value : "300.50") : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @DecimalMin(value : String!, inclusive : Boolean! = true, message : String = "graphql.validation.DecimalMin.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.DecimalMin.message`


### @Digits

The element must be a number inside the specified `integer` and `fraction` range.

- Example : `buyCar( carCost : Float @Digits(integer : 5, fraction : 2) : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

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

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @Max(value : Int! = 2147483647, message : String = "graphql.validation.Max.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Max.message`


### @Min

The element must be a number whose value must be greater than or equal to the specified minimum.

- Example : `driveCar( age : Int @Min(value : 18) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @Min(value : Int! = 0, message : String = "graphql.validation.Min.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Min.message`


### @Negative

The element must be a negative number.

- Example : `driveCar( lostLicencePoints : Int @Negative) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @Negative(message : String = "graphql.validation.Negative.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Negative.message`


### @NegativeOrZero

The element must be a negative number or zero.

- Example : `driveCar( lostLicencePoints : Int @NegativeOrZero) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @NegativeOrZero(message : String = "graphql.validation.NegativeOrZero.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.NegativeOrZero.message`


### @NotBlank

The String must contain at least one non-whitespace character, according to Java's Character.isWhitespace().

- Example : `updateAccident( accidentNotes : String @NotBlank) : DriverDetails`

- Applies to : `String`, `ID`, `Lists`

- SDL : `directive @NotBlank(message : String = "graphql.validation.NotBlank.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.NotBlank.message`


### @NotEmpty

The element must have a non-zero size.

- Example : `updateAccident( accidentNotes : String! @NotEmpty) : DriverDetails`

- Applies to : `String`, `ID`, `Lists`

- SDL : `directive @NotEmpty(message : String = "graphql.validation.NotEmpty.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.NotEmpty.message`

### @ContainerNotEmpty

The list or input object must have a non-zero size.

- Example : `updateAccident( accidentNotes : [Notes]! @ContainerNotEmpty) : DriverDetails`

- Applies to : `Lists`, `Input Objects`

- SDL : `directive @ContainerNotEmpty(message : String = "graphql.validation.ContainerNotEmpty.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.ContainerNotEmpty.message`


### @Pattern

The String must match the specified regular expression, which follows the Java regular expression conventions.

- Example : `updateDriver( licencePlate : String @Pattern(regexp : "[A-Z][A-Z][A-Z]-[0-9][0-9][0-9]") : DriverDetails`

- Applies to : `String`, `ID`, `Lists`

- SDL : `directive @Pattern(regexp : String! =".*", message : String = "graphql.validation.Pattern.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Pattern.message`


### @Positive

The element must be a positive number.

- Example : `driver( licencePoints : Int @Positive) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @Positive(message : String = "graphql.validation.Positive.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Positive.message`


### @PositiveOrZero

The element must be a positive number or zero.

- Example : `driver( licencePoints : Int @PositiveOrZero) : DriverDetails`

- Applies to : `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @PositiveOrZero(message : String = "graphql.validation.PositiveOrZero.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.PositiveOrZero.message`


### @Range

The element range must be between the specified `min` and `max` boundaries (inclusive).  It accepts numbers and strings that represent numerical values.

- Example : `driver( milesTravelled : Int @Range( min : 1000, max : 100000)) : DriverDetails`

- Applies to : `String`, `Byte`, `Short`, `Int`, `Long`, `BigDecimal`, `BigInteger`, `Float`, `Lists`

- SDL : `directive @Range(min : Int = 0, max : Int = 2147483647, message : String = "graphql.validation.Range.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Range.message`


### @Size

The string's size must be between the specified `min` and `max` boundaries (inclusive).

- Example : `updateDrivingNotes( drivingNote : String @Size( min : 1000, max : 100000)) : DriverDetails`

- Applies to : `String`, `ID`, `Lists`

- SDL : `directive @Size(min : Int = 0, max : Int = 2147483647, message : String = "graphql.validation.Size.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.Size.message`

### @ContainerSize

The list's or input object's size must be between the specified `min` and `max` boundaries (inclusive).

- Example : `updateDrivingNotes( drivingNote : [String!]! @ContainerSize( min : 10, max : 20)) : DriverDetails`

- Applies to : `Lists`, `Input Objects`

- SDL : `directive @ContainerSize(min : Int = 0, max : Int = 2147483647, message : String = "graphql.validation.ContainerSize.message") on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION`

- Message : `graphql.validation.ContainerSize.message`

<!-- end -->
