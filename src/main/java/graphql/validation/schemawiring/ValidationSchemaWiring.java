package graphql.validation.schemawiring;

import graphql.GraphQLError;
import graphql.PublicApi;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.validation.interpolation.MessageInterpolator;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.PossibleValidationRules;
import graphql.validation.rules.ValidationRules;
import graphql.validation.util.Util;

import java.util.List;

@PublicApi
public class ValidationSchemaWiring implements SchemaDirectiveWiring {

    private final PossibleValidationRules ruleCandidates;

    public ValidationSchemaWiring(PossibleValidationRules ruleCandidates) {
        this.ruleCandidates = ruleCandidates;
    }

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> env) {
        GraphQLFieldsContainer fieldsContainer = env.getFieldsContainer();
        GraphQLFieldDefinition fieldDefinition = env.getFieldDefinition();

        ValidationRules rules = ruleCandidates.getRulesFor(fieldDefinition, fieldsContainer);
        if (rules.isEmpty()) {
            return fieldDefinition; // no rules - no validation needed
        }

        OnValidationErrorStrategy errorStrategy = ruleCandidates.getOnValidationErrorStrategy();
        MessageInterpolator messageInterpolator = ruleCandidates.getMessageInterpolator();

        final DataFetcher currentDF = env.getCodeRegistry().getDataFetcher(fieldsContainer, fieldDefinition);
        final DataFetcher newDF = buildValidatingDataFetcher(rules, errorStrategy, messageInterpolator, currentDF);

        env.getCodeRegistry().dataFetcher(fieldsContainer, fieldDefinition, newDF);

        return fieldDefinition;
    }

    private DataFetcher buildValidatingDataFetcher(ValidationRules rules, OnValidationErrorStrategy errorStrategy, MessageInterpolator messageInterpolator, DataFetcher currentDF) {
        // ok we have some rules that need to be applied to this field and its arguments
        return environment -> {
            List<GraphQLError> errors = rules.runValidationRules(environment, messageInterpolator);
            if (!errors.isEmpty()) {
                // should we continue?
                if (!errorStrategy.shouldContinue(errors, environment)) {
                    return errorStrategy.onErrorValue(errors, environment);
                }
            }
            // we have no validation errors or they said continue so call the underlying data fetcher
            Object returnValue = currentDF.get(environment);
            if (errors.isEmpty()) {
                return returnValue;
            }
            return Util.mkDFRFromFetchedResult(errors, returnValue);
        };
    }
}
