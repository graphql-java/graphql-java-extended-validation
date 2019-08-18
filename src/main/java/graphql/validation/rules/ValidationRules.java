package graphql.validation.rules;

import graphql.Assert;
import graphql.GraphQLError;
import graphql.PublicApi;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.util.FpKit;
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
import static graphql.validation.rules.ValidationEnvironment.ValidatedElement.INPUT_OBJECT_FIELD;

/**
 * ValidationRules is a holder of {@link graphql.validation.rules.ValidationRule}s against a specific
 * type, field and possible argument via {@link ValidationCoordinates}.  It then allows those rules
 * to be run against the specific fields based on runtime execution during {@link graphql.schema.DataFetcher}
 * invocations.
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

    /**
     * Runs the contained rules that match the currently executing field named by the {@link graphql.schema.DataFetchingEnvironment}
     *
     * @param env          the field being executed
     * @param interpolator the message interpolator to use
     * @param locale       the locale in play
     * @return a list of zero or more data validation errors
     */
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
            GraphQLInputType inputType = fieldArg.getType();

            ValidationEnvironment ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
                    .dataFetchingEnvironment(env)
                    .argument(fieldArg)
                    .validatedElement(ARGUMENT)
                    .validatedType(inputType)
                    .validatedValue(argValue)
                    .validatedPath(fieldPath.segment(fieldArg.getName()))
                    .directives(fieldArg.getDirectives())
                    .messageInterpolator(interpolator)
                    .locale(locale)
                    .build();

            for (ValidationRule rule : rules) {
                List<GraphQLError> ruleErrors = runValidationImpl(rule, ruleEnvironment, inputType, argValue);
                errors.addAll(ruleErrors);
            }
        }

        return errors;
    }

    @SuppressWarnings("unchecked")
    private List<GraphQLError> runValidationImpl(ValidationRule rule, ValidationEnvironment validationEnvironment, GraphQLInputType inputType, Object validatedValue) {
        List<GraphQLError> errors = rule.runValidation(validationEnvironment);
        if (validatedValue == null) {
            return errors;
        }

        inputType = (GraphQLInputType) GraphQLTypeUtil.unwrapNonNull(inputType);

        if (GraphQLTypeUtil.isList(inputType)) {
            List<Object> values = new ArrayList<>(FpKit.toCollection(validatedValue));
            List<GraphQLError> ruleErrors = walkListArg(rule, validationEnvironment, (GraphQLList) inputType, values);
            errors.addAll(ruleErrors);
        }

        if (inputType instanceof GraphQLInputObjectType) {
            if (validatedValue instanceof Map) {
                Map<String, Object> objectValue = (Map<String, Object>) validatedValue;
                List<GraphQLError> ruleErrors = walkObjectArg(rule, validationEnvironment, (GraphQLInputObjectType) inputType, objectValue);
                errors.addAll(ruleErrors);
            } else {
                Assert.assertShouldNeverHappen("How can there be a `input` object type '%s' that does not have a matching Map java value", GraphQLTypeUtil.simplePrint(inputType));
            }
        }
        return errors;
    }


    private List<GraphQLError> walkObjectArg(ValidationRule rule, ValidationEnvironment validationEnvironment, GraphQLInputObjectType argumentType, Map<String, Object> objectMap) {
        List<GraphQLError> errors = new ArrayList<>();

        // run them in a stable order
        List<GraphQLInputObjectField> fieldDefinitions = Util.sort(argumentType.getFieldDefinitions(), GraphQLInputObjectField::getName);
        for (GraphQLInputObjectField inputField : fieldDefinitions) {

            GraphQLInputType fieldType = inputField.getType();
            List<GraphQLDirective> directives = inputField.getDirectives();
            Object validatedValue = objectMap.getOrDefault(inputField.getName(), inputField.getDefaultValue());
            if (validatedValue == null) {
                continue;
            }

            ExecutionPath newPath = validationEnvironment.getValidatedPath().segment(inputField.getName());

            ValidationEnvironment newValidationEnvironment = validationEnvironment.transform(builder -> builder
                    .validatedPath(newPath)
                    .validatedValue(validatedValue)
                    .validatedType(fieldType)
                    .directives(inputField.getDirectives())
                    .validatedElement(INPUT_OBJECT_FIELD)
            );

            List<GraphQLError> ruleErrors = runValidationImpl(rule, newValidationEnvironment, fieldType, validatedValue);
            errors.addAll(ruleErrors);
        }
        return errors;
    }

    private List<GraphQLError> walkListArg(ValidationRule rule, ValidationEnvironment validationEnvironment, GraphQLList argumentType, List<Object> objectList) {
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

            ExecutionPath newPath = validationEnvironment.getValidatedPath().segment(ix);

            ValidationEnvironment newValidationEnvironment = validationEnvironment.transform(builder -> builder
                    .validatedPath(newPath)
                    .validatedValue(value)
                    .validatedType(listItemType)
                    .directives(directives)
            );

            List<GraphQLError> ruleErrors = runValidationImpl(rule, newValidationEnvironment, listItemType, value);
            errors.addAll(ruleErrors);
            ix++;
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
