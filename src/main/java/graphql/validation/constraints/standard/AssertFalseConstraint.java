package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

import static graphql.Scalars.GraphQLBoolean;

public class AssertFalseConstraint extends AbstractAssertConstraint {

    public AssertFalseConstraint() {
        super("AssertFalse");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The boolean value must be false.")

                .example("updateDriver( isDrunk : Boolean @AssertFalse) : DriverDetails")

                .applicableTypeNames(GraphQLBoolean.getName())

                .directiveSDL("directive @AssertFalse(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }


    @Override
    protected boolean isOK(boolean isTrue) {
        return !isTrue;
    }
}
