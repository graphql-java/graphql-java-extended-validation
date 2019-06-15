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

import static java.util.stream.Collectors.toList;

abstract class AbstractMinMaxRule extends AbstractDirectiveValidationRule {

    public AbstractMinMaxRule(String name) {
        super(name);
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
    public List<String> getApplicableTypeNames() {
        return Stream.of(Scalars.GraphQLByte,
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
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object validatedValue = ruleEnvironment.getValidatedValue();
        //null values are valid
        if (validatedValue == null) {
            return Collections.emptyList();
        }

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);
        int value = getIntArg(directive, "value");

        boolean isOK;
        try {
            BigDecimal directiveBD = new BigDecimal(value);
            BigDecimal argBD = asBigDecimal(validatedValue);
            int comparisonResult = argBD.compareTo(directiveBD);
            isOK = isOK(comparisonResult);

        } catch (NumberFormatException nfe) {
            isOK = false;
        }


        if (!isOK) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "value", value,
                    "validatedValue", validatedValue));

        }
        return Collections.emptyList();
    }

    abstract protected boolean isOK(int comparisonResult);
}
