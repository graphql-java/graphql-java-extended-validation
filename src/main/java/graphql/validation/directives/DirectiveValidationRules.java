package graphql.validation.directives;

import graphql.GraphQLError;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.validation.directives.standardrules.MaxRule;
import graphql.validation.directives.standardrules.NotBlankRule;
import graphql.validation.directives.standardrules.NotEmptyRule;
import graphql.validation.directives.standardrules.SizeRule;
import graphql.validation.rules.ValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DirectiveValidationRules implements ValidationRule {
    private final Map<String, DirectiveValidationRule> directiveRules;

    public DirectiveValidationRules(Builder builder) {
        this.directiveRules = Collections.unmodifiableMap((builder.directiveRules));
    }

    public Map<String, DirectiveValidationRule> getDirectiveRules() {
        return directiveRules;
    }

    public static Builder newDirectiveValidationRules() {
        return new Builder();
    }

    @Override
    public boolean appliesToArgument(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return argument.getDirectives().stream().anyMatch(d -> oneOfOurs(d, argument, fieldDefinition, fieldsContainer));
    }

    private boolean oneOfOurs(GraphQLDirective directive, GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        DirectiveValidationRule rule = directiveRules.get(directive.getName());
        if (rule != null) {
            return rule.appliesToArgument(argument, fieldDefinition, fieldsContainer);
        }
        return false;
    }

    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        List<GraphQLError> errors = new ArrayList<>();
        GraphQLArgument argument = ruleEnvironment.getArgument();
        for (GraphQLDirective argumentDirective : argument.getDirectives()) {
            DirectiveValidationRule validationRule = directiveRules.get(argumentDirective.getName());
            if (validationRule == null) {
                continue;
            }
            List<GraphQLError> ruleErrors = validationRule.runValidation(ruleEnvironment);
            errors.addAll(ruleErrors);
        }
        return errors;
    }

    public static class Builder {
        private Map<String, DirectiveValidationRule> directiveRules = new LinkedHashMap<>();

        public Builder() {
            standardRules();
        }

        private void standardRules() {
            addRule(new SizeRule());
            addRule(new MaxRule());
            addRule(new NotEmptyRule());
            addRule(new NotBlankRule());
        }

        public Builder addRule(DirectiveValidationRule rule) {
            directiveRules.put(rule.getName(), rule);
            return this;
        }

        public DirectiveValidationRules build() {
            return new DirectiveValidationRules(this);
        }
    }
}
