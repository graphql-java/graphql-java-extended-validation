package graphql.validation.interpolation;

import graphql.GraphQLError;

import java.util.Map;

public interface MessageInterpolator {

    GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams);
}
