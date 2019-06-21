package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTest
import graphql.validation.constraints.DirectiveConstraint
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Unroll

class ExpressionConstraintTest extends BaseConstraintTest {

    static relayCheck = '''@Expression(value : "${ args.containsOneOf('first','last') }")'''

    @Unroll
    def "expression constraints on a field"() {

        DirectiveConstraint ruleUnderTest = new ExpressionConstraint()


        expect:

        def schema = buildSchema(ruleUnderTest.getDocumentation().getDirectiveSDL(), fieldDeclaration, "")

        ValidationEnvironment validationEnvironment = buildEnvForField(ruleUnderTest.name, schema, args)

        def errors = ruleUnderTest.runValidation(validationEnvironment)

        assertErrors(errors, expectedMessage)

        where:


        fieldDeclaration                                     | args                  | expectedMessage
        'field( first : Int, last : Int) : ID ' + relayCheck | [first: 10]           | ""
        'field( first : Int, last : Int) : ID ' + relayCheck | [first: 10, last: 20] | "Expression;path=/field;val:null;\t"
    }


    @Unroll
    def "expression constraints on a argument"() {

        DirectiveConstraint ruleUnderTest = new ExpressionConstraint()


        expect:

        def fieldDeclaration = "field( arg : String $expression ) : ID"
        def schema = buildSchema(ruleUnderTest.getDocumentation().getDirectiveSDL(), fieldDeclaration, "")

        ValidationEnvironment validationEnvironment = buildEnv(ruleUnderTest.name, schema, "arg", argVal)

        def errors = ruleUnderTest.runValidation(validationEnvironment)

        assertErrors(errors, expectedMessage)

        where:


        expression                                                    | argVal | expectedMessage
        '''@Expression(value : "${validatedValue.length() > 10}" )''' | "ABC"  | "Expression;path=/arg;val:ABC;\t"
        '''@Expression(value : "${validatedValue.length() > 3}" )'''  | "ABC"  | "Expression;path=/arg;val:ABC;\t"
        '''@Expression(value : "${validatedValue.length() > 2}" )'''  | "ABC"  | ""

        '''@Expression(value : "${validatedValue.length() > 2}" )'''  | "ABC"  | ""
    }

}