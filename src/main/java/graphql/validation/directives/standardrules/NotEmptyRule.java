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

public class NotEmptyRule extends AbstractDirectiveValidationRule {

    public NotEmptyRule() {
        super("NotEmpty");
    }


    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @NotEmpty(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.NotEmpty.message");
    }

    @Override
    public String getDescription() {
        return "The element must have a non zero size.";
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
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();
        GraphQLInputType argumentType = ruleEnvironment.getFieldOrArgumentType();

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
        int size = getStringOrObjectOrMapLength(argumentType, argumentValue);

        if (size <= 0) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "size", size,
                    "fieldOrArgumentValue", argumentValue));
        }
        return Collections.emptyList();
    }


}
