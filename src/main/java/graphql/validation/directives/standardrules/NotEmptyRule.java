package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;

// @NotEmpty(message : String = "graphql.validation.NotEmpty.message"
public class NotEmptyRule extends AbstractDirectiveValidationRule {

    public NotEmptyRule() {
        super("NotEmpty");
    }


    @Override
    protected boolean appliesToType(GraphQLInputType argumentType) {
        return isStringOrListOrMap(argumentType);
    }


    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getArgumentValue();
        GraphQLInputType argumentType = ruleEnvironment.getArgumentType();

        GraphQLDirective ruleDirective = getArgDirective(ruleEnvironment, getName());
        int valLen = getStringOrObjectOrMapLength(argumentType, argumentValue);

        if (valLen <= 0) {
            return mkError(ruleEnvironment, ruleDirective, mkMessageParams(
                    "length", valLen,
                    "argumentValue", argumentValue));
        }
        return Collections.emptyList();
    }


}
