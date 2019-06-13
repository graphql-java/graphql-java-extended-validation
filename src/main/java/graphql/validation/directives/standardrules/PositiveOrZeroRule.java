package graphql.validation.directives.standardrules;

import java.math.BigDecimal;

public class PositiveOrZeroRule extends AbstractPositiveNegativeRule {

    public PositiveOrZeroRule() {
        super("PositiveOrZero");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @PositiveOrZero(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.PositiveOrZero.message");
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) >= 0;
    }

}
