package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class RelayIdConstraintTest extends BaseConstraintTestSupport {

    @Unroll
    def "relay id rule constraints"() {

        DirectiveConstraint ruleUnderTest = new RelayIdConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                           | argVal            | expectedMessage
        "field( arg : ID @RelayID ) : ID"                          | ""                | "RelayID;path=/arg;val:;\t"
        "field( arg : ID @RelayID ) : ID"                          | "1234567891011"   | "RelayID;path=/arg;val:1234567891011;\t"
        "field( arg : ID! @RelayID ) : ID"                         | "1234567891011"   | "RelayID;path=/arg;val:1234567891011;\t"
        "field( arg : ID! @RelayID ) : ID"                         | "UGVyc29uOjE="    | ""
        "field( arg : ID! @RelayID( types:[\"Person\"] ) ) : ID"   | "UGVyc29uOjE="    | ""
        "field( arg : ID! @RelayID( types:[\"Ewok\"] ) ) : ID"     | "UGVyc29uOjE="    | "RelayID;path=/arg;val:UGVyc29uOjE=;\t"
        "field( arg : [ID!] @RelayID ) : ID"                       | ["1234567891011"] | "RelayID;path=/arg;val:[1234567891011];\t"
        "field( arg : [ID!] @RelayID( types:[\"Person\"] ) ) : ID" | ["UGVyc29uOjE="]  | ""
        "field( arg : [ID!] @RelayID( types:[\"Ewok\"] ) ) : ID"   | ["UGVyc29uOjE="]  | "RelayID;path=/arg;val:[UGVyc29uOjE=];\t"
    }
}