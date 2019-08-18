package graphql.validation.rules;

import graphql.GraphQLError;
import graphql.PublicSpi;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;

/**
 * A callback that indicates whether to continue the data fetching after validation errors are detected  and what value should be
 * returned if it decides to not continue.
 * <p>
 * {@link #RETURN_NULL} is a common strategy to use, that is return null as the value for an invalid field
 */
@PublicSpi
public interface OnValidationErrorStrategy {

    /**
     * This strategy will prevent the current data fetch and return null as a value along with the errors
     */
    OnValidationErrorStrategy RETURN_NULL = new OnValidationErrorStrategy() {
        @Override
        public boolean shouldContinue(List<GraphQLError> errors, DataFetchingEnvironment environment) {
            return false;
        }

        @Override
        public Object onErrorValue(List<GraphQLError> errors, DataFetchingEnvironment environment) {
            return DataFetcherResult.newResult().errors(errors).data(null).build();
        }
    };

    /**
     * This is called when there are validation errors present and it can decide whether to continue the current
     * data fetch (and hence return null) or whether it should in fact continue on anyway.
     *
     * @param errors      the list errors
     * @param environment the environment in play
     * @return true if the current data fetch should continue
     */
    boolean shouldContinue(List<GraphQLError> errors, DataFetchingEnvironment environment);

    /**
     * This will be called to generate a value that should be returned if we decide NOT to continue via {@link #shouldContinue(java.util.List, graphql.schema.DataFetchingEnvironment)}.
     *
     * @param errors      the list errors
     * @param environment the environment in play
     * @return an object (a sensible value would be null)
     */
    Object onErrorValue(List<GraphQLError> errors, DataFetchingEnvironment environment);
}
