package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class NegativePositiveConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "positive rule constraints"() {

        DirectiveConstraint ruleUnderTest = new PositiveConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                        | argVal   | expectedMessage
        "field( arg : Int @Positive) : ID"                      | -50      | "Positive;path=/arg;val:-50;\t"
        "field( arg : Int @Positive) : ID"                      | 50       | ""

        'field( arg : Int @Positive(message : "custom") ) : ID' | -50      | "custom;path=/arg;val:-50;\t"

        // edge case
        "field( arg : Int @Positive) : ID"                      | 0        | "Positive;path=/arg;val:0;\t"
        "field( arg : Int @Positive) : ID"                      | 1        | ""

        // nulls are valid
        'field( arg : Int  @Positive ) : ID'                    | null     | ''

        // Lists
        'field( arg : [Int] @Positive ) : ID'                   | [50, 0]  | 'Positive;path=/arg[1];val:0;\t'
        'field( arg : [Int] @Positive ) : ID'                   | [50, 51] | ''
        'field( arg : [Int] @Positive ) : ID'                   | [null]   | ''
    }

    @Unroll
    def "positiveOrZero rule constraints"() {

        DirectiveConstraint ruleUnderTest = new PositiveOrZeroConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                              | argVal   | expectedMessage
        "field( arg : Int @PositiveOrZero) : ID"                      | -50      | "PositiveOrZero;path=/arg;val:-50;\t"
        "field( arg : Int @PositiveOrZero) : ID"                      | 50       | ""

        'field( arg : Int @PositiveOrZero(message : "custom") ) : ID' | -50      | "custom;path=/arg;val:-50;\t"

        // edge case
        "field( arg : Int @PositiveOrZero) : ID"                      | -1       | "PositiveOrZero;path=/arg;val:-1;\t"
        "field( arg : Int @PositiveOrZero) : ID"                      | 0        | ""
        "field( arg : Int @PositiveOrZero) : ID"                      | 1        | ""

        // nulls are valid
        'field( arg : Int  @PositiveOrZero ) : ID'                    | null     | ''

        // Lists
        'field( arg : [Int] @PositiveOrZero ) : ID'                   | [50, -1] | 'PositiveOrZero;path=/arg[1];val:-1;\t'
        'field( arg : [Int] @PositiveOrZero ) : ID'                   | [50, 0]  | ''
        'field( arg : [Int] @PositiveOrZero ) : ID'                   | [null]   | ''
    }

    @Unroll
    def "negative rule constraints"() {

        DirectiveConstraint ruleUnderTest = new NegativeConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                        | argVal    | expectedMessage
        "field( arg : Int @Negative) : ID"                      | 50        | "Negative;path=/arg;val:50;\t"
        "field( arg : Int @Negative) : ID"                      | -50       | ""

        'field( arg : Int @Negative(message : "custom") ) : ID' | 50        | "custom;path=/arg;val:50;\t"

        // edge case
        "field( arg : Int @Negative) : ID"                      | 0         | "Negative;path=/arg;val:0;\t"
        "field( arg : Int @Negative) : ID"                      | -1        | ""

        // nulls are valid
        'field( arg : Int  @Negative ) : ID'                    | null      | ''

        // Lists
        'field( arg : [Int] @Negative ) : ID'                   | [0, -1]   | 'Negative;path=/arg[0];val:0;\t'
        'field( arg : [Int] @Negative ) : ID'                   | [-50, -1] | ''
        'field( arg : [Int] @Negative ) : ID'                   | [null]    | ''
    }

    @Unroll
    def "negative or zero rule constraints"() {

        DirectiveConstraint ruleUnderTest = new NegativeOrZeroConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                              | argVal  | expectedMessage
        'field( arg : Int @NegativeOrZero) : ID'                      | 50      | 'NegativeOrZero;path=/arg;val:50;\t'
        "field( arg : Int @NegativeOrZero) : ID"                      | -50     | ''

        'field( arg : Int @NegativeOrZero(message : "custom") ) : ID' | 50      | 'custom;path=/arg;val:50;\t'

        // edge case
        'field( arg : Int @NegativeOrZero) : ID'                      | 1       | 'NegativeOrZero;path=/arg;val:1;\t'
        'field( arg : Int @NegativeOrZero) : ID'                      | 0       | ''
        'field( arg : Int @NegativeOrZero) : ID'                      | -1      | ''

        // nulls are valid
        'field( arg : Int  @NegativeOrZero ) : ID'                    | null    | ''

        // Lists
        'field( arg : [Int] @NegativeOrZero ) : ID'                   | [1, -1] | 'NegativeOrZero;path=/arg[0];val:1;\t'
        'field( arg : [Int] @NegativeOrZero ) : ID'                   | [0, -1] | ''
        'field( arg : [Int] @NegativeOrZero ) : ID'                   | [null]  | ''
    }
}