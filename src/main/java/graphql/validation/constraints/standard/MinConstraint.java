package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

public class MinConstraint extends AbstractMinMaxConstraint {

    public MinConstraint() {
        super("Min");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The element must be a number whose value must be greater than or equal to the specified minimum.")

                .example("driveCar( age : Int @Min(value : 18) : DriverDetails")

                .applicableTypeNames(getApplicableTypeNames())

                .directiveSDL("directive @Min(value : Int! = 0, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    protected boolean isOK(int comparisonResult) {
        return comparisonResult >= 0;
    }
}
