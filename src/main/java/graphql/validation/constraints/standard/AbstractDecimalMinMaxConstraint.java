package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.GraphQLScalars;
import graphql.validation.rules.ValidationEnvironment;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractDecimalMinMaxConstraint extends AbstractDirectiveConstraint {
    private static final List<GraphQLScalarType> SUPPORTED_SCALARS = Stream.concat(
            Stream.of(Scalars.GraphQLString),
            GraphQLScalars.GRAPHQL_NUMBER_TYPES.stream()
    ).collect(Collectors.toList());

    public AbstractDecimalMinMaxConstraint(String name) {
        super(name);
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, SUPPORTED_SCALARS);
    }

    public List<String> getApplicableTypeNames() {
        return SUPPORTED_SCALARS.stream()
                .map(GraphQLScalarType::getName)
                .collect(Collectors.toList());
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
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
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment,
                    "value", validatedValue,
                    "inclusive", inclusive));

        }
        return Collections.emptyList();
    }

    abstract protected boolean isOK(boolean inclusive, int comparisonResult);


}
