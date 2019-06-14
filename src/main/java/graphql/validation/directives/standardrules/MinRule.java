package graphql.validation.directives.standardrules;

public class MinRule extends AbstractMinMaxRule {

    public MinRule() {
        super("Min");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Min(value : Int! = 0, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.Min.message");
    }

    @Override
    public String getDescription() {
        return "The element must be a number whose value must be greater than or equal to the specified minimum.";
    }

    @Override
    protected boolean isOK(int comparisonResult) {
        return comparisonResult >= 0;
    }
}
