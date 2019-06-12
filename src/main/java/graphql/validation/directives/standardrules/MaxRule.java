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

// @Max(value : String = "0", message : String = "graphql.validation.Max.message"
public class MaxRule extends AbstractDirectiveValidationRule {

    public MaxRule() {
        super("Max");
    }


    @Override
    protected boolean appliesToType(GraphQLInputType argumentType) {
        return appliesToTypes(argumentType,
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
        Object argumentValue = ruleEnvironment.getArgumentValue();

        GraphQLDirective maxDirective = getArgDirective(ruleEnvironment, getName());
        String maxValue = getStrArg(maxDirective, "value");

        BigDecimal maxDB = new BigDecimal(maxValue);
        BigDecimal valueBD = asBigDecimal(argumentValue);

        if (maxDB.compareTo(valueBD) > 0) {
            return mkError(ruleEnvironment, maxDirective, mkMessageParams(
                    "max", maxValue,
                    "argumentValue", argumentValue));

        }
        return Collections.emptyList();
    }


}
