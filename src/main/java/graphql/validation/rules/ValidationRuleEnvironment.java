package graphql.validation.rules;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeUtil;
import graphql.validation.interpolation.MessageInterpolator;

public class ValidationRuleEnvironment {

    private final DataFetchingEnvironment dataFetchingEnvironment;
    private final GraphQLArgument argument;
    private final Object argumentValue;
    private final MessageInterpolator interpolator;

    private ValidationRuleEnvironment(Builder builder) {
        this.dataFetchingEnvironment = builder.dataFetchingEnvironment;
        this.argument = builder.argument;
        this.argumentValue = builder.argumentValue;
        this.interpolator = builder.interpolator;
    }

    public static Builder newValidationRuleEnvironment() {
        return new Builder();
    }

    public GraphQLFieldsContainer getFieldsContainer() {
        return dataFetchingEnvironment.getExecutionStepInfo().getFieldContainer();
    }

    public GraphQLFieldDefinition getFieldDefinition() {
        return dataFetchingEnvironment.getExecutionStepInfo().getFieldDefinition();
    }

    public GraphQLArgument getArgument() {
        return argument;
    }

    public GraphQLInputType getArgumentType() {
        return argument.getType();
    }

    public GraphQLInputType getUnwrappedArgumentType() {
        return (GraphQLInputType) GraphQLTypeUtil.unwrapAll(argument.getType());
    }

    public Object getArgumentValue() {
        return argumentValue;
    }

    public DataFetchingEnvironment getDataFetchingEnvironment() {
        return dataFetchingEnvironment;
    }

    public MessageInterpolator getInterpolator() {
        return interpolator;
    }

    public static class Builder {
        private DataFetchingEnvironment dataFetchingEnvironment;
        private GraphQLArgument argument;
        private Object argumentValue;
        private MessageInterpolator interpolator;

        public Builder dataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
            this.dataFetchingEnvironment = dataFetchingEnvironment;
            return this;
        }

        public Builder argument(GraphQLArgument argument) {
            this.argument = argument;
            return this;
        }

        public Builder argumentValue(Object argValue) {
            this.argumentValue = argValue;
            return this;
        }

        public Builder messageInterpolator(MessageInterpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        public ValidationRuleEnvironment build() {
            return new ValidationRuleEnvironment(this);
        }
    }
}
