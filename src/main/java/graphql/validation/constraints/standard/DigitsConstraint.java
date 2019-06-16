package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class DigitsConstraint extends AbstractDirectiveConstraint {

    public DigitsConstraint() {
        super("Digits");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Digits(integer : Int!, fraction : Int!, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                Scalars.GraphQLString,
                Scalars.GraphQLByte,
                Scalars.GraphQLShort,
                Scalars.GraphQLInt,
                Scalars.GraphQLLong,
                Scalars.GraphQLBigDecimal,
                Scalars.GraphQLBigInteger,
                Scalars.GraphQLFloat
        );
    }

    @Override
    public List<String> getApplicableTypeNames() {
        return Stream.of(Scalars.GraphQLString,
                Scalars.GraphQLByte,
                Scalars.GraphQLShort,
                Scalars.GraphQLInt,
                Scalars.GraphQLLong,
                Scalars.GraphQLBigDecimal,
                Scalars.GraphQLBigInteger,
                Scalars.GraphQLFloat)
                .map(GraphQLScalarType::getName)
                .collect(toList());
    }

    @Override
    public String getDescription() {
        return "The element must be a number inside the specified `integer` and `fraction` range.";
    }

    @Override
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        if (validatedValue == null) {
            return Collections.emptyList();
        }

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
