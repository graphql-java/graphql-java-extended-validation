package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Collections;
import java.util.List;

public class NotBlankRule extends AbstractDirectiveConstraint {

    public NotBlankRule() {
        super("NotBlank");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The String must contain at least one non-whitespace character, according to Java's Character.isWhitespace().")

                .example("updateAccident( accidentNotes : String @NotBlank) : DriverDetails")

                .applicableTypeNames(Scalars.GraphQLString.getName())

                .directiveSDL("directive @NotBlank(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, Scalars.GraphQLString);
    }

    @Override
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);

        if (validatedValue == null || isBlank(validatedValue)) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment));

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
