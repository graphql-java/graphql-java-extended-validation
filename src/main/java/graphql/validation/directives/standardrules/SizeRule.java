package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

// @Size(min : Int = 0, max : Int = 9999999, message : String = "graphql.validation.Size.message"
public class SizeRule extends AbstractDirectiveValidationRule {

    public SizeRule() {
        super("Size");
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


        int valLen = getStringOrObjectOrMapLength(argType, argumentValue);

        Map<String, Object> msgParams = mkMessageParams(
                "min", min,
                "max", max,
                "length", valLen,
                "argumentValue", argumentValue);

        if (valLen < min) {
            return mkError(ruleEnvironment, sizeDirective, msgParams);
        }
        if (valLen > max) {
            return mkError(ruleEnvironment, sizeDirective, msgParams);
        }
        return Collections.emptyList();
    }


}
