package graphql.validation.schemawiring;

import graphql.PublicApi;
import graphql.TrivialDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.validation.interpolation.MessageInterpolator;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.TargetedValidationRules;
import graphql.validation.rules.ValidationRules;

import java.util.Locale;

/**
 * A {@link SchemaDirectiveWiring} that can be used to inject validation rules into the data fetchers
 * when the graphql schema is being built.  It will use the validation rules and ask each one of they apply to the field and or its
 * arguments.
 * <p>
 * If there are rules that apply then it will it will change the {@link DataFetcher} of that field so that rules get run
 * BEFORE the original field fetch is run.
 */
@PublicApi
public class ValidationSchemaWiring implements SchemaDirectiveWiring {

    private final ValidationRules ruleCandidates;

    public ValidationSchemaWiring(ValidationRules ruleCandidates) {
        this.ruleCandidates = ruleCandidates;
    }

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> env) {
        GraphQLFieldsContainer fieldsContainer = env.getFieldsContainer();
        GraphQLFieldDefinition fieldDefinition = env.getFieldDefinition();
        TargetedValidationRules rules = ruleCandidates.buildRulesFor(fieldDefinition, fieldsContainer);
        if (rules.isEmpty()) {
            return fieldDefinition;
        }
        if (! (fieldsContainer instanceof GraphQLObjectType)) {
            // only object type fields can have data fetchers
            return fieldDefinition;
        }
        GraphQLObjectType graphQLObjectType = (GraphQLObjectType) fieldsContainer;
        OnValidationErrorStrategy errorStrategy = ruleCandidates.getOnValidationErrorStrategy();
        MessageInterpolator messageInterpolator = ruleCandidates.getMessageInterpolator();
        Locale locale = ruleCandidates.getLocale();

        final DataFetcher<?> currentDF = env.getCodeRegistry().getDataFetcher(graphQLObjectType, fieldDefinition);
        final DataFetcher<?> newDF = buildValidatingDataFetcher(errorStrategy, messageInterpolator, currentDF, locale);

        env.getCodeRegistry().dataFetcher(graphQLObjectType, fieldDefinition, newDF);

        return fieldDefinition;
    }

    private DataFetcher<Object> buildValidatingDataFetcher(OnValidationErrorStrategy errorStrategy,
                                                           MessageInterpolator messageInterpolator,
                                                           DataFetcher<?> currentDF,
                                                           final Locale defaultLocale) {
        if (currentDF instanceof TrivialDataFetcher) {
           return new TrivialFieldValidatorDataFetcher(
                   errorStrategy,
                   messageInterpolator,
                   currentDF,
                   defaultLocale,
                   ruleCandidates
           );
        }
        
        return new FieldValidatorDataFetcher(
                errorStrategy,
                messageInterpolator,
                currentDF,
                defaultLocale,
                ruleCandidates
        );
    }

}
