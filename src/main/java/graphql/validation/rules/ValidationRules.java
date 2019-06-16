package graphql.validation.rules;

import graphql.GraphQLError;
import graphql.PublicApi;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.validation.interpolation.MessageInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ValidationRules is a holder of {@link graphql.validation.rules.ValidationRule}s against a specific
 * type, field nad possible argument aka {@link ValidationCoordinates}
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

    public List<GraphQLError> runValidationRules(DataFetchingEnvironment env, MessageInterpolator interpolator) {

        List<GraphQLError> errors = new ArrayList<>();

        GraphQLObjectType fieldContainer = env.getExecutionStepInfo().getFieldContainer();
        GraphQLFieldDefinition fieldDefinition = env.getFieldDefinition();

        for (GraphQLArgument fieldArg : fieldDefinition.getArguments()) {

            ValidationCoordinates argCoords = ValidationCoordinates.newCoordinates(fieldContainer, fieldDefinition, fieldArg);

            List<ValidationRule> rules = rulesMap.getOrDefault(argCoords, Collections.emptyList());
            if (rules.isEmpty()) {
                continue;
            }

            Object argValue = env.getArgument(fieldArg.getName());

            ValidationRuleEnvironment ruleEnvironment = ValidationRuleEnvironment.newValidationRuleEnvironment()
                    .dataFetchingEnvironment(env)
                    .argument(fieldArg)
                    .validatedValue(argValue)
                    .messageInterpolator(interpolator)
                    .build();

            for (ValidationRule rule : rules) {
                List<GraphQLError> ruleErrors = rule.runValidation(ruleEnvironment);
                errors.addAll(ruleErrors);
            }
        }

        ValidationCoordinates fieldCoords = ValidationCoordinates.newCoordinates(fieldContainer, fieldDefinition);


        List<ValidationRule> rules = rulesMap.getOrDefault(fieldCoords, Collections.emptyList());
        if (!rules.isEmpty()) {
            ValidationRuleEnvironment ruleEnvironment = ValidationRuleEnvironment.newValidationRuleEnvironment()
                    .dataFetchingEnvironment(env)
                    .messageInterpolator(interpolator)
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
