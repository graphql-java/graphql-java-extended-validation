package graphql.validation.rules;

import graphql.GraphQLError;
import graphql.PublicSpi;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;

import java.util.List;

/**
 * A validation rule is code that can be applied inside a {@link ValidationEnvironment} and produce
 * a list of zero or more {@link graphql.GraphQLError}s as validation.
 */
@PublicSpi
public interface ValidationRule {

    /**
     * This is called to work out if this rule applies to a specified field
     *
     * @param fieldDefinition the field to check
     * @param fieldsContainer the field container
     *
     * @return true if this rule applies to the field
     */
    boolean appliesTo(GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer);

    /**
     * This is called to work out if this rule applies to the argument of a specified field
     *
     * @param argument        the argument to check
     * @param fieldDefinition the field to check
     * @param fieldsContainer the field container
     *
     * @return true if this rule applies to the argument of the field field
     */
    boolean appliesTo(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer);

    /**
     * This is called to runs the rule
     *
     * @param validationEnvironment the validation environment
     *
     * @return a non null list of errors where emptyList() means its valid
     */
    List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment);
}
