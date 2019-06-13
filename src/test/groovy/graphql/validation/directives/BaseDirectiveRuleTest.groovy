package graphql.validation.directives

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import graphql.validation.TestUtil
import graphql.validation.interpolation.MessageInterpolator
import graphql.validation.rules.ValidationRuleEnvironment
import spock.lang.Specification

class BaseDirectiveRuleTest extends Specification {

    MessageInterpolator interpolator = new MessageInterpolator() {

        @Override
        GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams, ValidationRuleEnvironment ruleEnvironment) {
            def s = messageTemplate.replace("graphql.validation.", "").replace(".message", "")
            s += ";"
            s += "path=" + ruleEnvironment.getFieldOrArgumentPath().toString() + ";"
            s += "val:" + messageParams.getOrDefault("fieldOrArgumentValue", "") + ";"
            return GraphqlErrorBuilder.newError().message(s).build()
        }
    }

    List<GraphQLError> runRules(DirectiveValidationRule ruleUnderTest, String fieldDeclaration, String argName, Object argValue) {
        def schema = buildSchema(ruleUnderTest.getDirectiveDeclarationSDL(), fieldDeclaration, "")

        ValidationRuleEnvironment ruleEnvironment = buildEnv(ruleUnderTest.name, schema, argName, argValue)

        return ruleUnderTest.runValidation(ruleEnvironment)
    }

    void assertErrors(List<GraphQLError> errors, String expectedMessage) {
        def message = ""
        for (def e : errors) {
            message += e.message + "\t"
        }
        assert message == expectedMessage, "expected '" + expectedMessage + "' but got \n'" + message + "'"
    }


    ValidationRuleEnvironment buildEnv(String targetDirective, GraphQLSchema schema, String argName, argValue) {
        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")
        GraphQLArgument argUnderTest = fieldDefinition.getArgument(argName)


        def ruleEnvironment = ValidationRuleEnvironment.newValidationRuleEnvironment()
                .argument(argUnderTest)
                .fieldOrArgumentValue(argValue)
                .fieldOrArgumentType(argUnderTest.getType())
                .fieldDefinition(fieldDefinition)
                .fieldsContainer(fieldsContainer)
                .context(GraphQLDirective.class, argUnderTest.getDirective(targetDirective))
                .messageInterpolator(interpolator)
                .build()
        ruleEnvironment
    }

    GraphQLSchema buildSchema(String directiveDeclarationSDL, String fieldDeclaration, String extraSDL) {
        def sdl = """

            input InputObject {
                name : String
                age : Int
            }

            ${directiveDeclarationSDL}

            ${extraSDL}

            type Query {
                ${fieldDeclaration}
            }
        """
        TestUtil.schema(sdl)
    }

}