package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

import java.math.BigDecimal;

public class NegativeConstraint extends AbstractPositiveNegativeConstraint {

    public NegativeConstraint() {
        super("Negative");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element must be a negative number.")
                .example("driveCar( lostLicencePoints : Int @Negative) : DriverDetails")
                .applicableTypes(getApplicableTypes())
                .directiveSDL("directive @Negative(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) < 0;
    }

}
