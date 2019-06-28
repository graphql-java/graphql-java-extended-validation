package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class PatternConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "pattern rule constraints"() {

        DirectiveConstraint ruleUnderTest = new PatternConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                       | argVal | expectedMessage
        'field( arg : String @Pattern(regexp:"[A-Z]*") ) : ID' | "ABCd" | 'Pattern;path=/arg;val:ABCd;\t'
        'field( arg : String @Pattern(regexp:"[A-Z]*") ) : ID' | "ABC"  | ''

        // nulls are valid
        'field( arg : String @Pattern(regexp:"[A-Z]*") ) : ID' | null   | ''
    }
}