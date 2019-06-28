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
     * This is called to runs the rule.  A rule maybe invoked MULTIPLE times per field.  It will be invoked
     * once for the original field, then invoked for each of the arguments on a field and then if the input types are complex ones
     * such as {@link graphql.schema.GraphQLInputObjectType} then it will be invoked for each attribute of that
     * type.
     * <p>
     * A rule should consult {@link ValidationEnvironment#getValidatedElement()} to check
     * what element is being validated.  If the rule does not handle that type of element,
     * simply return an empty list of errors.
     *
     * @param validationEnvironment the validation environment
     *
     * @return a non null list of errors where emptyList() means its valid
     */
    List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment);
}
