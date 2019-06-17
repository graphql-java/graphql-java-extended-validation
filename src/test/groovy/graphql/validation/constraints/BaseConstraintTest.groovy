package graphql.validation.constraints

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.execution.ExecutionPath
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import graphql.validation.TestUtil
import graphql.validation.interpolation.MessageInterpolator
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Specification

class BaseConstraintTest extends Specification {

    MessageInterpolator interpolator = new MessageInterpolator() {

        @Override
        GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams, ValidationEnvironment validationEnvironment) {
            def s = messageTemplate.replace("graphql.validation.", "").replace(".message", "")
            s += ";"
            s += "path=" + validationEnvironment.getFieldOrArgumentPath().toString() + ";"
            s += "val:" + messageParams.getOrDefault("validatedValue", "") + ";"
            return GraphqlErrorBuilder.newError().message(s).build()
        }
    }

    List<GraphQLError> runValidation(DirectiveConstraint ruleUnderTest, String fieldDeclaration, String argName, Object argValue) {
        def schema = buildSchema(ruleUnderTest.getDocumentation().getDirectiveSDL(), fieldDeclaration, "")

        ValidationEnvironment validationEnvironment = buildEnv(ruleUnderTest.name, schema, argName, argValue)

        return ruleUnderTest.runValidation(validationEnvironment)
    }

    void assertErrors(List<GraphQLError> errors, String expectedMessage) {
        def message = ""
        for (def e : errors) {
            message += e.message + "\t"
        }
        assert message == expectedMessage, "expected '" + expectedMessage + "' but got \n'" + message + "'"
    }


    ValidationEnvironment buildEnv(String targetDirective, GraphQLSchema schema, String argName, argValue) {
        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")
        GraphQLArgument argUnderTest = fieldDefinition.getArgument(argName)


        def ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
                .argument(argUnderTest)
                .validatedValue(argValue)
                .fieldOrArgumentType(argUnderTest.getType())
                .fieldDefinition(fieldDefinition)
                .fieldsContainer(fieldsContainer)
                .executionPath(ExecutionPath.rootPath().segment(fieldDefinition.getName()))
                .fieldOrArgumentPath(ExecutionPath.rootPath().segment(argName))
                .context(GraphQLDirective.class, argUnderTest.getDirective(targetDirective))
                .messageInterpolator(interpolator)
                .build()
        ruleEnvironment
    }

    ValidationEnvironment buildEnv(String targetDirective, GraphQLSchema schema, Map<String, Object> arguments) {
        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")

        def ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
                .argumentValues(arguments)
                .fieldDefinition(fieldDefinition)
                .fieldsContainer(fieldsContainer)
                .executionPath(ExecutionPath.rootPath().segment(fieldDefinition.getName()))
                .context(GraphQLDirective.class, fieldDefinition.getDirective(targetDirective))
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