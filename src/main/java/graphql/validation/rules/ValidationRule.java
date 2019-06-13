package graphql.validation.rules;

import graphql.GraphQLError;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;

import java.util.List;

public interface ValidationRule {

    boolean appliesToType(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer);

    List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment);
}
