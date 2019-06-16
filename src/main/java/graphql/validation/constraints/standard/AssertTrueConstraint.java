package graphql.validation.constraints.standard;

public class AssertTrueConstraint extends AbstractAssertConstraint {

    public AssertTrueConstraint() {
        super("AssertTrue");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @AssertTrue(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The boolean value must be true.";
    }

    @Override
    protected boolean isOK(boolean isTrue) {
        return isTrue;
    }
}
