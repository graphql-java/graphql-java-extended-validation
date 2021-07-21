package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;
import java.util.Collections;
import java.util.List;
import static graphql.Scalars.GraphQLBoolean;

abstract class AbstractAssertConstraint extends AbstractDirectiveConstraint {

    public AbstractAssertConstraint(String name) {
        super(name);
    }


    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, GraphQLBoolean);
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        boolean isTrue = asBoolean(validatedValue);

        if (!isOK(isTrue)) {
            return mkError(validationEnvironment);
        }

        return Collections.emptyList();
    }

    protected abstract boolean isOK(boolean isTrue);


}
