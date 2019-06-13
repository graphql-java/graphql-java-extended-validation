package graphql.validation.directives.standardrules;

import java.math.BigDecimal;

public class NegativeOrZeroRule extends AbstractPositiveNegativeRule {

    public NegativeOrZeroRule() {
        super("NegativeOrZero");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @NegativeOrZero(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.NegativeOrZero.message");
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) <= 0;
    }

}
