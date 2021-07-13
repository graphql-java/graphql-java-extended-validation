package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

import static graphql.Scalars.GraphQLBoolean;

public class AssertTrueConstraint extends AbstractAssertConstraint {

    public AssertTrueConstraint() {
        super("AssertTrue");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The boolean value must be true.")
                .example("driveCar( hasLicence : Boolean @AssertTrue) : DriverDetails")
                .applicableTypeNames(GraphQLBoolean.getName())
                .directiveSDL("directive @AssertTrue(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    protected boolean isOK(boolean isTrue) {
        return isTrue;
    }
}
