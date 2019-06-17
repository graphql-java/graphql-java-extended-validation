package graphql.validation.constraints;

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
import graphql.validation.constraints.standard.ArgumentsConstraint;
import graphql.validation.constraints.standard.AssertFalseConstraint;
import graphql.validation.constraints.standard.AssertTrueConstraint;
import graphql.validation.constraints.standard.DecimalMaxConstraint;
import graphql.validation.constraints.standard.DecimalMinConstraint;
import graphql.validation.constraints.standard.DigitsConstraint;
import graphql.validation.constraints.standard.MaxConstraint;
import graphql.validation.constraints.standard.MinConstraint;
import graphql.validation.constraints.standard.NegativeConstraint;
import graphql.validation.constraints.standard.NegativeOrZeroConstraint;
import graphql.validation.constraints.standard.NotBlankRule;
import graphql.validation.constraints.standard.NotEmptyRule;
import graphql.validation.constraints.standard.PatternConstraint;
import graphql.validation.constraints.standard.PositiveConstraint;
import graphql.validation.constraints.standard.PositiveOrZeroConstraint;
import graphql.validation.constraints.standard.RangeConstraint;
import graphql.validation.constraints.standard.SizeConstraint;
import graphql.validation.rules.ValidationEnvironment;
import graphql.validation.rules.ValidationRule;
import graphql.validation.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This contains a liszt of {@link graphql.validation.constraints.DirectiveConstraint}s and
 * runs them as a group on a field and its argument values.
 * <p>
 * This ships with a set of standard constraints via {@link #STANDARD_CONSTRAINTS} but you can
 * add your own implementations if you wish
 */
@PublicApi
public class DirectiveConstraints implements ValidationRule {

    /**
     * These are the standard directive rules that come with the system
     */
    public final static List<DirectiveConstraint> STANDARD_CONSTRAINTS = Arrays.asList(
            new ArgumentsConstraint(),
            new AssertFalseConstraint(),
            new AssertTrueConstraint(),
            new DecimalMaxConstraint(),
            new DecimalMinConstraint(),
            new DigitsConstraint(),
            new MaxConstraint(),
            new MinConstraint(),
            new NegativeOrZeroConstraint(),
            new NegativeConstraint(),
            new NotBlankRule(),
            new NotEmptyRule(),
            new PatternConstraint(),
            new PositiveOrZeroConstraint(),
            new PositiveConstraint(),
            new RangeConstraint(),
            new SizeConstraint()
    );

    private final Map<String, DirectiveConstraint> constraints;

    public DirectiveConstraints(Builder builder) {
        this.constraints = Collections.unmodifiableMap((builder.directiveRules));
    }

    public static Builder newDirectiveConstraints() {
        return new Builder();
    }

    public Map<String, DirectiveConstraint> getConstraints() {
        return constraints;
    }

    public String getDirectivesSDL() {
        StringBuilder sb = new StringBuilder();
        for (DirectiveConstraint directiveConstraint : constraints.values()) {
            sb.append("\n   ").append(directiveConstraint.getDocumentation().getDirectiveSDL()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean appliesTo(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        for (DirectiveConstraint directiveRule : constraints.values()) {
            boolean applies = directiveRule.appliesTo(fieldDefinition, fieldsContainer);
            if (applies) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean appliesTo(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        for (DirectiveConstraint directiveRule : constraints.values()) {
            boolean applies = directiveRule.appliesTo(argument, fieldDefinition, fieldsContainer);
            if (applies) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {

        GraphQLArgument argument = validationEnvironment.getArgument();
        Object validatedValue = validationEnvironment.getValidatedValue();
        List<GraphQLDirective> directives = argument.getDirectives();

        //
        // all the directives validation code does NOT care for NULL ness since the graphql engine covers that.
        // eg a @NonNull validation directive makes no sense in graphql like it might in Java
        //
        GraphQLInputType inputType = Util.unwrapNonNull(validationEnvironment.getFieldOrArgumentType());
        validationEnvironment = validationEnvironment.transform(b -> b.fieldOrArgumentType(inputType));

        return runValidationImpl(validationEnvironment, inputType, validatedValue, directives);
    }

    @SuppressWarnings("unchecked")
    private List<GraphQLError> runValidationImpl(ValidationEnvironment validationEnvironment, GraphQLInputType inputType, Object validatedValue, List<GraphQLDirective> directives) {
        List<GraphQLError> errors = new ArrayList<>();
        for (GraphQLDirective directive : directives) {
            DirectiveConstraint validationRule = constraints.get(directive.getName());
            if (validationRule == null) {
                continue;
            }

            validationEnvironment = validationEnvironment.transform(b -> b.context(GraphQLDirective.class, directive));
            //
            // now run the directive rule with this directive instance
            List<GraphQLError> ruleErrors = validationRule.runValidation(validationEnvironment);
            errors.addAll(ruleErrors);
        }

        if (validatedValue == null) {
            return errors;
        }

        inputType = (GraphQLInputType) GraphQLTypeUtil.unwrapNonNull(inputType);

        if (GraphQLTypeUtil.isList(inputType)) {
            List<Object> values = new ArrayList<>(FpKit.toCollection(validatedValue));
            List<GraphQLError> ruleErrors = walkListArg(validationEnvironment, (GraphQLList) inputType, values);
            errors.addAll(ruleErrors);
        }

        if (inputType instanceof GraphQLInputObjectType) {
            if (validatedValue instanceof Map) {
                Map<String, Object> objectValue = (Map<String, Object>) validatedValue;
                List<GraphQLError> ruleErrors = walkObjectArg(validationEnvironment, (GraphQLInputObjectType) inputType, objectValue);
                errors.addAll(ruleErrors);
            } else {
                Assert.assertShouldNeverHappen("How can there be a `input` object type '%s' that does not have a matching Map java value", GraphQLTypeUtil.simplePrint(inputType));
            }
        }
        return errors;
    }

    private List<GraphQLError> walkObjectArg(ValidationEnvironment validationEnvironment, GraphQLInputObjectType argumentType, Map<String, Object> objectMap) {
        List<GraphQLError> errors = new ArrayList<>();

        for (GraphQLInputObjectField inputField : argumentType.getFieldDefinitions()) {

            GraphQLInputType fieldType = inputField.getType();
            List<GraphQLDirective> directives = inputField.getDirectives();
            Object validatedValue = objectMap.getOrDefault(inputField.getName(), inputField.getDefaultValue());
            if (validatedValue == null) {
                continue;
            }

            ExecutionPath fieldOrArgPath = validationEnvironment.getFieldOrArgumentPath().segment(inputField.getName());

            ValidationEnvironment newValidationEnvironment = validationEnvironment.transform(builder -> builder
                    .fieldOrArgumentPath(fieldOrArgPath)
                    .validatedValue(validatedValue)
                    .fieldOrArgumentType(fieldType)
            );

            List<GraphQLError> ruleErrors = runValidationImpl(newValidationEnvironment, fieldType, validatedValue, directives);
            errors.addAll(ruleErrors);
        }
        return errors;
    }

    private List<GraphQLError> walkListArg(ValidationEnvironment validationEnvironment, GraphQLList argumentType, List<Object> objectList) {
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

            ExecutionPath fieldOrArgPath = validationEnvironment.getFieldOrArgumentPath().segment(ix);

            ValidationEnvironment newValidationEnvironment = validationEnvironment.transform(builder -> builder
                    .fieldOrArgumentPath(fieldOrArgPath)
                    .validatedValue(value)
                    .fieldOrArgumentType(listItemType)
            );

            List<GraphQLError> ruleErrors = runValidationImpl(newValidationEnvironment, listItemType, value, directives);
            errors.addAll(ruleErrors);
            ix++;
        }
        return errors;
    }


    public static class Builder {
        private Map<String, DirectiveConstraint> directiveRules = new LinkedHashMap<>();

        public Builder() {
            STANDARD_CONSTRAINTS.forEach(this::addRule);
        }

        public Builder addRule(DirectiveConstraint rule) {
            directiveRules.put(rule.getName(), rule);
            return this;
        }

        public Builder clearRules() {
            directiveRules.clear();
            return this;
        }

        public DirectiveConstraints build() {
            return new DirectiveConstraints(this);
        }
    }
}
