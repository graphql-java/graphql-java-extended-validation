package graphql.validation.constraints.standard;

public class DecimalMaxConstraint extends AbstractDecimalMinMaxConstraint {

    public DecimalMaxConstraint() {
        super("DecimalMax");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @DecimalMax(value : String!, inclusive : Boolean! = true, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The element must be a number whose value must be less than or equal to the specified maximum.";
    }

    @Override
    public String getExample() {
        return "driver( bloodAlcoholLevel : Float @DecimalMax(value : \"0.05\") : DriverDetails";
    }

    @Override
    protected boolean isOK(boolean inclusive, int comparisonResult) {
        return inclusive ? comparisonResult <= 0 : comparisonResult < 0;
    }
}
