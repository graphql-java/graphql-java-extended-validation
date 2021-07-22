package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.GraphQLScalars;
import graphql.validation.rules.ValidationEnvironment;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

abstract class AbstractPositiveNegativeConstraint extends AbstractDirectiveConstraint {

    public AbstractPositiveNegativeConstraint(String name) {
        super(name);
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, GraphQLScalars.GRAPHQL_NUMBER_TYPES);
    }

    public List<GraphQLScalarType> getApplicableTypes() {
        return GraphQLScalars.GRAPHQL_NUMBER_TYPES;
    }


    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        boolean isOK;
        try {
            BigDecimal bigDecimal = asBigDecimal(validatedValue);
            isOK = isOK(bigDecimal);
        } catch (NumberFormatException nfe) {
            isOK = false;
        }

        if (!isOK) {
            return mkError(validationEnvironment);

        }
        return Collections.emptyList();
    }

    abstract protected boolean isOK(BigDecimal bigDecimal);
}
