package graphql.validation.interpolation

import graphql.execution.ResultPath
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.GraphQLSchema
import graphql.validation.TestUtil
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Specification
import spock.lang.Unroll

class ResourceBundleMessageInterpolatorTest extends Specification {

    ValidationEnvironment buildEnv(GraphQLSchema schema, String argName, argValue, MessageInterpolator interpolator, Locale locale) {
        GraphQLFieldsContainer fieldsContainer = schema.getObjectType("Query") as GraphQLFieldsContainer
        GraphQLFieldDefinition fieldDefinition = fieldsContainer.getFieldDefinition("field")
        GraphQLArgument argUnderTest = fieldDefinition.getArgument(argName)

        def ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
                .argument(argUnderTest)
                .validatedValue(argValue)
                .validatedType(argUnderTest.getType())
                .fieldDefinition(fieldDefinition)
                .fieldsContainer(fieldsContainer)
                .messageInterpolator(interpolator)
                .executionPath(ResultPath.rootPath().segment(fieldDefinition.getName()))
                .validatedPath(ResultPath.rootPath().segment(argName))
                .locale(locale)
                .build()
        ruleEnvironment
    }


    def sdl = """

            input InputObject {
                name : String
                age : Int
            }

            type Query {
                field(arg : String) : String
            }
        """
    def schema = TestUtil.schema(sdl)

    @Unroll
    def "can interpolate things : #messageTemplate"() {

        def interpolatorUnderTest = new ResourceBundleMessageInterpolator()

        def validatedValue = [zig: "zag"]
        def messageParams = ["validatedValue": validatedValue, "p1": "pv1", "p2": "pv2", "min": "5", "max": "10", path : "a/b/c", "value" : "100", "inclusive" : true]
        ValidationEnvironment validationEnvironment = buildEnv(schema, "arg", validatedValue, interpolatorUnderTest, null)

        expect:

        def actual = interpolatorUnderTest.interpolate(messageTemplate, messageParams, validationEnvironment)
        actual.message == expected

        where:

        messageTemplate                   | expected
        // resource bundle finding
        'graphql.test.message'            | 'Test message with expressions : zag and replacements : pv1'
        // system level message finding from graphql.validation
        'graphql.validation.Size.message' | 'a/b/c size must be between 5 and 10'
        // message with el expression
        'graphql.validation.DecimalMax.message' | 'a/b/c must be less than or equal to 100'
        // expressions
        'Could not ${validatedValue.zig}' | 'Could not zag'
        // message param replacement
        'Must match {p1}'                 | 'Must match pv1'
    }

    @Unroll
    def "can use formatting and locales"() {

        def interpolatorUnderTest = new ResourceBundleMessageInterpolator()

        def validatedValue = 42.0
        def messageParams = ["validatedValue": validatedValue, "p1": "pv1", "p2": "pv2", "min": "5", "max": "10"]
        ValidationEnvironment validationEnvironment = buildEnv(schema, "arg", validatedValue, interpolatorUnderTest, locale)

        expect:

        def actual = interpolatorUnderTest.interpolate(messageTemplate, messageParams, validationEnvironment)
        actual.message == expected

        where:

        messageTemplate                                             | expected | locale
        // resource bundle finding
        '''${formatter.format('%1$.2f', validatedValue)}''' | '42,00' | Locale.GERMAN
        '''${formatter.format('%1$.2f', validatedValue)}''' | '42.00' | Locale.US


    }
}
