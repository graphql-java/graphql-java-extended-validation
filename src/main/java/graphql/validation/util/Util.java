package graphql.validation.util;

import graphql.Assert;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;

public class Util {

    /**
     * This will unwrap one level of List typeness and ALL levels of NonNull ness.
     *
     * @param inputType the type to unwrap
     *
     * @return an input type
     */
    public static GraphQLInputType unwrapOneAndAllNonNull(GraphQLInputType inputType) {
        GraphQLType type = GraphQLTypeUtil.unwrapNonNull(inputType);
        type = GraphQLTypeUtil.unwrapOne(type); // one level
        type = GraphQLTypeUtil.unwrapNonNull(type);
        if (type instanceof GraphQLInputType) {
            return (GraphQLInputType) type;
        } else {
            String argType = GraphQLTypeUtil.simplePrint(inputType);
            return Assert.assertShouldNeverHappen("You have a wrapped type that is in fact not a input type : %s", argType);
        }
    }

    public static GraphQLInputType unwrapNonNull(GraphQLInputType inputType) {
        GraphQLType type = GraphQLTypeUtil.unwrapNonNull(inputType);
        if (type instanceof GraphQLInputType) {
            return (GraphQLInputType) type;
        } else {
            String argType = GraphQLTypeUtil.simplePrint(inputType);
            return Assert.assertShouldNeverHappen("You have a wrapped type that is in fact not a input type : %s", argType);
        }
    }
}
