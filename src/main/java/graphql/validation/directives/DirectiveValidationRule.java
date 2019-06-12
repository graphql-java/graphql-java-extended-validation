package graphql.validation.directives;

import graphql.GraphQLError;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.validation.rules.ValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;

public interface DirectiveValidationRule extends ValidationRule {

    String getName();

    @Override
    default boolean appliesToArgument(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return false;
    }

    @Override
    default List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        return Collections.emptyList();
    }
}
