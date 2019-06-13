package graphql.validation.directives.standardrules;

public class MaxRule extends AbstractMinMaxRule {

    public MaxRule() {
        super("Max");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Max(value : Int! = %d, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                Integer.MAX_VALUE, "graphql.validation.Max.message");
    }

    @Override
    protected boolean isOK(int comparisonResult) {
        return comparisonResult <= 0;
    }
}
