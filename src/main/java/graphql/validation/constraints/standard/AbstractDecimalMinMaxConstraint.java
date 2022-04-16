package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractDecimalMinMaxConstraint extends AbstractDirectiveConstraint {

    public AbstractDecimalMinMaxConstraint(String name) {
        super(name);
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                Scalars.GraphQLString, // note we allow strings
                ExtendedScalars.GraphQLByte,
                ExtendedScalars.GraphQLShort,
                Scalars.GraphQLInt,
                ExtendedScalars.GraphQLLong,
                ExtendedScalars.GraphQLBigDecimal,
                ExtendedScalars.GraphQLBigInteger,
                Scalars.GraphQLFloat
        );
    }

    public List<String> getApplicableTypeNames() {
        return Stream.of(Scalars.GraphQLString, // note we allow strings
                ExtendedScalars.GraphQLByte,
                ExtendedScalars.GraphQLShort,
                Scalars.GraphQLInt,
                ExtendedScalars.GraphQLLong,
                ExtendedScalars.GraphQLBigDecimal,
                ExtendedScalars.GraphQLBigInteger,
                Scalars.GraphQLFloat)
                .map(GraphQLScalarType::getName)
                .collect(Collectors.toList());
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        //null values are valid
        if (validatedValue == null) {
            return Collections.emptyList();
        }

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
                    "value", value,
                    "inclusive", inclusive));

        }
        return Collections.emptyList();
    }

    abstract protected boolean isOK(boolean inclusive, int comparisonResult);


}
