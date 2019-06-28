package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class RangeConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "range rule constraints"() {

        DirectiveConstraint ruleUnderTest = new RangeConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                                 | argVal                | expectedMessage
        "field( arg : String @Range(max : 10) ) : ID"                    | "100"                 | "Range;path=/arg;val:100;\t"
        "field( arg : String @Range(max : 100) ) : ID"                   | "99"                  | ""
        "field( arg : String @Range(max : 10, min : 5) ) : ID"           | "3"                   | "Range;path=/arg;val:3;\t"

        'field( arg : String @Range(min : 5, message : "custom") ) : ID' | "2"                   | "custom;path=/arg;val:2;\t"
        "field( arg : String @Range(min : 5) ) : ID"                     | null                  | ""

        // can handle other numeric
        "field( arg : String @Range(max : 10) ) : ID"                    | Byte.valueOf("12")    | "Range;path=/arg;val:12;\t"
        "field( arg : String @Range(max : 10) ) : ID"                    | Double.valueOf("12")  | "Range;path=/arg;val:12.0;\t"
        "field( arg : String @Range(max : 10) ) : ID"                    | Long.valueOf("12")    | "Range;path=/arg;val:12;\t"
        "field( arg : String @Range(max : 10) ) : ID"                    | Integer.valueOf("12") | "Range;path=/arg;val:12;\t"
        "field( arg : String @Range(max : 10) ) : ID"                    | Short.valueOf("12")   | "Range;path=/arg;val:12;\t"
    }
}