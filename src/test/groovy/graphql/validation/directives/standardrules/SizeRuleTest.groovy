package graphql.validation.directives.standardrules


import graphql.validation.directives.BaseDirectiveRuleTest
import graphql.validation.directives.DirectiveValidationRule
import spock.lang.Unroll

class SizeRuleTest extends BaseDirectiveRuleTest {


    @Unroll
    def "simple argument size rule constraints"() {

        DirectiveValidationRule ruleUnderTest = new SizeRule()

        expect:

        def errors = runRules(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                                    | argVal          | expectedMessage
        "field( arg : String @Size(max : 10) ) : ID"                    | "1234567891011" | "Size;path=/testArg;val:1234567891011;\t"
        "field( arg : String @Size(max : 100) ) : ID"                   | "1234567891011" | ""
        "field( arg : String @Size(max : 10, min : 5) ) : ID"           | "123"           | "Size;path=/testArg;val:123;\t"

        'field( arg : String @Size(min : 5, message : "custom") ) : ID' | "123"           | "custom;path=/testArg;val:123;\t"
        "field( arg : String @Size(min : 5) ) : ID"                     | null            | "Size;path=/testArg;val:null;\t"
    }
}