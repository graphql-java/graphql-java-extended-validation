package graphql.validation.schemawiring;

import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.validation.rules.ValidationRuleCandidates;
import graphql.validation.rules.ValidationRules;

import java.util.List;

public class ValidationSchemaWiring implements SchemaDirectiveWiring {

    private final ValidationRuleCandidates ruleCandidates;

    public ValidationSchemaWiring(ValidationRuleCandidates ruleCandidates) {
        this.ruleCandidates = ruleCandidates;
    }

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> env) {
        GraphQLFieldsContainer fieldsContainer = env.getFieldsContainer();
        GraphQLFieldDefinition fieldDefinition = env.getFieldDefinition();

        ValidationRules rules = ruleCandidates.getRulesFor(fieldDefinition, fieldsContainer);
        if (rules.isEmpty()) {
            return fieldDefinition;
        }

        // ok we have some rules that need to be applied to this field and its arguments
        DataFetcher currentDF = env.getCodeRegistry().getDataFetcher(fieldsContainer, fieldDefinition);
        DataFetcher newDF = environment -> {
            List<GraphQLError> errors = rules.runValidationRules(environment, ruleCandidates.getMessageInterpolator());
            if (!errors.isEmpty()) {
                return DataFetcherResult.newResult().errors(errors).build();
            }
            // we have no validation errors so call the underlying data fetcher
            return currentDF.get(environment);
        };
        env.getCodeRegistry().dataFetcher(fieldsContainer, fieldDefinition, newDF);

        return fieldDefinition;
    }
}
