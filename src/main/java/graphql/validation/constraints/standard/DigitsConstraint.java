package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
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
import static java.util.stream.Collectors.toList;

public class DigitsConstraint extends AbstractDirectiveConstraint {
    private static final List<GraphQLScalarType> SUPPORTED_SCALARS = Stream.concat(
            Stream.of(Scalars.GraphQLString),
            GraphQLScalars.GRAPHQL_NUMBER_TYPES.stream()
    ).collect(Collectors.toList());

    public DigitsConstraint() {
        super("Digits");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The element must be a number inside the specified `integer` and `fraction` range.")
                .example("buyCar( carCost : Float @Digits(integer : 5, fraction : 2) : DriverDetails")
                .applicableTypeNames(SUPPORTED_SCALARS
                        .stream()
                        .map(GraphQLScalarType::getName)
                        .collect(toList()))
                .directiveSDL("directive @Digits(integer : Int!, fraction : Int!, message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, SUPPORTED_SCALARS);
    }


    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        int maxIntegerLength = getIntArg(directive, "integer");
        int maxFractionLength = getIntArg(directive, "fraction");

        boolean isOk;
        try {
            BigDecimal bigNum = asBigDecimal(validatedValue);
            isOk = isOk(bigNum, maxIntegerLength, maxFractionLength);
        } catch (NumberFormatException e) {
            isOk = false;
        }

        if (!isOk) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment,
                    "integer", maxIntegerLength,
                    "fraction", maxFractionLength));
        }
        return Collections.emptyList();
    }

    private boolean isOk(BigDecimal bigNum, int maxIntegerLength, int maxFractionLength) {
        int integerPartLength = bigNum.precision() - bigNum.scale();
        int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

        return maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength;
    }
}
