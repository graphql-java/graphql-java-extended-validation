package graphql.validation.directives.standardrules;

import java.math.BigDecimal;

public class PositiveRule extends AbstractPositiveNegativeRule {

    public PositiveRule() {
        super("Positive");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Positive(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.Positive.message");
    }

    @Override
    protected boolean isOK(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }

}
