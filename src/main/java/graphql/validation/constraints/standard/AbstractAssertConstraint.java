package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.Scalars.GraphQLBoolean;

abstract class AbstractAssertConstraint extends AbstractDirectiveConstraint {

    public AbstractAssertConstraint(String name) {
        super(name);
    }


    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                GraphQLBoolean
        );
    }

    @Override
    public List<String> getApplicableTypeNames() {
        return Stream.of(GraphQLBoolean).map(GraphQLScalarType::getName).collect(Collectors.toList());
    }

    @Override
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        //null values are valid
        if (validatedValue == null) {
            return Collections.emptyList();
        }

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);

        boolean isTrue = asBoolean(validatedValue);
        if (!isOK(isTrue)) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment));

        }
        return Collections.emptyList();
    }

    protected abstract boolean isOK(boolean isTrue);


}
