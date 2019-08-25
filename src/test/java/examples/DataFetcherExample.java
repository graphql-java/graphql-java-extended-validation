package examples;

import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.validation.rules.ValidationEnvironment;
import graphql.validation.rules.ValidationRule;
import graphql.validation.rules.ValidationRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"unused", "Convert2Lambda"})
public class DataFetcherExample {


    public static void main(String[] args) {

        //
        // an example of writing your own custom validation rule
        //
        ValidationRule myCustomValidationRule = new ValidationRule() {
            @Override
            public boolean appliesTo(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
                return fieldDefinition.getName().equals("decide whether this rule applies here");
            }

            @Override
            public boolean appliesTo(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
                return argument.getName().equals("decide whether this rule applies here to an argument");
            }

            @Override
            public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {

                List<GraphQLError> errors = new ArrayList<>();
                Map<String, Object> argumentValues = validationEnvironment.getArgumentValues();
                for (String argName : argumentValues.keySet()) {
                    Object argValue = argumentValues.get(argName);
                    GraphQLError error = runCodeThatValidatesInputHere(validationEnvironment, argName, argValue);
                    if (error != null) {
                        errors.add(error);
                    }
                }
                return errors;
            }
        };

        DataFetcher dataFetcher = new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment env) {

                //
                // By default the ValidationRule contains the SDL @directive rules, but
                // you can also add your own as we do here.
                //
                ValidationRules validationRules = ValidationRules
                        .newValidationRules()
                        .locale(Locale.getDefault())
                        .addRule(myCustomValidationRule)
                        .build();

                //
                // The expected strategy is to return null data and the errors if there are any validation
                // problems
                //
                List<GraphQLError> errors = validationRules.runValidationRules(env);
                if (!errors.isEmpty()) {
                    return DataFetcherResult.newResult().errors(errors).data(null).build();
                }
                return normalDataFetchingCodeRunsNow(env);
            }
        };


    }

    private static GraphQLError runCodeThatValidatesInputHere(ValidationEnvironment validationEnvironment, String argName, Object argValue) {
        return null;
    }

    private static Object normalDataFetchingCodeRunsNow(DataFetchingEnvironment env) {
        return null;
    }


}
