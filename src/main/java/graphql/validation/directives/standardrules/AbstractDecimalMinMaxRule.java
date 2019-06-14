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
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractDecimalMinMaxRule extends AbstractDirectiveValidationRule {

    public AbstractDecimalMinMaxRule(String name) {
        super(name);
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                Scalars.GraphQLString, // note we allow strings
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
        return Stream.of(Scalars.GraphQLString, // note we allow strings
                Scalars.GraphQLByte,
                Scalars.GraphQLShort,
                Scalars.GraphQLInt,
                Scalars.GraphQLLong,
                Scalars.GraphQLBigDecimal,
                Scalars.GraphQLBigInteger,
                Scalars.GraphQLFloat)
                .map(GraphQLScalarType::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();
        //null values are valid
        if (argumentValue == null) {
            return Collections.emptyList();
        }

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
        String value = getStrArg(directive, "value");
        boolean inclusive = getBoolArg(directive, "inclusive");

        boolean isOK;
        try {
            BigDecimal directiveBD = new BigDecimal(value);
            BigDecimal argBD = asBigDecimal(argumentValue);
            int comparisonResult = argBD.compareTo(directiveBD);
            isOK = isOK(inclusive, comparisonResult);

        } catch (NumberFormatException nfe) {
            isOK = false;
        }

        if (!isOK) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "value", argumentValue,
                    "inclusive", inclusive,
                    "fieldOrArgumentValue", argumentValue));

        }
        return Collections.emptyList();
    }

    abstract protected boolean isOK(boolean inclusive, int comparisonResult);


}
