package graphql.validation.locale

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.execution.ExecutionStepInfo
import graphql.execution.MergedField
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLFieldsContainer
import graphql.validation.TestUtil
import graphql.validation.constraints.DirectiveConstraints
import graphql.validation.interpolation.ResourceBundleMessageInterpolator
import graphql.validation.rules.TargetedValidationRules
import graphql.validation.rules.ValidationCoordinates
import graphql.validation.rules.ValidationEnvironment
import graphql.validation.rules.ValidationRule
import spock.lang.Specification

class LocaleUtilTest extends Specification {

    def directiveRules = DirectiveConstraints.newDirectiveConstraints().build()

    def sdl = '''

            ''' + directiveRules.directivesSDL + '''

            type Car {
                model : String
                make : String
            }

            input CarFilter {
                model : String @Size(max : 10)
                make : String
                age : Int @Range(max : 5) @Expression(value : "${validatedValue==20}")
            }


            type Query {
                cars(filter : CarFilter) : [Car] @Expression(value : "${false}")
            }
        '''

    def schema = TestUtil.schema(sdl)

    static class LocaleProviderImpl implements LocaleProvider {
        Locale locale

        LocaleProviderImpl(Locale locale) {
            this.locale = locale
        }

        @Override
        Locale getLocale() {
            return locale
        }
    }

    static class ReflectionLocaleImpl {
        Locale locale

        ReflectionLocaleImpl(Locale locale) {
            this.locale = locale
        }

        @SuppressWarnings("unused")
        Locale getLocale() {
            return locale
        }
    }

    def "integration test that it can detect locale from dfe objects"() {

        def queryType = schema.getQueryType()
        def carFieldDef = queryType.getFieldDefinition("cars")

        def field = TestUtil.parseField('cars')
        def mergedField = MergedField.newMergedField(field).build()

        ExecutionStepInfo esi = ExecutionStepInfo.newExecutionStepInfo()
                .type(carFieldDef.getType())
                .fieldDefinition(carFieldDef)
                .fieldContainer(queryType)
                .build()


        def customRule = new ValidationRule() {
            @Override
            boolean appliesTo(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
                return true
            }

            @Override
            boolean appliesTo(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
                return false
            }

            @Override
            List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
                return [GraphqlErrorBuilder.newError().message("Locale=" + validationEnvironment.getLocale().getCountry()).build()]
            }
        }

        def targetedValidationRules = TargetedValidationRules.newValidationRules().addRule(ValidationCoordinates.newCoordinates(queryType, carFieldDef), customRule).build()

        when:

        DataFetchingEnvironment dfe = DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
                .mergedField(mergedField)
                .fieldDefinition(carFieldDef)
                .graphQLSchema(schema)
                .parentType(queryType)
                .executionStepInfo(esi)
                .build()

        def errors = targetedValidationRules.runValidationRules(dfe, new ResourceBundleMessageInterpolator(), Locale.CHINA)
        then:
        errors[0].message == "Locale=CN"

        // extract from context
        when:

        dfe = DataFetchingEnvironmentImpl.newDataFetchingEnvironment(dfe)
                .context(new LocaleProviderImpl(Locale.CANADA))
                .build()

        errors = targetedValidationRules.runValidationRules(dfe, new ResourceBundleMessageInterpolator(), Locale.CHINA)
        then:
        errors[0].message == "Locale=CA"

        // extract from source
        when:

        dfe = DataFetchingEnvironmentImpl.newDataFetchingEnvironment(dfe)
                .context(null)
                .source(new LocaleProviderImpl(Locale.FRANCE))
                .build()

        errors = targetedValidationRules.runValidationRules(dfe, new ResourceBundleMessageInterpolator(), Locale.CHINA)
        then:
        errors[0].message == "Locale=FR"

        // extract from root
        when:

        dfe = DataFetchingEnvironmentImpl.newDataFetchingEnvironment(dfe)
                .context(null)
                .source(null)
                .root(new LocaleProviderImpl(Locale.GERMANY))
                .build()

        errors = targetedValidationRules.runValidationRules(dfe, new ResourceBundleMessageInterpolator(), Locale.CHINA)
        then:
        errors[0].message == "Locale=DE"

        // use reflection
        when:

        dfe = DataFetchingEnvironmentImpl.newDataFetchingEnvironment(dfe)
                .context(new ReflectionLocaleImpl(Locale.UK))
                .build()

        errors = targetedValidationRules.runValidationRules(dfe, new ResourceBundleMessageInterpolator(), Locale.CHINA)
        then:
        errors[0].message == "Locale=GB"

        // use reflection caching
        when:

        dfe = DataFetchingEnvironmentImpl.newDataFetchingEnvironment(dfe)
                .context(new ReflectionLocaleImpl(Locale.UK))
                .build()

        errors = targetedValidationRules.runValidationRules(dfe, new ResourceBundleMessageInterpolator(), Locale.CHINA)
        then:
        errors[0].message == "Locale=GB"
    }
}
