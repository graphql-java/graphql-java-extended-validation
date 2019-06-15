package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static graphql.Scalars.GraphQLString;
import static java.util.stream.Collectors.toList;

public class RangeRule extends AbstractDirectiveValidationRule {

    public RangeRule() {
        super("Range");
    }


    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Range(min : Int = 0, max : Int = %d, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                Integer.MAX_VALUE, "graphql.validation.Range.message");
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                GraphQLString,
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
        return Stream.of(GraphQLString,
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
        return "The element range must be between the specified `min` and `max` boundaries (inclusive).  It " +
                "accepts numbers and strings that represent numerical values.";
    }

    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {

        Object validatedValue = ruleEnvironment.getValidatedValue();
        //null values are valid
        if (validatedValue == null) {
            return Collections.emptyList();
        }

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
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
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "min", min,
                    "max", max,
                    "validatedValue", validatedValue));

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
