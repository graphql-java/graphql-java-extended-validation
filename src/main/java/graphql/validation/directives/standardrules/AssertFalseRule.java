package graphql.validation.directives.standardrules;

public class AssertFalseRule extends AbstractAssertRule {

    public AssertFalseRule() {
        super("AssertFalse");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @AssertFalse(message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.AssertFalse.message");
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
