package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.GraphQLScalars;
import graphql.validation.rules.ValidationEnvironment;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

abstract class AbstractMinMaxConstraint extends AbstractDirectiveConstraint {
    public AbstractMinMaxConstraint(String name) {
        super(name);
    }

    public List<GraphQLScalarType> getApplicableTypes() {
        return GraphQLScalars.GRAPHQL_NUMBER_TYPES;
    }


    @Override
    protected boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, GraphQLScalars.GRAPHQL_NUMBER_TYPES);
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        int value = getIntArg(directive, "value");

        boolean isOK;
        try {
            BigDecimal directiveBD = new BigDecimal(value);
            BigDecimal argBD = asBigDecimal(validatedValue);
            int comparisonResult = argBD.compareTo(directiveBD);
            isOK = isOK(comparisonResult);

        } catch (NumberFormatException nfe) {
            isOK = false;
        }


        if (!isOK) {
            return mkError(validationEnvironment, "value", value);
        }

        return Collections.emptyList();
    }

    abstract protected boolean isOK(int comparisonResult);
    
    @Override
    protected boolean appliesToListElements() {
        return true;
    }
}
