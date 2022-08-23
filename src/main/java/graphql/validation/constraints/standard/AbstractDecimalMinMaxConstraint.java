package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static graphql.validation.constraints.GraphQLScalars.GRAPHQL_NUMBER_AND_STRING_TYPES;

abstract class AbstractDecimalMinMaxConstraint extends AbstractDirectiveConstraint {
    public AbstractDecimalMinMaxConstraint(String name) {
        super(name);
    }

    public List<GraphQLScalarType> getApplicableTypes() {
        return GRAPHQL_NUMBER_AND_STRING_TYPES;
    }

    @Override
    protected boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, GRAPHQL_NUMBER_AND_STRING_TYPES);
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        GraphQLAppliedDirective directive = validationEnvironment.getContextObject(GraphQLAppliedDirective.class);
        String value = getStrArg(directive, "value");
        boolean inclusive = getBoolArg(directive, "inclusive");

        boolean isOK;
        try {
            BigDecimal directiveBD = new BigDecimal(value);
            BigDecimal argBD = asBigDecimal(validatedValue);
            int comparisonResult = argBD.compareTo(directiveBD);
            isOK = isOK(inclusive, comparisonResult);
        } catch (NumberFormatException nfe) {
            isOK = false;
        }

        if (!isOK) {
            return mkError(validationEnvironment, "value", value, "inclusive", inclusive);
        }

        return Collections.emptyList();
    }

    abstract protected boolean isOK(boolean inclusive, int comparisonResult);

    @Override
    protected boolean appliesToListElements() {
        return true;
    }
}
