package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotEmptyRule extends AbstractDirectiveConstraint {

    public NotEmptyRule() {
        super("NotEmpty");
    }


    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @NotEmpty(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The element must have a non zero size.";
    }

    @Override
    public String getExample() {
        return "updateAccident( accidentNotes : [Notes]! @NotEmpty) : DriverDetails";
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
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        GraphQLInputType argumentType = validationEnvironment.getFieldOrArgumentType();

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        int size = getStringOrObjectOrMapLength(argumentType, validatedValue);

        if (size <= 0) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment,
                    "size", size
            ));
        }
        return Collections.emptyList();
    }


}
