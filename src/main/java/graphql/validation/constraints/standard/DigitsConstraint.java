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
import java.util.Optional;

import static graphql.validation.constraints.GraphQLScalars.GRAPHQL_NUMBER_AND_STRING_TYPES;

public class DigitsConstraint extends AbstractDirectiveConstraint {
    public DigitsConstraint() {
        super("Digits");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
            .messageTemplate(getMessageTemplate())
            .description("The element must be a number inside the specified `integer` and optionally inside `fraction` range.")
            .example("buyCar( carCost : Float @Digits(integer : 5, fraction : 2) : DriverDetails")
            .applicableTypes(GRAPHQL_NUMBER_AND_STRING_TYPES)
            .directiveSDL("directive @Digits(integer : Int!, fraction : Int, message : String = \"%s\") " +
                    "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate())
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
        int maxIntegerLength = getIntArg(directive, "integer");
        Optional<Integer> maxFractionLengthOpt = getIntArgOpt(directive, "fraction");

        boolean isOk;
        try {
            BigDecimal bigNum = asBigDecimal(validatedValue);
            boolean isFractionPartOk = maxFractionLengthOpt
                .map(maxFractionLength -> isFractionPartOk(bigNum, maxFractionLength))
                .orElse(true);

            isOk = isFractionPartOk && isIntegerPartOk(bigNum, maxIntegerLength);
        } catch (NumberFormatException e) {
            isOk = false;
        }

        if (!isOk) {
            return mkError(
                validationEnvironment,
                "integer",
                maxIntegerLength, "fraction",
                maxFractionLengthOpt.map(Object::toString).orElse("unlimited")
            );
        }

        return Collections.emptyList();
    }

    private static boolean isIntegerPartOk(BigDecimal bigNum, int maxIntegerLength) {
        final int integerPartLength = bigNum.precision() - bigNum.scale();
        return maxIntegerLength >= integerPartLength;
    }

    private static boolean isFractionPartOk(BigDecimal bigNum, int maxFractionLength) {
        final int fractionPartLength = Math.max(bigNum.scale(), 0);
        return maxFractionLength >= fractionPartLength;
    }

    @Override
    protected boolean appliesToListElements() {
        return true;
    }
}
