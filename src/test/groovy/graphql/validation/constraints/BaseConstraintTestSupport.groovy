package graphql.validation.constraints

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.execution.ExecutionStepInfo
import graphql.execution.MergedField
import graphql.execution.ResultPath
import graphql.language.Field
import graphql.language.SourceLocation
import graphql.schema.*
import graphql.validation.TestUtil
import graphql.validation.interpolation.MessageInterpolator
import graphql.validation.rules.TargetedValidationRules
import graphql.validation.rules.ValidationCoordinates
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Specification

class BaseConstraintTestSupport extends Specification {

    MessageInterpolator interpolator = new MessageInterpolator() {

        @Override
        GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams, ValidationEnvironment validationEnvironment) {
            def s = messageTemplate.replace("graphql.validation.", "").replace(".message", "")
            s += ";"
            s += "path=" + validationEnvironment.getValidatedPath().toString() + ";"
            s += "val:" + messageParams.getOrDefault("validatedValue", "") + ";"
            return GraphqlErrorBuilder.newError().message(s).build()
        }
    }

    List<GraphQLError> runValidation(DirectiveConstraint ruleUnderTest, String fieldDeclaration, String argName, Object argValue) {
        runValidation(ruleUnderTest, fieldDeclaration, "", argName, argValue)
    }

    List<GraphQLError> runValidation(DirectiveConstraint ruleUnderTest, String fieldDeclaration, String extraSDL, String argName, Object argValue) {
        def schema = buildSchema(ruleUnderTest.getDocumentation().getDirectiveSDL(), fieldDeclaration, extraSDL)

        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")
        GraphQLArgument argUnderTest = fieldDefinition.getArgument(argName)

        ValidationCoordinates coordinates = ValidationCoordinates.newCoordinates(fieldsContainer, fieldDefinition, argUnderTest)

        def validationRules = TargetedValidationRules.newValidationRules().addRule(coordinates, ruleUnderTest).build()

        def path = ResultPath.rootPath()

        def astField = Field.newField()
                .name(fieldDefinition.name)
                .sourceLocation(new SourceLocation(6, 9))
                .build()
        def astMergedField = MergedField.newMergedField().addField(astField).build()

        def stepInfo = ExecutionStepInfo.newExecutionStepInfo()
                .fieldDefinition(fieldDefinition)
                .fieldContainer(fieldsContainer as GraphQLObjectType)
                .type(fieldDefinition.getType())
                .path(path).build()

        def argsMap = [:]
        argsMap[argName] = argValue

        def dfe = DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
                .fieldDefinition(fieldDefinition)
                .executionStepInfo(stepInfo)
                .fieldType(fieldDefinition.getType())
                .mergedField(astMergedField)
                .arguments(argsMap)
                .build()

        return validationRules.runValidationRules(dfe, interpolator, Locale.getDefault())
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
                .validatedType(argUnderTest.getType())
                .fieldDefinition(fieldDefinition)
                .fieldsContainer(fieldsContainer)
                .executionPath(ResultPath.rootPath().segment(fieldDefinition.getName()))
                .validatedPath(ResultPath.rootPath().segment(argName))
                .context(GraphQLAppliedDirective.class, argUnderTest.getAppliedDirective(targetDirective))
                .messageInterpolator(interpolator)
                .build()
        ruleEnvironment
    }

    ValidationEnvironment buildEnvForField(DirectiveConstraint targetDirective, GraphQLSchema schema, Map<String, Object> arguments) {
        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")

        def path = ResultPath.rootPath().segment(fieldDefinition.getName())
        def ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
                .argumentValues(arguments)
                .fieldsContainer(fieldsContainer)
                .fieldDefinition(fieldDefinition)
                .executionPath(path)
                .validatedElement(ValidationEnvironment.ValidatedElement.FIELD)
                .validatedPath(path)
                .directives(fieldDefinition.getAppliedDirectives())
                .context(GraphQLAppliedDirective.class, fieldDefinition.getAppliedDirective(targetDirective.name))
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