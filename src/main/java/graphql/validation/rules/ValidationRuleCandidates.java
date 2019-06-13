package graphql.validation.rules;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.validation.interpolation.MessageInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.validation.rules.ArgumentCoordinates.newArgumentCoordinates;

public class ValidationRuleCandidates {
    private final List<ValidationRule> rules;
    private final MessageInterpolator messageInterpolator;

    private ValidationRuleCandidates(Builder builder) {
        this.rules = new ArrayList<>(builder.rules);
        this.messageInterpolator = builder.messageInterpolator;
    }

    public static Builder newValidationRuleCandidates() {
        return new Builder();
    }

    public MessageInterpolator getMessageInterpolator() {
        return messageInterpolator;
    }

    public List<ValidationRule> getRules() {
        return rules;
    }

    public ValidationRules getRulesFor(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        ValidationRules.Builder rulesBuilder = ValidationRules.newValidationRules();
        for (GraphQLArgument fieldArg : fieldDefinition.getArguments()) {
            ArgumentCoordinates argumentCoordinates = newArgumentCoordinates(fieldsContainer, fieldDefinition, fieldArg);

            List<ValidationRule> rules = getRulesFor(fieldArg, fieldDefinition, fieldsContainer);
            rulesBuilder.addRules(argumentCoordinates, rules);
        }

        return rulesBuilder.build();
    }

    public List<ValidationRule> getRulesFor(GraphQLArgument fieldArg, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return rules.stream()
                .filter(rule -> rule.appliesToType(fieldArg, fieldDefinition, fieldsContainer))
                .collect(Collectors.toList());
    }

    public static class Builder {
        private MessageInterpolator messageInterpolator;
        private List<ValidationRule> rules = new ArrayList<>();

        public Builder addRule(ValidationRule rule) {
            rules.add(rule);
            return this;
        }

        public Builder addRule(MessageInterpolator messageInterpolator) {
            this.messageInterpolator = messageInterpolator;
            return this;
        }

        public ValidationRuleCandidates build() {
            return new ValidationRuleCandidates(this);
        }
    }
}