package graphql.validation.constraints;

import graphql.GraphQLError;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeUtil;
import graphql.validation.rules.ValidationEnvironment;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GraphQLListElementValidator {
    public boolean appliesToType(GraphQLInputType inputType, Function<GraphQLInputType, Boolean> appliesToTypeOrListElement) {
        if (GraphQLTypeUtil.isList(inputType)) {
            return appliesToTypeOrListElement.apply((GraphQLInputType) GraphQLTypeUtil.unwrapAll(inputType));
        }

        return appliesToTypeOrListElement.apply(inputType);
    }

    public List<GraphQLError> runConstraintOnListElements(ValidationEnvironment validationEnvironment, Function<ValidationEnvironment, List<GraphQLError>> runConstraintOnElement) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        if (validatedValue instanceof Collection<?>) {
            final AtomicInteger index = new AtomicInteger(0);
            return ((Collection<?>) validatedValue)
                    .stream()
                    .flatMap((item) -> runConstraintOnElement.apply(validationEnvironment.transform((environment) -> {
                        environment
                                .validatedValue(item)
                                .validatedPath(validationEnvironment.getValidatedPath().segment(index.getAndIncrement()))
                                .validatedType((GraphQLInputType) GraphQLTypeUtil.unwrapAll(validationEnvironment.getValidatedType()));
                    })).stream())
                    .collect(Collectors.toList());
        }

        return runConstraintOnElement.apply(validationEnvironment);
    }
}
