package graphql.validation.directives

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.validation.TestUtil
import graphql.validation.interpolation.MessageInterpolator
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

        def rules = DirectiveValidationRules.newDirectiveValidationRules().build()

        def field = '''
                field( argUnderTest : String @Size(max : 10) ) : String
        '''

        def sdl = """

            ${rules.directivesDeclarationSDL}

            type Query {
                ${field}
            }
        """

        def schema = TestUtil.schema(sdl)

        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")
        GraphQLArgument argUnderTest = fieldDefinition.getArgument("argUnderTest")

        MessageInterpolator interpolator = new MessageInterpolator() {

            @Override
            GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams, ValidationRuleEnvironment ruleEnvironment) {
                def s = messageTemplate
                s += ":"
                messageParams.forEach({ k, v -> s += k + "=" + v + ";" })
                return GraphqlErrorBuilder.newError().message(s).build()
            }
        }

        when:

        def ruleEnvironment = ValidationRuleEnvironment.newValidationRuleEnvironment()
                .argument(argUnderTest)
                .argumentValue("1234567891011")
                .fieldDefinition(fieldDefinition)
                .fieldsContainer(fieldsContainer)
                .messageInterpolator(interpolator)
                .build()


        then:
        rules.appliesToArgument(argUnderTest, fieldDefinition, fieldsContainer)
        rules.runValidation(ruleEnvironment).size() == 0


    }
}
