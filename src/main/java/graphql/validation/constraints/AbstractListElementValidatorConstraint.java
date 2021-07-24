package graphql.validation.constraints;

import graphql.GraphQLError;
import graphql.schema.GraphQLInputType;
import graphql.validation.rules.ValidationEnvironment;
import java.util.List;

public abstract class AbstractListElementValidatorConstraint extends AbstractDirectiveConstraint {
    private final GraphQLListElementValidator validator = new GraphQLListElementValidator();

    public AbstractListElementValidatorConstraint(String name) {
        super(name);
    }

    @Override
    final protected boolean appliesToType(GraphQLInputType inputType) {
        return validator.appliesToType(inputType, this::appliesToTypeOrListElement);
    }

    protected abstract boolean appliesToTypeOrListElement(GraphQLInputType inputType);

    @Override
    final protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        return validator.runConstraintOnListElements(validationEnvironment, this::runConstraintOnElement);
    }

    protected abstract List<GraphQLError> runConstraintOnElement(ValidationEnvironment validationEnvironment);
}
