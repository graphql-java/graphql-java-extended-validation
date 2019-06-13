package graphql.validation.directives.standardrules;

import graphql.Assert;
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

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
        int maxIntegerLength = getIntArg(directive, "integer");
        int maxFractionLength = getIntArg(directive, "fraction");

        if (argumentValue == null) {
            return Collections.emptyList();
        }

        BigDecimal bigNum;
        if (argumentValue instanceof BigDecimal) {
            bigNum = (BigDecimal) argumentValue;
        } else if (argumentValue instanceof Number) {
            bigNum = new BigDecimal(argumentValue.toString()).stripTrailingZeros();
        } else {
            return Assert.assertShouldNeverHappen("You MUST provide a Number of the Digits directive rule");
        }

        int integerPartLength = bigNum.precision() - bigNum.scale();
        int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

        if (!(maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength)) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "integer", maxIntegerLength,
                    "fraction", fractionPartLength,
                    "fieldOrArgumentValue", argumentValue));
        }
        return Collections.emptyList();
    }
}
