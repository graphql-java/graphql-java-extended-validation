package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;

public class NotBlankRule extends AbstractDirectiveValidationRule {

    public NotBlankRule() {
        super("NotBlank");
    }


    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @NotBlank(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.NotBlank.message");
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, Scalars.GraphQLString);
    }


    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);

        if (argumentValue == null || isBlank(argumentValue)) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "fieldOrArgumentValue", argumentValue));

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
