package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

public class MaxConstraint extends AbstractMinMaxConstraint {

    public MaxConstraint() {
        super("Max");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element must be a number whose value must be less than or equal to the specified maximum.")
                .example("driveCar( horsePower : Float @Max(value : 1000) : DriverDetails")
                .applicableTypes(getApplicableTypes())
                .directiveSDL("directive @Max(value : Int! = %d, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        Integer.MAX_VALUE, getMessageTemplate())
                .build();
    }

    @Override
    protected boolean isOK(int comparisonResult) {
        return comparisonResult <= 0;
    }
}
