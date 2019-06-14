package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class DigitsRule extends AbstractDirectiveValidationRule {

    public DigitsRule() {
        super("Digits");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Digits(integer : Int!, fraction : Int!, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.Digits.message");
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
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();
        if (argumentValue == null) {
            return Collections.emptyList();
        }

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
        int maxIntegerLength = getIntArg(directive, "integer");
        int maxFractionLength = getIntArg(directive, "fraction");

        boolean isOk;
        try {
            BigDecimal bigNum = asBigDecimal(argumentValue);
            isOk = isOk(bigNum, maxIntegerLength, maxFractionLength);
        } catch (NumberFormatException e) {
            isOk = false;
        }

        if (!isOk) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "integer", maxIntegerLength,
                    "fraction", maxFractionLength,
                    "fieldOrArgumentValue", argumentValue));
        }
        return Collections.emptyList();
    }

    private boolean isOk(BigDecimal bigNum, int maxIntegerLength, int maxFractionLength) {
        int integerPartLength = bigNum.precision() - bigNum.scale();
        int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

        return maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength;
    }
}
