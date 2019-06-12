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
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @%s(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getName(), "graphql.validation.NotEmpty.message");
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
        int size = getStringOrObjectOrMapLength(argumentType, argumentValue);

        if (size <= 0) {
            return mkError(ruleEnvironment, ruleDirective, mkMessageParams(
                    "size", size,
                    "argumentValue", argumentValue));
        }
        return Collections.emptyList();
    }


}
