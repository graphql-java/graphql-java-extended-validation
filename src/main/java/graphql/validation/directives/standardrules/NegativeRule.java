package graphql.validation.directives.standardrules;

import java.math.BigDecimal;

public class NegativeRule extends AbstractPositiveNegativeRule {

    public NegativeRule() {
        super("Negative");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Negative(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.Negative.message");
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
