package graphql.validation.directives.standardrules


import graphql.validation.directives.BaseDirectiveRuleTest
import graphql.validation.directives.DirectiveValidationRule
import spock.lang.Unroll

class AssertTrueFalseRuleTest extends BaseDirectiveRuleTest {


    @Unroll
    def "assert true rule constraints"() {

        DirectiveValidationRule ruleUnderTest = new AssertTrueRule()

        expect:

        def errors = runRules(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                             | argVal | expectedMessage
        'field( arg : Boolean @AssertTrue ) : ID'                    | false  | 'AssertTrue;path=/arg;val:false;\t'
        'field( arg : Boolean @AssertTrue ): ID'                     | true   | ''

        'field( arg : Boolean @AssertTrue(message : "custom")) : ID' | false  | 'custom;path=/arg;val:false;\t'

        // nulls are valid
        'field( arg : Boolean @AssertTrue ) : ID'                    | null   | ''
    }

    @Unroll
    def "assert false rule constraints"() {

        DirectiveValidationRule ruleUnderTest = new AssertFalseRule()

        expect:

        def errors = runRules(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                              | argVal | expectedMessage
        'field( arg : Boolean @AssertFalse ) : ID'                    | true   | 'AssertFalse;path=/arg;val:true;\t'
        'field( arg : Boolean @AssertFalse ): ID'                     | false  | ''

        'field( arg : Boolean @AssertFalse(message : "custom")) : ID' | true   | 'custom;path=/arg;val:true;\t'

        // nulls are valid
        'field( arg : Boolean @AssertFalse ) : ID'                    | null   | ''
    }
}