package graphql.validation.rules;

import graphql.PublicApi;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.validation.interpolation.MessageInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.validation.rules.ArgumentCoordinates.newArgumentCoordinates;

/**
 * {@link PossibleValidationRules} is a simple holder of possible rules
 * and you can then pass it field and arguments and narrow down the list of actual rules
 * that apply to those fields and arguments.
 */
@PublicApi
public class PossibleValidationRules {

    private final OnValidationErrorStrategy onValidationErrorStrategy;
    private final List<ValidationRule> rules;
    private final MessageInterpolator messageInterpolator;

    private PossibleValidationRules(Builder builder) {
        this.rules = Collections.unmodifiableList(builder.rules);
        this.messageInterpolator = builder.messageInterpolator;
        this.onValidationErrorStrategy = builder.onValidationErrorStrategy;
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

    public OnValidationErrorStrategy getOnValidationErrorStrategy() {
        return onValidationErrorStrategy;
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
        private OnValidationErrorStrategy onValidationErrorStrategy = OnValidationErrorStrategy.RETURN_NULL;
        private MessageInterpolator messageInterpolator;
        private List<ValidationRule> rules = new ArrayList<>();

        public Builder addRule(ValidationRule rule) {
            rules.add(rule);
            return this;
        }

        public Builder messageInterpolator(MessageInterpolator messageInterpolator) {
            this.messageInterpolator = messageInterpolator;
            return this;
        }

        public Builder onValidationErrorStrategy(OnValidationErrorStrategy onValidationErrorStrategy) {
            this.onValidationErrorStrategy = onValidationErrorStrategy;
            return this;
        }

        public PossibleValidationRules build() {
            return new PossibleValidationRules(this);
        }
    }
}