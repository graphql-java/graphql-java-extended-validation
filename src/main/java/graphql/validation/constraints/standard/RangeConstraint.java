package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.rules.ValidationEnvironment;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static graphql.validation.constraints.GraphQLScalars.GRAPHQL_NUMBER_AND_STRING_TYPES;

public class RangeConstraint extends AbstractDirectiveConstraint {
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
                .applicableTypes(GRAPHQL_NUMBER_AND_STRING_TYPES)
                .directiveSDL("directive @Range(min : Int = 0, max : Int = %d, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        Integer.MAX_VALUE, getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, GRAPHQL_NUMBER_AND_STRING_TYPES);
    }


    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        GraphQLAppliedDirective directive = validationEnvironment.getContextObject(GraphQLAppliedDirective.class);
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
            return mkError(validationEnvironment, "min", min, "max", max);

        }
        return Collections.emptyList();
    }

    private boolean isOK(BigDecimal argBD, BigDecimal min, BigDecimal max) {
        if (argBD.compareTo(max) > 0) {
            return false;
        }

        return argBD.compareTo(min) >= 0;
    }

    @Override
    protected boolean appliesToListElements() {
        return true;
    }
}
