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

public class NotEmptyRule extends AbstractDirectiveConstraint {

    public NotEmptyRule() {
        super("NotEmpty");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The element must have a non zero size.")

                .example("updateAccident( accidentNotes : [Notes]! @NotEmpty) : DriverDetails")

                .applicableTypeNames(Scalars.GraphQLString.getName(), Scalars.GraphQLID.getName(), "Lists", "Input Objects")

                .directiveSDL("directive @NotEmpty(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isStringOrIDOrListOrMap(inputType);
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        GraphQLInputType argumentType = validationEnvironment.getValidatedType();

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        int size = getStringOrIDOrObjectOrMapLength(argumentType, validatedValue);

        if (size <= 0) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment,
                    "size", size
            ));
        }
        return Collections.emptyList();
    }


}
