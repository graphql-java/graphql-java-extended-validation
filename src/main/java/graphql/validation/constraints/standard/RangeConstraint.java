package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.constraints.GraphQLScalars;
import graphql.validation.rules.ValidationEnvironment;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static graphql.Scalars.GraphQLString;

public class RangeConstraint extends AbstractDirectiveConstraint {
    private static final List<GraphQLScalarType> SUPPORTED_SCALARS = Stream.concat(
            Stream.of(Scalars.GraphQLString),
            GraphQLScalars.GRAPHQL_NUMBER_TYPES.stream()
    ).collect(Collectors.toList());

    public RangeConstraint() {
        super("Range");
    }


    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element range must be between the specified `min` and `max` boundaries (inclusive).  It " +
                        "accepts numbers and strings that represent numerical values.")
                .example("driver( milesTravelled : Int @Range( min : 1000, max : 100000)) : DriverDetails")
                .applicableTypes(SUPPORTED_SCALARS)
                .directiveSDL("directive @Range(min : Int = 0, max : Int = %d, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        Integer.MAX_VALUE, getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                GraphQLString,
                ExtendedScalars.GraphQLByte,
                ExtendedScalars.GraphQLShort,
                Scalars.GraphQLInt,
                ExtendedScalars.GraphQLLong,
                ExtendedScalars.GraphQLBigDecimal,
                ExtendedScalars.GraphQLBigInteger,
                Scalars.GraphQLFloat
        );
    }


    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {

        Object validatedValue = validationEnvironment.getValidatedValue();
        //null values are valid
        if (validatedValue == null) {
            return Collections.emptyList();
        }

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        BigDecimal min = asBigDecimal(getIntArg(directive, "min"));
        BigDecimal max = asBigDecimal(getIntArg(directive, "max"));

        boolean isOK;
        try {
            BigDecimal argBD = asBigDecimal(validatedValue);
            isOK = isOK(argBD, min, max);
        } catch (NumberFormatException nfe) {
            isOK = false;
        }

        if (!isOK) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment,
                    "min", min,
                    "max", max
            ));

        }
        return Collections.emptyList();
    }

    private boolean isOK(BigDecimal argBD, BigDecimal min, BigDecimal max) {
        if (argBD.compareTo(max) > 0) {
            return false;
        }
        if (argBD.compareTo(min) < 0) {
            return false;
        }
        return true;
    }
}
