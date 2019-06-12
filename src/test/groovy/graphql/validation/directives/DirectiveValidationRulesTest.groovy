package graphql.validation.directives

import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.validation.TestUtil
import graphql.validation.rules.ValidationRuleEnvironment
import spock.lang.Specification

class DirectiveValidationRulesTest extends Specification {

    def "basic building"() {
        when:
        def rules = DirectiveValidationRules.newDirectiveValidationRules().build()

        then:
        rules.getDirectiveRules().size() == 4
    }

    def "basic size testing"() {
        def field = '''
                field( argUnderTest : String) : String
        '''

        def sdl = """
            type Query {
                ${field}
            }
        """

        def schema = TestUtil.schema(sdl)

        GraphQLFieldsContainer containerType = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = containerType.getFieldDefinition("field")
        GraphQLArgument argUnderTest = fieldDefinition.getArgument("argUnderTest")


        when:
        def rules = DirectiveValidationRules.newDirectiveValidationRules().build()

        def ruleEnvironment = ValidationRuleEnvironment.newValidationRuleEnvironment()
                .argument(argUnderTest)
                .argumentValue("argumentValue")
                .build()


        then:
        rules.appliesToArgument(argUnderTest, fieldDefinition, containerType)
        rules.runValidation(ruleEnvironment).size() == 0


    }
}
