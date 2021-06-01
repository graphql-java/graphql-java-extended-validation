package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class SizeConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "size rule constraints"() {

        DirectiveConstraint ruleUnderTest = new SizeConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                                | argVal          | expectedMessage
        // strings
        "field( arg : String @Size(max : 10) ) : ID"                    | "1234567891011" | "Size;path=/arg;val:1234567891011;\t"
        "field( arg : String @Size(max : 100) ) : ID"                   | "1234567891011" | ""
        "field( arg : String @Size(max : 10, min : 5) ) : ID"           | "123"           | "Size;path=/arg;val:123;\t"

        'field( arg : String @Size(min : 5, message : "custom") ) : ID' | "123"           | "custom;path=/arg;val:123;\t"
        "field( arg : String @Size(min : 5) ) : ID"                     | null            | ""

        //IDs
        "field( arg : ID @Size(max : 10) ) : ID"                    | "1234567891011" | "Size;path=/arg;val:1234567891011;\t"
        "field( arg : ID @Size(max : 100) ) : ID"                   | "1234567891011" | ""
        "field( arg : ID @Size(max : 10, min : 5) ) : ID"           | "123"           | "Size;path=/arg;val:123;\t"

        'field( arg : ID @Size(min : 5, message : "custom") ) : ID' | "123"           | "custom;path=/arg;val:123;\t"
        "field( arg : ID @Size(min : 5) ) : ID"                     | null            | ""
    }
}