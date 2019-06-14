package graphql.validation.directives;

import graphql.Assert;
import graphql.GraphQLError;
import graphql.PublicApi;
import graphql.execution.ExecutionPath;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLTypeUtil;
import graphql.util.FpKit;
import graphql.validation.directives.standardrules.AssertFalseRule;
import graphql.validation.directives.standardrules.AssertTrueRule;
import graphql.validation.directives.standardrules.DecimalMaxRule;
import graphql.validation.directives.standardrules.DecimalMinRule;
import graphql.validation.directives.standardrules.DigitsRule;
import graphql.validation.directives.standardrules.MaxRule;
import graphql.validation.directives.standardrules.MinRule;
import graphql.validation.directives.standardrules.NegativeOrZeroRule;
import graphql.validation.directives.standardrules.NegativeRule;
import graphql.validation.directives.standardrules.NotBlankRule;
import graphql.validation.directives.standardrules.NotEmptyRule;
import graphql.validation.directives.standardrules.PatternRule;
import graphql.validation.directives.standardrules.PositiveOrZeroRule;
import graphql.validation.directives.standardrules.PositiveRule;
import graphql.validation.directives.standardrules.SizeRule;
import graphql.validation.rules.ValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;
import graphql.validation.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PublicApi
public class DirectiveValidationRules implements ValidationRule {


    /**
     * These are the standard directive rules that come with the system
     */
    public final static List<DirectiveValidationRule> STANDARD_RULES = Arrays.asList(
            new AssertFalseRule(),
            new AssertTrueRule(),
            new DecimalMaxRule(),
            new DecimalMinRule(),
            new DigitsRule(),
            new MaxRule(),
            new MinRule(),
            new NegativeOrZeroRule(),
            new NegativeRule(),
            new NotBlankRule(),
            new NotEmptyRule(),
            new PatternRule(),
            new PositiveOrZeroRule(),
            new PositiveRule(),
            new SizeRule()
    );

    private final Map<String, DirectiveValidationRule> directiveRules;

    public DirectiveValidationRules(Builder builder) {
        this.directiveRules = Collections.unmodifiableMap((builder.directiveRules));
    }

    public Map<String, DirectiveValidationRule> getDirectiveRules() {
        return directiveRules;
    }

    public String getDirectivesDeclarationSDL() {
        StringBuilder sb = new StringBuilder();
        for (DirectiveValidationRule value : directiveRules.values()) {
            sb.append("\n   ").append(value.getDirectiveDeclarationSDL()).append("\n");
        }
        return sb.toString();
    }

    public static Builder newDirectiveValidationRules() {
        return new Builder();
    }

    @Override
    public boolean appliesToType(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return argument.getDirectives().stream().anyMatch(d -> oneOfOurs(d, argument));
    }

    private boolean oneOfOurs(GraphQLDirective directive, GraphQLArgument argument) {
        DirectiveValidationRule rule = directiveRules.get(directive.getName());
        if (rule != null) {
            return rule.appliesToType(argument.getType());
        }
        return false;
    }

    private void assertDirectiveOnTheRightType(DirectiveValidationRule directiveRule, GraphQLInputType inputType) {
        boolean applicable = directiveRule.appliesToType(inputType);
        if (!applicable) {
            String argType = GraphQLTypeUtil.simplePrint(inputType);
            Assert.assertShouldNeverHappen("The directive %s cannot be placed on arguments of type %s", directiveRule.getName(), argType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {

        GraphQLArgument argument = ruleEnvironment.getArgument();
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();
        List<GraphQLDirective> directives = argument.getDirectives();
        if (directives.isEmpty()) {
            return Collections.emptyList();
        }

        //
        // all the directives validation code does NOT care for NULL ness since the graphql engine covers that.
        // eg a @NonNull validation directive makes no sense in graphql like it might in Java
        //
        GraphQLInputType inputType = Util.unwrapNonNull(ruleEnvironment.getFieldOrArgumentType());
        ruleEnvironment = ruleEnvironment.transform(b -> b.fieldOrArgumentType(inputType));

        return runValidationImpl(ruleEnvironment, inputType, argumentValue, directives);
    }

    @SuppressWarnings("unchecked")
    private List<GraphQLError> runValidationImpl(ValidationRuleEnvironment ruleEnvironment, GraphQLInputType inputType, Object argumentValue, List<GraphQLDirective> directives) {
        List<GraphQLError> errors = new ArrayList<>();
        for (GraphQLDirective directive : directives) {
            DirectiveValidationRule validationRule = directiveRules.get(directive.getName());
            if (validationRule == null) {
                continue;
            }
            //
            // double check that directive is in fact on an element it can handle
            //
            assertDirectiveOnTheRightType(validationRule, inputType);

            ruleEnvironment = ruleEnvironment.transform(b -> b.context(GraphQLDirective.class, directive));
            //
            // now run the directive rule with this directive instance
            List<GraphQLError> ruleErrors = validationRule.runValidation(ruleEnvironment);
            errors.addAll(ruleErrors);
        }

        if (argumentValue == null) {
            return errors;
        }

        inputType = (GraphQLInputType) GraphQLTypeUtil.unwrapNonNull(inputType);

        if (GraphQLTypeUtil.isList(inputType)) {
            List<Object> values = new ArrayList<>(FpKit.toCollection(argumentValue));
            List<GraphQLError> ruleErrors = walkListArg(ruleEnvironment, (GraphQLList) inputType, values);
            errors.addAll(ruleErrors);
        }

        if (inputType instanceof GraphQLInputObjectType) {
            if (argumentValue instanceof Map) {
                Map<String, Object> objectValue = (Map<String, Object>) argumentValue;
                List<GraphQLError> ruleErrors = walkObjectArg(ruleEnvironment, (GraphQLInputObjectType) inputType, objectValue);
                errors.addAll(ruleErrors);
            } else {
                Assert.assertShouldNeverHappen("How can there be a `input` object type '%s' that does not have a matching Map java value", GraphQLTypeUtil.simplePrint(inputType));
            }
        }
        return errors;
    }

    private List<GraphQLError> walkObjectArg(ValidationRuleEnvironment ruleEnvironment, GraphQLInputObjectType argumentType, Map<String, Object> objectMap) {
        List<GraphQLError> errors = new ArrayList<>();

        for (GraphQLInputObjectField inputField : argumentType.getFieldDefinitions()) {

            GraphQLInputType fieldType = inputField.getType();
            List<GraphQLDirective> directives = inputField.getDirectives();
            Object argumentValue = objectMap.getOrDefault(inputField.getName(), inputField.getDefaultValue());
            if (argumentValue == null) {
                continue;
            }

            ExecutionPath fieldOrArgPath = ruleEnvironment.getFieldOrArgumentPath().segment(inputField.getName());

            ValidationRuleEnvironment newRuleEnvironment = ruleEnvironment.transform(builder -> builder
                    .fieldOrArgumentPath(fieldOrArgPath)
                    .fieldOrArgumentValue(argumentValue)
                    .fieldOrArgumentType(fieldType)
            );

            List<GraphQLError> ruleErrors = runValidationImpl(newRuleEnvironment, fieldType, argumentValue, directives);
            errors.addAll(ruleErrors);
        }
        return errors;
    }

    private List<GraphQLError> walkListArg(ValidationRuleEnvironment ruleEnvironment, GraphQLList argumentType, List<Object> objectList) {
        List<GraphQLError> errors = new ArrayList<>();

        GraphQLInputType listItemType = Util.unwrapOneAndAllNonNull(argumentType);
        List<GraphQLDirective> directives;
        if (!(listItemType instanceof GraphQLDirectiveContainer)) {
            directives = Collections.emptyList();
        } else {
            directives = ((GraphQLDirectiveContainer) listItemType).getDirectives();
        }
        int ix = 0;
        for (Object value : objectList) {

            ExecutionPath fieldOrArgPath = ruleEnvironment.getFieldOrArgumentPath().segment(ix);

            ValidationRuleEnvironment newRuleEnvironment = ruleEnvironment.transform(builder -> builder
                    .fieldOrArgumentPath(fieldOrArgPath)
                    .fieldOrArgumentValue(value)
                    .fieldOrArgumentType(listItemType)
            );

            List<GraphQLError> ruleErrors = runValidationImpl(newRuleEnvironment, listItemType, value, directives);
            errors.addAll(ruleErrors);
            ix++;
        }
        return errors;
    }


    public static class Builder {
        private Map<String, DirectiveValidationRule> directiveRules = new LinkedHashMap<>();

        public Builder() {
            STANDARD_RULES.forEach(this::addRule);
        }

        public Builder addRule(DirectiveValidationRule rule) {
            directiveRules.put(rule.getName(), rule);
            return this;
        }

        public Builder clearRules() {
            directiveRules.clear();
            return this;
        }

        public DirectiveValidationRules build() {
            return new DirectiveValidationRules(this);
        }
    }
}
