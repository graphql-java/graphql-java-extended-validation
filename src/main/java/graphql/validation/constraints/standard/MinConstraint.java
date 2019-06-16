package graphql.validation.constraints.standard;

public class MinConstraint extends AbstractMinMaxConstraint {

    public MinConstraint() {
        super("Min");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Min(value : Int! = 0, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The element must be a number whose value must be greater than or equal to the specified minimum.";
    }

    @Override
    public String getExample() {
        return "driver( age : Int @Min(value : 18) : DriverDetails";
    }


    @Override
    protected boolean isOK(int comparisonResult) {
        return comparisonResult >= 0;
    }
}
