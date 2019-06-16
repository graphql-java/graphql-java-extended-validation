package graphql.validation.util;

import graphql.Assert;
import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class Util {

    /**
     * This will unwrap one level of List ness and ALL levels of NonNull ness.
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

    public static Object mkDFRFromFetchedResult(List<GraphQLError> errors, Object value) {
        if (value instanceof CompletionStage) {
            return ((CompletionStage<?>) value).thenApply(v -> mkDFRFromFetchedResult(errors, v));
        } else if (value instanceof DataFetcherResult) {
            DataFetcherResult df = (DataFetcherResult) value;
            return mkDFR(df.getData(), concat(errors, df.getErrors()), df.getLocalContext());
        } else {
            return mkDFR(value, errors, null);
        }
    }

    public static DataFetcherResult<Object> mkDFR(Object value, List<GraphQLError> errors, Object localContext) {
        return DataFetcherResult.newResult().data(value).errors(errors).localContext(localContext).build();
    }

    public static <T> List<T> concat(List<T> l1, List<T> l2) {
        List<T> errors = new ArrayList<>();
        errors.addAll(l1);
        errors.addAll(l2);
        return errors;
    }


    public static ExecutionPath concatPaths(ExecutionPath parent, ExecutionPath child) {
        if (child == null) {
            return parent;
        }
        List<Object> segments = child.toList();
        for (Object segment : segments) {
            if (segment instanceof Integer) {
                parent = parent.segment(((Integer) segment));
            } else {
                parent = parent.segment((String.valueOf(segment)));
            }
        }
        return parent;
    }
}
