package graphql.validation.constraints.standard;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.Documentation;

public class SizeConstraint extends AbstractSizeConstraint {
    public SizeConstraint() {
        super("Size");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element size must be between the specified `min` and `max` boundaries (inclusive).")
                .example("updateDrivingNotes( drivingNote : String @Size( min : 1000, max : 100000)) : DriverDetails")
                .applicableTypes(Scalars.GraphQLString, Scalars.GraphQLID)
                .directiveSDL("directive @Size(min : Int = 0, max : Int = %d, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        Integer.MAX_VALUE, getMessageTemplate())
                .build();
    }


    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isStringOrID(inputType);
    }

    @Override
    protected boolean appliesToListElements() {
        return true;
    }
}
