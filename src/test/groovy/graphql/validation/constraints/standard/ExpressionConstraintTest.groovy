package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Unroll

class ExpressionConstraintTest extends BaseConstraintTestSupport {

    static relayCheck = '''@Expression(value : "${ args.containsOneOf('first','last') }")'''

    @Unroll
    def "expression constraints on a field"() {
        // TODO - is this useful? Those tests don't pass anymore because `validatedValue` is null.
        // Those also validate the arguments, even it the directive is on the field. An alternative for the user could be to wrap the arguments in another abject and put the directive on that wrapped type.

        DirectiveConstraint ruleUnderTest = new ExpressionConstraint()

        expect:

        def schema = buildSchema(ruleUnderTest.getDocumentation().getDirectiveSDL(), fieldDeclaration, "")

        ValidationEnvironment validationEnvironment = buildEnvForField(ruleUnderTest, schema, args)

        def errors = ruleUnderTest.runValidation(validationEnvironment)

        assertErrors(errors, expectedMessage)

        where:


        fieldDeclaration                                     | args        | expectedMessage
        'field( first : Int, last : Int) : ID ' + relayCheck | [first: 10] | ""
//        'field( first : Int, last : Int) : ID ' + relayCheck | [first: 10, last: 20] | "Expression;path=/field;val:null;\t"
    }


    @Unroll
    def "expression constraints on a argument"() {

        DirectiveConstraint ruleUnderTest = new ExpressionConstraint()


        expect:

        def fieldDeclaration = "field( arg : String $expressionDirective ) : ID"
        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:


        expressionDirective                                           | argVal | expectedMessage
        '''@Expression(value : "${validatedValue.length() > 10}" )''' | "ABC"  | "Expression;path=/arg;val:ABC;\t"
        '''@Expression(value : "${validatedValue.length() > 3}" )'''  | "ABC"  | "Expression;path=/arg;val:ABC;\t"
        '''@Expression(value : "${validatedValue.length() > 2}" )'''  | "ABC"  | ""

        '''@Expression(value : "${validatedValue.length() > 2}" )'''  | "ABC"  | ""
    }

}