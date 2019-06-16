package graphql.validation.constraints.standard;

import java.math.BigDecimal;

public class NegativeConstraint extends AbstractPositiveNegativeConstraint {

    public NegativeConstraint() {
        super("Negative");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Negative(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The element must be a negative number.";
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) < 0;
    }

}
