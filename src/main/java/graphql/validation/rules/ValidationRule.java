package graphql.validation.rules;

import graphql.GraphQLError;
import graphql.PublicSpi;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;

import java.util.List;

/**
 * A validation rule is code that can be applied inside a {@link graphql.validation.rules.ValidationRuleEnvironment} and produce
 * a list of zero or more {@link graphql.GraphQLError}s as validation.
 */
@PublicSpi
public interface ValidationRule {

    boolean appliesToType(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer);

    List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment);
}
