package graphql.validation.rules;

import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeUtil;
import graphql.validation.interpolation.MessageInterpolator;

public class ValidationRuleEnvironment {

    private final GraphQLFieldsContainer fieldsContainer;
    private final GraphQLFieldDefinition fieldDefinition;
    private final GraphQLArgument argument;
    private final Object argumentValue;
    private final ExecutionPath executionPath;
    private final SourceLocation location;
    private final MessageInterpolator interpolator;

    private ValidationRuleEnvironment(Builder builder) {
        this.argument = builder.argument;
        this.argumentValue = builder.argumentValue;
        this.interpolator = builder.interpolator;
        this.fieldsContainer = builder.fieldsContainer;
        this.fieldDefinition = builder.fieldDefinition;
        this.executionPath = builder.executionPath;
        this.location = builder.location;
    }

    public static Builder newValidationRuleEnvironment() {
        return new Builder();
    }

    public GraphQLFieldsContainer getFieldsContainer() {
        return fieldsContainer;
    }

    public GraphQLFieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public ExecutionPath getExecutionPath() {
        return executionPath;
    }

    public SourceLocation getLocation() {
        return location;
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

    public MessageInterpolator getInterpolator() {
        return interpolator;
    }

    public static class Builder {
        private GraphQLFieldsContainer fieldsContainer;
        private GraphQLFieldDefinition fieldDefinition;
        private GraphQLArgument argument;
        private Object argumentValue;
        private ExecutionPath executionPath;
        private SourceLocation location;
        private MessageInterpolator interpolator;

        public Builder dataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
            fieldsContainer(dataFetchingEnvironment.getExecutionStepInfo().getFieldContainer());
            fieldDefinition(dataFetchingEnvironment.getFieldDefinition());
            executionPath(dataFetchingEnvironment.getExecutionStepInfo().getPath());
            location(dataFetchingEnvironment.getField().getSourceLocation());
            return this;
        }

        public Builder argumentValue(Object argValue) {
            this.argumentValue = argValue;
            return this;
        }

        public Builder argument(GraphQLArgument argument) {
            this.argument = argument;
            return this;
        }

        public Builder fieldsContainer(GraphQLFieldsContainer fieldsContainer) {
            this.fieldsContainer = fieldsContainer;
            return this;
        }

        public Builder fieldDefinition(GraphQLFieldDefinition fieldDefinition) {
            this.fieldDefinition = fieldDefinition;
            return this;
        }

        public Builder executionPath(ExecutionPath executionPath) {
            this.executionPath = executionPath;
            return this;
        }

        public Builder location(SourceLocation location) {
            this.location = location;
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
