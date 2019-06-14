package graphql.validation.interpolation;

import graphql.GraphQLError;
import graphql.PublicSpi;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Map;

@PublicSpi
public interface MessageInterpolator {
    GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams, ValidationRuleEnvironment ruleEnvironment);
}
