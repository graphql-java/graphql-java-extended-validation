package graphql.validation.directives.standardrules


import graphql.validation.directives.BaseDirectiveRuleTest
import graphql.validation.directives.DirectiveValidationRule
import spock.lang.Unroll

class PatternRuleTest extends BaseDirectiveRuleTest {


    @Unroll
    def "pattern rule constraints"() {

        DirectiveValidationRule ruleUnderTest = new PatternRule()

        expect:

        def errors = runRules(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                       | argVal | expectedMessage
        'field( arg : String @Pattern(regexp:"[A-Z]*") ) : ID' | "ABCd" | 'Pattern;path=/arg;val:ABCd;\t'
        'field( arg : String @Pattern(regexp:"[A-Z]*") ) : ID' | "ABC"  | ''

        // nulls are valid
        'field( arg : String @Pattern(regexp:"[A-Z]*") ) : ID' | null   | ''
    }
}