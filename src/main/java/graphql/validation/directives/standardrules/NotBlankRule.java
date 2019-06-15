package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
    public String getDescription() {
        return "The String must contain at least one non-whitespace character, according to Java's Character.isWhitespace().";
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, Scalars.GraphQLString);
    }

    @Override
    public List<String> getApplicableTypeNames() {
        return Stream.of(Scalars.GraphQLString)
                .map(GraphQLScalarType::getName)
                .collect(toList());
    }


    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object validatedValue = ruleEnvironment.getValidatedValue();

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);

        if (validatedValue == null || isBlank(validatedValue)) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "validatedValue", validatedValue));

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
