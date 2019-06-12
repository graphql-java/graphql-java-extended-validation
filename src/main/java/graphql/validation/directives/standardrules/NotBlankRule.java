package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;

// @Max(value : String = "0", message : String = "graphql.validation.Max.message"
public class NotBlankRule extends AbstractDirectiveValidationRule {

    public NotBlankRule() {
        super("NotBlank");
    }


    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @%s(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getName(), "graphql.validation.NotBlank.message");
    }

    @Override
    protected boolean appliesToType(GraphQLInputType argumentType) {
        return appliesToTypes(argumentType, Scalars.GraphQLString);
    }


    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getArgumentValue();

        GraphQLDirective notBlankDir = getArgDirective(ruleEnvironment, getName());


        if (argumentValue == null || isBlank(argumentValue)) {
            return mkError(ruleEnvironment, notBlankDir, mkMessageParams(
                    "argumentValue", argumentValue));

        }
        return Collections.emptyList();
    }

    private boolean isBlank(Object value) {
        char[] chars = value.toString().toCharArray();
        for (char c : chars) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }
}
