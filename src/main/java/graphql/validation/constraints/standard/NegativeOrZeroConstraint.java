package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

import java.math.BigDecimal;

public class NegativeOrZeroConstraint extends AbstractPositiveNegativeConstraint {

    public NegativeOrZeroConstraint() {
        super("NegativeOrZero");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element must be a negative number or zero.")
                .example("driveCar( lostLicencePoints : Int @NegativeOrZero) : DriverDetails")
                .applicableTypeNames(getApplicableTypeNames())
                .directiveSDL("directive @NegativeOrZero(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }


    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) <= 0;
    }

}
