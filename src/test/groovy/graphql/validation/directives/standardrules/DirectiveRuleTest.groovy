package graphql.validation.directives.standardrules

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import graphql.validation.TestUtil
import graphql.validation.directives.DirectiveValidationRule
import graphql.validation.interpolation.MessageInterpolator
import graphql.validation.rules.ValidationRuleEnvironment
import spock.lang.Specification

class DirectiveRuleTest extends Specification {

    MessageInterpolator interpolator = new MessageInterpolator() {

        @Override
        GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams, ValidationRuleEnvironment ruleEnvironment) {
            def s = messageTemplate
            s += ":"
            messageParams.forEach({ k, v -> s += k + "=" + v + ";" })
            return GraphqlErrorBuilder.newError().message(s).build()
        }
    }


    ValidationRuleEnvironment buildEnv(GraphQLSchema schema, String argName, argValue) {
        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")
        GraphQLArgument argUnderTest = fieldDefinition.getArgument(argName)


        def ruleEnvironment = ValidationRuleEnvironment.newValidationRuleEnvironment()
                .argument(argUnderTest)
                .argumentValue(argValue)
                .fieldDefinition(fieldDefinition)
                .fieldsContainer(fieldsContainer)
                .messageInterpolator(interpolator)
                .build()
        ruleEnvironment
    }

    GraphQLSchema buildSchema(DirectiveValidationRule rule, String fieldDeclaration) {
        def sdl = """

            ${rule.directiveDeclarationSDL}

            type Query {
                ${fieldDeclaration}
            }
        """
        TestUtil.schema(sdl)
    }

}