package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SizeRule extends AbstractDirectiveValidationRule {

    public SizeRule() {
        super("Size");
    }


    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Size(min : Int = 0, max : Int = %d, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                Integer.MAX_VALUE, "graphql.validation.Size.message");
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isStringOrListOrMap(inputType);
    }

    @Override
    public List<String> getApplicableTypeNames() {
        return Arrays.asList(Scalars.GraphQLString.getName(), "Lists", "Input Objects");
    }

    @Override
    public String getDescription() {
        return "The element size must be between the specified `min` and `max` boundaries (inclusive).";
    }

    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object validatedValue = ruleEnvironment.getValidatedValue();
        GraphQLInputType argType = ruleEnvironment.getFieldOrArgumentType();

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
        int min = getIntArg(directive, "min");
        int max = getIntArg(directive, "max");


        int size = getStringOrObjectOrMapLength(argType, validatedValue);

        if (size < min) {
            return mkError(ruleEnvironment, directive, mkParams(validatedValue, min, max, size));
        }
        if (size > max) {
            return mkError(ruleEnvironment, directive, mkParams(validatedValue, min, max, size));
        }
        return Collections.emptyList();
    }

    private Map<String, Object> mkParams(Object validatedValue, int min, int max, int size) {
        return mkMessageParams(
                "min", min,
                "max", max,
                "size", size,
                "validatedValue", validatedValue);
    }
}
