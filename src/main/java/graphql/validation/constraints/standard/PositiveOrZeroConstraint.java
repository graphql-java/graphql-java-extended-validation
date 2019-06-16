package graphql.validation.constraints.standard;

import java.math.BigDecimal;

public class PositiveOrZeroConstraint extends AbstractPositiveNegativeConstraint {

    public PositiveOrZeroConstraint() {
        super("PositiveOrZero");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @PositiveOrZero(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The element must be a positive number or zero.";
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) >= 0;
    }

}
