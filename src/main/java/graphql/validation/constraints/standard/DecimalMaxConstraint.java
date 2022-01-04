package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

public class DecimalMaxConstraint extends AbstractDecimalMinMaxConstraint {

    public DecimalMaxConstraint() {
        super("DecimalMax");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element must be a number whose value must be less than or equal to the specified maximum.")
                .example("driveCar( bloodAlcoholLevel : Float @DecimalMax(value : \"0.05\") : DriverDetails")
                .applicableTypes(getApplicableTypes())
                .directiveSDL("directive @DecimalMax(value : String!, inclusive : Boolean! = true, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    protected boolean isOK(boolean inclusive, int comparisonResult) {
        return inclusive ? comparisonResult <= 0 : comparisonResult < 0;
    }
}
