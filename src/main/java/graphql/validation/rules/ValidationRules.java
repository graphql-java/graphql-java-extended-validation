package graphql.validation.rules;

import graphql.GraphQLError;
import graphql.PublicApi;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.validation.constraints.DirectiveConstraints;
import graphql.validation.interpolation.MessageInterpolator;
import graphql.validation.interpolation.ResourceBundleMessageInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static graphql.Assert.assertNotNull;

/**
 * {@link ValidationRules} is a holder of validation rules
 * and you can then pass it field and arguments and narrow down the list of actual rules
 * that apply to those fields and arguments.
 * <p>
 * It also allows you to run the appropriate rules via the
 * {@link #runValidationRules(graphql.schema.DataFetchingEnvironment)} method.
 */
@PublicApi
public class ValidationRules {

    private final OnValidationErrorStrategy onValidationErrorStrategy;
    private final List<ValidationRule> rules;
    private final MessageInterpolator messageInterpolator;
    private final Locale locale;

    private ValidationRules(Builder builder) {
        this.rules = Collections.unmodifiableList(builder.rules);
        this.messageInterpolator = builder.messageInterpolator;
        this.onValidationErrorStrategy = builder.onValidationErrorStrategy;
        this.locale = builder.locale;
    }

    public MessageInterpolator getMessageInterpolator() {
        return messageInterpolator;
    }

    public Locale getLocale() {
        return locale;
    }

    public List<ValidationRule> getRules() {
        return rules;
    }

    public OnValidationErrorStrategy getOnValidationErrorStrategy() {
        return onValidationErrorStrategy;
    }

    public TargetedValidationRules buildRulesFor(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        TargetedValidationRules.Builder rulesBuilder = TargetedValidationRules.newValidationRules();

        ValidationCoordinates fieldCoordinates = ValidationCoordinates.newCoordinates(fieldsContainer, fieldDefinition);
        List<ValidationRule> fieldRules = getRulesFor(fieldDefinition, fieldsContainer);
        rulesBuilder.addRules(fieldCoordinates, fieldRules);

        for (GraphQLArgument fieldArg : fieldDefinition.getArguments()) {
            ValidationCoordinates validationCoordinates = ValidationCoordinates.newCoordinates(fieldsContainer, fieldDefinition, fieldArg);

            List<ValidationRule> rules = getRulesFor(fieldArg, fieldDefinition, fieldsContainer);
            rulesBuilder.addRules(validationCoordinates, rules);
        }

        return rulesBuilder.build();
    }

    public List<ValidationRule> getRulesFor(GraphQLArgument fieldArg, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return rules.stream()
                .filter(rule -> rule.appliesTo(fieldArg, fieldDefinition, fieldsContainer))
                .collect(Collectors.toList());
    }

    public List<ValidationRule> getRulesFor(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return rules.stream()
                .filter(rule -> rule.appliesTo(fieldDefinition, fieldsContainer))
                .collect(Collectors.toList());
    }


    /**
     * This helper method will run the validation rules that apply to the provided {@link graphql.schema.DataFetchingEnvironment}
     *
     * @param env the data fetching environment
     *
     * @return a list of zero or more input data validation errors
     */
    public List<GraphQLError> runValidationRules(DataFetchingEnvironment env) {
        GraphQLFieldsContainer fieldsContainer = env.getExecutionStepInfo().getObjectType();
        GraphQLFieldDefinition fieldDefinition = env.getFieldDefinition();

        MessageInterpolator messageInterpolator = this.getMessageInterpolator();

        TargetedValidationRules rules = this.buildRulesFor(fieldDefinition, fieldsContainer);
        return rules.runValidationRules(env, messageInterpolator, this.getLocale());
    }

    /**
     * A builder of validation rules.  By default the SDL @directive rules from
     * {@link graphql.validation.constraints.DirectiveConstraints#STANDARD_CONSTRAINTS} are included
     * but you can add extra rules or call {@link graphql.validation.rules.ValidationRules.Builder#clearRules()}
     * to start afresh.
     *
     * @return a new builder of rules
     */
    public static Builder newValidationRules() {
        return new Builder();
    }


    public static class Builder {
        private Locale locale;
        private OnValidationErrorStrategy onValidationErrorStrategy = OnValidationErrorStrategy.RETURN_NULL;
        private MessageInterpolator messageInterpolator = new ResourceBundleMessageInterpolator();
        private List<ValidationRule> rules = new ArrayList<>();


        public Builder() {
            // we start with the standard directive constraints to make us easier to use
            addRules(DirectiveConstraints.STANDARD_CONSTRAINTS);
        }

        public Builder addRule(ValidationRule rule) {
            rules.add(assertNotNull(rule));
            return this;
        }

        public Builder addRules(Collection<? extends ValidationRule> rules) {
            rules.forEach(this::addRule);
            return this;
        }

        public Builder addRules(ValidationRule... rules) {
            return addRules(Arrays.asList(rules));
        }

        public Builder clearRules() {
            rules.clear();
            return this;
        }

        public Builder messageInterpolator(MessageInterpolator messageInterpolator) {
            this.messageInterpolator = assertNotNull(messageInterpolator);
            return this;
        }

        /**
         * This sets the locale of the validation rules.  This is only needed while graphql-java does not allow you to get the
         * locale from the {@link graphql.ExecutionInput}.  A PR for this is in the works.  Once that is available, then this method
         * will not be as useful.
         *
         * @param locale the locale to use for message interpolation
         *
         * @return this builder
         */
        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder onValidationErrorStrategy(OnValidationErrorStrategy onValidationErrorStrategy) {
            this.onValidationErrorStrategy = assertNotNull(onValidationErrorStrategy);
            return this;
        }

        public ValidationRules build() {
            return new ValidationRules(this);
        }
    }
}