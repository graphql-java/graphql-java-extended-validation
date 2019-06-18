package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.el.ELSupport;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArgumentsConstraint extends AbstractDirectiveConstraint {

    public ArgumentsConstraint() {
        super("Arguments");
    }


    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The provided expression must evaluate to true.")

                .example("drivers( first : Int, after : String!, last : Int, before : String) \n" +
                        " : DriverConnection @Arguments(expression : \"${args.containsOneOf('first','last') }\"")

                .applicableTypeNames("Output Fields")

                .directiveSDL("directive @Arguments(expression : String!, message : String = \"%s\") " +
                                "on FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return false;
    }

    @Override
    public boolean appliesTo(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return fieldDefinition.getDirective(getName()) != null;
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        GraphQLFieldDefinition fieldDefinition = validationEnvironment.getFieldDefinition();
        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        String expression = curlyBraces(getStrArg(directive, "expression"));

        Map<String, Object> variables = mkMap(
                "fieldDefinition", fieldDefinition,
                "args", validationEnvironment.getArgumentValues()
        );
        ELSupport elSupport = new ELSupport(validationEnvironment.getLocale());
        boolean isOK = elSupport.evaluateBoolean(expression, variables);

        if (!isOK) {
            return mkError(validationEnvironment, directive, mkMessageParams(null, validationEnvironment,
                    "expression", expression));

        }
        return Collections.emptyList();
    }

    private String curlyBraces(String expression) {
        expression = expression.trim();
        if (!expression.startsWith("${") && !expression.startsWith("#{")) {
            expression = "${" + expression;
        }
        if (!expression.startsWith("}")) {
            expression = expression + "}";
        }
        return expression;
    }
}
