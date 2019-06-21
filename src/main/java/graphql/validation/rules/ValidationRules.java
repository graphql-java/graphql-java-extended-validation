package graphql.validation.rules;

import graphql.GraphQLError;
import graphql.PublicApi;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.validation.interpolation.MessageInterpolator;
import graphql.validation.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static graphql.validation.rules.ValidationEnvironment.ValidatedElement.ARGUMENT;
import static graphql.validation.rules.ValidationEnvironment.ValidatedElement.FIELD;

/**
 * ValidationRules is a holder of {@link graphql.validation.rules.ValidationRule}s against a specific
 * type, field and possible argument via {@link ValidationCoordinates}
 */
@PublicApi
public class ValidationRules {

    private final Map<ValidationCoordinates, List<ValidationRule>> rulesMap;

    public ValidationRules(Builder builder) {
        this.rulesMap = new HashMap<>(builder.rulesMap);
    }

    public static Builder newValidationRules() {
        return new Builder();
    }

    public boolean isEmpty() {
        return rulesMap.isEmpty();
    }

    public List<GraphQLError> runValidationRules(DataFetchingEnvironment env, MessageInterpolator interpolator, Locale locale) {

        List<GraphQLError> errors = new ArrayList<>();

        GraphQLObjectType fieldContainer = env.getExecutionStepInfo().getFieldContainer();
        GraphQLFieldDefinition fieldDefinition = env.getFieldDefinition();
        ExecutionPath fieldPath = env.getExecutionStepInfo().getPath();
        //
        // run the field specific rules
        ValidationCoordinates fieldCoords = ValidationCoordinates.newCoordinates(fieldContainer, fieldDefinition);
        List<ValidationRule> rules = rulesMap.getOrDefault(fieldCoords, Collections.emptyList());
        if (!rules.isEmpty()) {
            ValidationEnvironment ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
                    .dataFetchingEnvironment(env)
                    .messageInterpolator(interpolator)
                    .validatedElement(FIELD)
                    .validatedPath(fieldPath)
                    .build();

            for (ValidationRule rule : rules) {
                List<GraphQLError> ruleErrors = rule.runValidation(ruleEnvironment);
                errors.addAll(ruleErrors);
            }
        }
        //
        // run the argument specific rules next
        List<GraphQLArgument> sortedArgs = Util.sort(fieldDefinition.getArguments(), GraphQLArgument::getName);
        for (GraphQLArgument fieldArg : sortedArgs) {

            ValidationCoordinates argCoords = ValidationCoordinates.newCoordinates(fieldContainer, fieldDefinition, fieldArg);

            rules = rulesMap.getOrDefault(argCoords, Collections.emptyList());
            if (rules.isEmpty()) {
                continue;
            }

            Object argValue = env.getArgument(fieldArg.getName());

            ValidationEnvironment ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
                    .dataFetchingEnvironment(env)
                    .argument(fieldArg)
                    .validatedElement(ARGUMENT)
                    .validatedType(fieldArg.getType())
                    .validatedValue(argValue)
                    .validatedPath(fieldPath.segment(fieldArg.getName()))
                    .messageInterpolator(interpolator)
                    .locale(locale)
                    .build();

            for (ValidationRule rule : rules) {
                List<GraphQLError> ruleErrors = rule.runValidation(ruleEnvironment);
                errors.addAll(ruleErrors);
            }
        }

        return errors;
    }

    public static class Builder {
        Map<ValidationCoordinates, List<ValidationRule>> rulesMap = new HashMap<>();

        public Builder addRule(ValidationCoordinates coordinates, ValidationRule rule) {
            rulesMap.compute(coordinates, (key, listOfRules) -> {
                if (listOfRules == null) {
                    listOfRules = new ArrayList<>();
                }
                listOfRules.add(rule);
                return listOfRules;
            });
            return this;
        }

        public Builder addRules(ValidationCoordinates argCoords, List<ValidationRule> rules) {
            for (ValidationRule rule : rules) {
                addRule(argCoords, rule);
            }
            return this;
        }

        public ValidationRules build() {
            return new ValidationRules(this);
        }
    }

}
