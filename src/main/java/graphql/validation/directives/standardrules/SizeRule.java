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
        return String.format("directive @%s(min : Int = %d, max : Int = %d, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getName(), 0, Integer.MAX_VALUE, "graphql.validation.Size.message");
    }

    @Override
    protected boolean appliesToType(GraphQLInputType argumentType) {
        return isStringOrListOrMap(argumentType);
    }

    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        GraphQLInputType argType = ruleEnvironment.getArgumentType();
        Object argumentValue = ruleEnvironment.getArgumentValue();

        GraphQLDirective sizeDirective = getArgDirective(ruleEnvironment, getName());
        int min = getIntArg(sizeDirective, "min", 0);
        int max = getIntArg(sizeDirective, "max", Integer.MAX_VALUE);


        int size = getStringOrObjectOrMapLength(argType, argumentValue);

        Map<String, Object> msgParams = mkMessageParams(
                "min", min,
                "max", max,
                "size", size,
                "argumentValue", argumentValue);

        if (size < min) {
            return mkError(ruleEnvironment, sizeDirective, msgParams);
        }
        if (size > max) {
            return mkError(ruleEnvironment, sizeDirective, msgParams);
        }
        return Collections.emptyList();
    }


}
