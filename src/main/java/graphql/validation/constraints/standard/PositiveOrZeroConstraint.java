package graphql.validation.constraints.standard;

import graphql.validation.constraints.Documentation;

import java.math.BigDecimal;

public class PositiveOrZeroConstraint extends AbstractPositiveNegativeConstraint {

    public PositiveOrZeroConstraint() {
        super("PositiveOrZero");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element must be a positive number or zero.")
                .example("driver( licencePoints : Int @PositiveOrZero) : DriverDetails")
                .applicableTypes(getApplicableTypes())
                .directiveSDL("directive @PositiveOrZero(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) >= 0;
    }

}
