package graphql.validation.constraints.standard;

public class DecimalMinConstraint extends AbstractDecimalMinMaxConstraint {

    public DecimalMinConstraint() {
        super("DecimalMin");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @DecimalMin(value : String!, inclusive : Boolean! = true, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The element must be a number whose value must be greater than or equal to the specified minimum.";
    }

    @Override
    protected boolean isOK(boolean inclusive, int comparisonResult) {
        return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
    }
}
