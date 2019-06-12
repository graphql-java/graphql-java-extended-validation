package graphql.validation.directives.standardrules


import graphql.validation.rules.ValidationRuleEnvironment
import spock.lang.Unroll

class SizeRuleTest extends DirectiveRuleTest {


    @Unroll
    def "size rule constraints"() {

        def rule = new SizeRule()

        expect:

        def schema = buildSchema(rule, fieldDeclaration)

        ValidationRuleEnvironment ruleEnvironment = buildEnv(schema, "testArg", argVal)

        assert rule.appliesToArgument(ruleEnvironment.getArgument(), ruleEnvironment.getFieldDefinition(), ruleEnvironment.getFieldsContainer())

        def errors = rule.runValidation(ruleEnvironment)
        errors.size() == expectedSize
        if (expectedSize > 0) {
            def message = errors[0].message
            assert message == expectedMessage, "expecting '" + expectedMessage + "' but got '" + message + "'"
        }

        where:

        fieldDeclaration                                                        | argVal          | expectedSize | expectedMessage
        "field( testArg : String @Size(max : 10) ) : String"                    | "1234567891011" | 1            | "graphql.validation.Size.message:min=0;max=10;size=13;argumentValue=1234567891011;"
        "field( testArg : String @Size(max : 100) ) : String"                   | "1234567891011" | 0            | ""
        "field( testArg : String @Size(max : 10, min : 5) ) : String"           | "123"           | 1            | "graphql.validation.Size.message:min=5;max=10;size=3;argumentValue=123;"

        'field( testArg : String @Size(min : 5, message : "custom") ) : String' | "123"           | 1            | "custom:min=5;max=2147483647;size=3;argumentValue=123;"
        "field( testArg : String @Size(min : 5) ) : String"                     | null            | 1            | "graphql.validation.Size.message:min=5;max=2147483647;size=0;argumentValue=null;"
    }
}