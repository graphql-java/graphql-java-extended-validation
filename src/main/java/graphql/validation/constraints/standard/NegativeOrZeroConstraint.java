package graphql.validation.constraints.standard;

import java.math.BigDecimal;

public class NegativeOrZeroConstraint extends AbstractPositiveNegativeConstraint {

    public NegativeOrZeroConstraint() {
        super("NegativeOrZero");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @NegativeOrZero(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The element must be a negative number or zero.";
    }

    @Override
    public String getExample() {
        return "driver( licencePoints : Int @NegativeOrZero) : DriverDetails";
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) <= 0;
    }

}
