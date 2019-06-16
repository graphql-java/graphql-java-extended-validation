package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

public class DecimalMinConstraint extends AbstractDecimalMinMaxConstraint {

    public DecimalMinConstraint() {
        super("DecimalMin");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The element must be a number whose value must be greater than or equal to the specified minimum.")

                .example("driveCar( carHorsePower : Float @DecimalMin(value : \"300.50\") : DriverDetails")

                .applicableTypeNames(getApplicableTypeNames())

                .directiveSDL("directive @DecimalMin(value : String!, inclusive : Boolean! = true, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    protected boolean isOK(boolean inclusive, int comparisonResult) {
        return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
    }
}
