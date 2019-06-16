package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.rules.ValidationEnvironment;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import java.util.Collections;
import java.util.List;

public class ArgumentsConstraint extends AbstractDirectiveConstraint {

    public ArgumentsConstraint() {
        super("@Arguments");
    }


    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("TODO")

                .example("drivers( first : Int, after : String!, last : Int, before : String) \n" +
                        " : DriverConnection @Arguments(expr : \"${(! empty first && ! empty after) || (! empty last && ! empty before)}\"")

                .applicableTypeNames("Output Fields")

                .directiveSDL("directive @Arguments(expr : String!, message : String = \"%s\") " +
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
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {

        GraphQLFieldDefinition fieldDefinition = validationEnvironment.getFieldDefinition();

        // TODO
        return Collections.emptyList();
    }

    private void bindVariable(ExpressionFactory expressionFactory, ELContext elContext, String variableName, Object variable, Class variableClass) {
        ValueExpression valueExpression = expressionFactory.createValueExpression(
                variable,
                variableClass
        );
        elContext.getVariableMapper().setVariable(variableName, valueExpression);
    }
}
