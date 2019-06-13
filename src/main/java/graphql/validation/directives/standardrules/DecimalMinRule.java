package graphql.validation.directives.standardrules;

public class DecimalMinRule extends AbstractDecimalMinMaxRule {

    public DecimalMinRule() {
        super("DecimalMin");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @DecimalMin(value : String!, inclusive : Boolean! = true, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.DecimalMin.message");
    }

    @Override
    protected boolean isOK(boolean inclusive, int comparisonResult) {
        return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
    }
}
