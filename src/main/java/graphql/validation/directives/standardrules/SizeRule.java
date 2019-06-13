package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

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
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        GraphQLInputType argType = ruleEnvironment.getFieldOrArgumentType();
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
        int min = getIntArg(directive, "min");
        int max = getIntArg(directive, "max");


        int size = getStringOrObjectOrMapLength(argType, argumentValue);

        if (size < min) {
            return mkError(ruleEnvironment, directive, mkParams(argumentValue, min, max, size));
        }
        if (size > max) {
            return mkError(ruleEnvironment, directive, mkParams(argumentValue, min, max, size));
        }
        return Collections.emptyList();
    }

    private Map<String, Object> mkParams(Object argumentValue, int min, int max, int size) {
        return mkMessageParams(
                "min", min,
                "max", max,
                "size", size,
                "fieldOrArgumentValue", argumentValue);
    }
}
