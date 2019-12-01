package graphql.validation.rules;

import static graphql.validation.rules.ValidationEnvironment.ValidatedElement.ARGUMENT;
import static graphql.validation.rules.ValidationEnvironment.ValidatedElement.FIELD;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import graphql.validation.locale.LocaleUtil;
import graphql.validation.rules.ValidationEnvironment.ValidatedElement;
import graphql.validation.util.Util;

/**
 * TargetedValidationRules is a holder of {@link graphql.validation.rules.ValidationRule}s targeted
 * against a specific type, field and possible argument via {@link ValidationCoordinates}. It then
 * allows those rules to be run against the specific fields based on runtime execution during
 * {@link graphql.schema.DataFetcher} invocations.
 */
@PublicApi
public class TargetedValidationRules {

  private final ValidationRules validationRules;

  private final Map<ValidationCoordinates, List<ValidationRule>> rulesMap;

  public TargetedValidationRules(Builder builder) {
    this.rulesMap = new HashMap<>(builder.rulesMap);
    this.validationRules = builder.validationRules;
  }

  public static Builder newValidationRules(ValidationRules validationRules) {
    return new Builder(validationRules);
  }

  public boolean isEmpty() {
    return rulesMap.isEmpty();
  }

  /**
   * Runs the contained rules that match the currently executing field named by the
   * {@link graphql.schema.DataFetchingEnvironment}
   *
   * @param env the field being executed
   * @param interpolator the message interpolator to use
   * @param defaultLocale the default locale in play
   *
   * @return a list of zero or more input data validation errors
   */
  public List<GraphQLError> runValidationRules(DataFetchingEnvironment env,
      MessageInterpolator interpolator, Locale defaultLocale) {

    defaultLocale = LocaleUtil.determineLocale(env, defaultLocale);

    List<GraphQLError> errors = new ArrayList<>();

    GraphQLObjectType fieldContainer = env.getExecutionStepInfo().getFieldContainer();
    GraphQLFieldDefinition fieldDefinition = env.getFieldDefinition();
    ExecutionPath fieldPath = env.getExecutionStepInfo().getPath();
    //
    // run the field specific rules
    ValidationCoordinates fieldCoords =
        ValidationCoordinates.newCoordinates(fieldContainer, fieldDefinition);
    List<ValidationRule> rules = rulesMap.getOrDefault(fieldCoords, Collections.emptyList());
    if (!rules.isEmpty()) {
      ValidationEnvironment ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
          .dataFetchingEnvironment(env).messageInterpolator(interpolator).locale(defaultLocale)
          .validatedElement(FIELD).validatedPath(fieldPath).build();

      for (ValidationRule rule : rules) {
        List<GraphQLError> ruleErrors = rule.runValidation(ruleEnvironment);
        errors.addAll(ruleErrors);
      }
    }
    //
    // run the argument specific rules next
    List<GraphQLArgument> sortedArgs =
        Util.sort(fieldDefinition.getArguments(), GraphQLArgument::getName);
    for (GraphQLArgument fieldArg : sortedArgs) {

      ValidationCoordinates argCoords =
          ValidationCoordinates.newCoordinates(fieldContainer, fieldDefinition, fieldArg);

      rules = rulesMap.getOrDefault(argCoords, Collections.emptyList());
      if (rules.isEmpty()) {
        continue;
      }

      Object argValue = env.getArgument(fieldArg.getName());
      GraphQLInputType inputType = fieldArg.getType();

      ValidationEnvironment ruleEnvironment = ValidationEnvironment.newValidationEnvironment()
          .dataFetchingEnvironment(env).argument(fieldArg).validatedElement(ARGUMENT)
          .graphQLSchema(env.getGraphQLSchema()).validatedType(inputType).validatedValue(argValue)
          .validatedPath(fieldPath.segment(fieldArg.getName())).directives(fieldArg.getDirectives())
          .messageInterpolator(interpolator).locale(defaultLocale).build();

      errors.addAll(runValidationImpl(rules, ruleEnvironment, inputType, argValue, null));
    }

    return errors;
  }

  @SuppressWarnings("unchecked")
  private List<GraphQLError> runValidationImpl(List<ValidationRule> rules,
      ValidationEnvironment validationEnvironment, GraphQLInputType inputType,
      Object validatedValue, GraphQLInputObjectType parentInputType) {
    List<GraphQLError> errors = new ArrayList<GraphQLError>();
    for (ValidationRule rule : rules) {
      errors.addAll(rule.runValidation(validationEnvironment));
    }

    if (validatedValue == null) {
      return errors;
    }

    inputType = (GraphQLInputType) GraphQLTypeUtil.unwrapNonNull(inputType);


    if (GraphQLTypeUtil.isList(inputType)) {
      List<Object> values = new ArrayList<>(FpKit.toCollection(validatedValue));
      List<GraphQLError> ruleErrors = walkListArg(rules, validationEnvironment,
          (GraphQLList) inputType, values, parentInputType);
      errors.addAll(ruleErrors);
    }


    if (inputType instanceof GraphQLInputObjectType) {
      if (validatedValue instanceof Map) {
        Map<String, Object> objectValue = (Map<String, Object>) validatedValue;
        List<GraphQLError> ruleErrors =
            walkObjectArg(validationEnvironment, (GraphQLInputObjectType) inputType, objectValue);
        errors.addAll(ruleErrors);
      } else {
        Assert.assertShouldNeverHappen(
            "How can there be a `input` object type '%s' that does not have a matching Map java value",
            GraphQLTypeUtil.simplePrint(inputType));
      }
    }
    return errors;
  }


  private List<GraphQLError> walkObjectArg(ValidationEnvironment validationEnvironment,
      GraphQLInputObjectType argumentType, Map<String, Object> objectMap) {
    List<GraphQLError> errors = new ArrayList<>();

    // run them in a stable order
    List<GraphQLInputObjectField> fieldDefinitions =
        Util.sort(argumentType.getFieldDefinitions(), GraphQLInputObjectField::getName);
    for (GraphQLInputObjectField inputField : fieldDefinitions) {
      Object validatedValue =
          objectMap.getOrDefault(inputField.getName(), inputField.getDefaultValue());
      if (validatedValue == null) {
        continue;
      }

      ExecutionPath newPath =
          validationEnvironment.getValidatedPath().segment(inputField.getName());
      GraphQLInputObjectField fieldDef = validationEnvironment.getGraphQLSchema().getCodeRegistry()
          .getFieldVisibility().getFieldDefinition(argumentType, inputField.getName());

      ValidationEnvironment newValidationEnvironment = validationEnvironment
          .transform(builder -> builder.validatedPath(newPath).validatedValue(validatedValue)
              .validatedType(inputField.getType()).directives(fieldDef.getDirectives())
              .validatedElement(ValidatedElement.INPUT_OBJECT_FIELD));


      List<ValidationRule> rulesChild = validationRules.getRulesFor(
          newValidationEnvironment.getArgument(), newValidationEnvironment.getFieldDefinition(),
          newValidationEnvironment.getFieldsContainer());

      errors.addAll(runValidationImpl(rulesChild, newValidationEnvironment, inputField.getType(),
          validatedValue, argumentType));

    }
    return errors;
  }

  private List<GraphQLError> walkListArg(List<ValidationRule> rules,
      ValidationEnvironment validationEnvironment, GraphQLList argumentType,
      List<Object> objectList, GraphQLInputObjectType parentInputType) {
    List<GraphQLError> errors = new ArrayList<>();

    GraphQLInputType listItemType = Util.unwrapOneAndAllNonNull(argumentType);
    List<GraphQLDirective> directives;
    if (!(listItemType instanceof GraphQLDirectiveContainer)) {
      directives = Collections.emptyList();
    } else {
      directives = validationEnvironment.getGraphQLSchema().getCodeRegistry().getFieldVisibility()
          .getFieldDefinition(parentInputType,
              validationEnvironment.getValidatedPath().getSegmentName())
          .getDirectives();
    }

    int ix = 0;
    for (Object value : objectList) {

      ExecutionPath newPath = validationEnvironment.getValidatedPath().segment(ix);

      ValidationEnvironment newValidationEnvironment =
          validationEnvironment.transform(builder -> builder.validatedPath(newPath)
              .validatedValue(value).validatedType(listItemType).directives(directives));

      List<GraphQLError> ruleErrors =
          runValidationImpl(rules, newValidationEnvironment, listItemType, value, parentInputType);
      errors.addAll(ruleErrors);
      ix++;
    }
    return errors;
  }

  public static class Builder {
    ValidationRules validationRules;
    Map<ValidationCoordinates, List<ValidationRule>> rulesMap = new HashMap<>();

    public Builder(ValidationRules validationRules) {
      this.validationRules = validationRules;
    }

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

    public TargetedValidationRules build() {
      return new TargetedValidationRules(this);
    }
  }

}
