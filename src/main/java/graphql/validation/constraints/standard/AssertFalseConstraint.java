package graphql.validation.constraints.standard;

public class AssertFalseConstraint extends AbstractAssertConstraint {

    public AssertFalseConstraint() {
        super("AssertFalse");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @AssertFalse(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getExample() {
        return "driver( isDrunk : Boolean @AssertFalse) : DriverDetails";
    }

    @Override
    public String getDescription() {
        return "The boolean value must be false.";
    }

    @Override
    protected boolean isOK(boolean isTrue) {
        return !isTrue;
    }
}
