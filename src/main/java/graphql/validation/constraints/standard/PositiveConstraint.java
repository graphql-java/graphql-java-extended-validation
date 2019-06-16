package graphql.validation.constraints.standard;

import java.math.BigDecimal;

public class PositiveConstraint extends AbstractPositiveNegativeConstraint {

    public PositiveConstraint() {
        super("Positive");
    }

    @Override
    public String getDescription() {
        return "The element must be a positive number.";
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Positive(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getExample() {
        return "driver( licencePoints : Int @Positive) : DriverDetails";
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }

}
