package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Collections;
import java.util.List;

public abstract class AbstractSizeConstraint extends AbstractDirectiveConstraint {
    public AbstractSizeConstraint(String name) {
        super(name);
    }

    @Override
    final protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        GraphQLInputType argType = validationEnvironment.getValidatedType();

        GraphQLAppliedDirective directive = validationEnvironment.getContextObject(GraphQLAppliedDirective.class);
        int min = getIntArg(directive, "min");
        int max = getIntArg(directive, "max");

        int size = getStringOrIDOrObjectOrMapLength(argType, validatedValue);

        if (size < min || size > max) {
            return mkError(validationEnvironment, "min", min, "max", max, "size", size);
        }

        return Collections.emptyList();
    }
}
