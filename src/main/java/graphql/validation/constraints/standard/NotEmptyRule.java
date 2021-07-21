package graphql.validation.constraints.standard;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.Documentation;

public class NotEmptyRule extends AbstractNotEmptyRule {

    public NotEmptyRule() {
        super("NotEmpty");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element must have a non zero size")
                .example("updateAccident( accidentNotes : [String!]! @NotEmpty) : DriverDetails")
                .applicableTypeNames(Scalars.GraphQLString.getName(), Scalars.GraphQLID.getName())
                .directiveSDL("directive @NotEmpty(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isStringOrID(inputType);
    }
}
