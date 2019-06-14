package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.Scalars.GraphQLBoolean;

abstract class AbstractAssertRule extends AbstractDirectiveValidationRule {

    public AbstractAssertRule(String name) {
        super(name);
    }


    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                GraphQLBoolean
        );
    }

    @Override
    public List<String> getApplicableTypeNames() {
        return Stream.of(GraphQLBoolean).map(GraphQLScalarType::getName).collect(Collectors.toList());
    }

    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();
        //null values are valid
        if (argumentValue == null) {
            return Collections.emptyList();
        }

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);

        boolean isTrue = asBoolean(argumentValue);
        if (!isOK(isTrue)) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "fieldOrArgumentValue", argumentValue));

        }
        return Collections.emptyList();
    }

    protected abstract boolean isOK(boolean isTrue);


}
