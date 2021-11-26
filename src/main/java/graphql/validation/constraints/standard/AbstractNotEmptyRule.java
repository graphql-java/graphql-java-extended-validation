package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;
import java.util.Collections;
import java.util.List;

public abstract class AbstractNotEmptyRule extends AbstractDirectiveConstraint {
    public AbstractNotEmptyRule(String name) {
        super(name);
    }

    @Override
    final protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        GraphQLInputType argumentType = validationEnvironment.getValidatedType();

        int size = getStringOrIDOrObjectOrMapLength(argumentType, validatedValue);

        if (size <= 0) {
            return mkError(validationEnvironment, "size", size);
        }

        return Collections.emptyList();
    }
}
