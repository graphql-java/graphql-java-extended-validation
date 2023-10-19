package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class DigitsConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "digit rule constraints"() {

        DirectiveConstraint ruleUnderTest = new DigitsConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                                    | argVal                         | expectedMessage
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | null                           | ''
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | Byte.valueOf("0")              | ''
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | Double.valueOf("500.2")        | ''
        'field( arg : String @Digits(integer : 5) ) : ID'                   | Double.valueOf("500.2345678")  | ''

        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | new BigDecimal("-12345.12")    | ''
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | new BigDecimal("-123456.12")   | 'Digits;path=/arg;val:-123456.12;\t'
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | new BigDecimal("-123456.123")  | 'Digits;path=/arg;val:-123456.123;\t'
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | new BigDecimal("-12345.123")   | 'Digits;path=/arg;val:-12345.123;\t'
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | new BigDecimal("12345.123")    | 'Digits;path=/arg;val:12345.123;\t'

        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | Float.valueOf("-000000000.22") | ''

        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | Integer.valueOf("256874")      | 'Digits;path=/arg;val:256874;\t'
        'field( arg : String @Digits(integer : 5, fraction : 2) ) : ID'     | Double.valueOf("12.0001")      | 'Digits;path=/arg;val:12.0001;\t'

        // zero length
        'field( arg : String @Digits(integer : 0, fraction : 0) ) : ID'     | null                           | ''
        'field( arg : String @Digits(integer : 0, fraction : 0) ) : ID'     | Byte.valueOf("0")              | 'Digits;path=/arg;val:0;\t'
        'field( arg : String @Digits(integer : 0, fraction : 0) ) : ID'     | Double.valueOf("0")            | 'Digits;path=/arg;val:0.0;\t'
        'field( arg : String @Digits(integer : 0, fraction : 0) ) : ID'     | new BigDecimal(0)              | 'Digits;path=/arg;val:0;\t'
        'field( arg : String @Digits(integer : 0, fraction : 0) ) : ID'     | 0                              | 'Digits;path=/arg;val:0;\t'
        'field( arg : String @Digits(integer : 0, fraction : 0) ) : ID'     | 0L                             | 'Digits;path=/arg;val:0;\t'

        // zeroes are trimmed
        'field( arg : String @Digits(integer : 12, fraction : 3) ) : ID'    | 0.001d                         | ''
        'field( arg : String @Digits(integer : 12, fraction : 3) ) : ID'    | 0.00100d                       | ''
        'field( arg : String @Digits(integer : 12, fraction : 3) ) : ID'    | 0.0001d                        | 'Digits;path=/arg;val:1.0E-4;\t'


        // Lists
        'field( arg : [String] @Digits( integer : 5, fraction : 2 ) ) : ID' | ["500.2", "343.2343"]          | 'Digits;path=/arg[1];val:343.2343;\t'
        'field( arg : [String] @Digits( integer : 5, fraction : 2 ) ) : ID' | ["500.2", "343.2"]             | ''
        'field( arg : [String] @Digits( integer : 5, fraction : 2 ) ) : ID' | [null]                | ''
    }
}