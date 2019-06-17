package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTest
import graphql.validation.constraints.DirectiveConstraint
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Unroll

class ArgumentsConstraintTest extends BaseConstraintTest {

    static relayCheck = '@Arguments(expression : "${containsOneOf([args.first, args.last])}")'

    static relayCheck2 = '''@Arguments(expression : "${args.containsOneOf('first','last')}")'''


    @Unroll
    def "arguments constraints"() {

        DirectiveConstraint ruleUnderTest = new ArgumentsConstraint()


        expect:

        def schema = buildSchema(ruleUnderTest.getDocumentation().getDirectiveSDL(), fieldDeclaration, "")

        ValidationEnvironment validationEnvironment = buildEnv(ruleUnderTest.name, schema, args)

        def errors = ruleUnderTest.runValidation(validationEnvironment)

        assertErrors(errors, expectedMessage)

        where:



        fieldDeclaration                                     | args        | expectedMessage
        'field( first : Int, last : Int) : ID ' + relayCheck | [first: 10] | ""
    }
}