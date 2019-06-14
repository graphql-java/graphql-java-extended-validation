package graphql.validation.directives.standardrules;

public class AssertTrueRule extends AbstractAssertRule {

    public AssertTrueRule() {
        super("AssertTrue");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @AssertTrue(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.AssertTrue.message");
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
