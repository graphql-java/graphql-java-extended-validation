package graphql.validation.rules;

import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.validation.interpolation.MessageInterpolator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static graphql.Assert.assertNotNull;

public class ValidationRuleEnvironment {

    private final GraphQLFieldsContainer fieldsContainer;
    private final GraphQLFieldDefinition fieldDefinition;
    private final GraphQLArgument argument;
    private final GraphQLInputType fieldOrArgumentType;
    private final Object fieldOrArgumentValue;
    private final ExecutionPath fieldOrArgumentPath;
    private final ExecutionPath executionPath;
    private final SourceLocation location;
    private final MessageInterpolator interpolator;
    private final Map<Class, Object> contextMap;

    private ValidationRuleEnvironment(Builder builder) {
        this.argument = builder.argument;
        this.interpolator = builder.interpolator;
        this.fieldsContainer = builder.fieldsContainer;
        this.fieldDefinition = builder.fieldDefinition;
        this.executionPath = builder.executionPath;
        this.location = builder.location;
        this.fieldOrArgumentValue = builder.fieldOrArgumentValue;
        this.fieldOrArgumentPath = builder.fieldOrArgumentPath;
        this.fieldOrArgumentType = builder.fieldOrArgumentType;
        this.contextMap = builder.contextMap;
    }

    public static Builder newValidationRuleEnvironment() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextObject(Class<T> clazz, Object... defaultVal) {
        return (T) contextMap.getOrDefault(clazz, defaultVal.length == 0 ? null : defaultVal[0]);
    }

    public GraphQLFieldsContainer getFieldsContainer() {
        return fieldsContainer;
    }

    public GraphQLFieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public GraphQLArgument getArgument() {
        return argument;
    }

    public ExecutionPath getExecutionPath() {
        return executionPath;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public ExecutionPath getFieldOrArgumentPath() {
        return fieldOrArgumentPath;
    }

    public GraphQLInputType getFieldOrArgumentType() {
        return fieldOrArgumentType;
    }

    public Object getFieldOrArgumentValue() {
        return fieldOrArgumentValue;
    }

    public MessageInterpolator getInterpolator() {
        return interpolator;
    }


    public ValidationRuleEnvironment transform(Consumer<Builder> builderConsumer) {
        Builder builder = newValidationRuleEnvironment().validationRuleEnvironment(this);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static class Builder {
        private GraphQLFieldsContainer fieldsContainer;
        private GraphQLFieldDefinition fieldDefinition;
        private GraphQLArgument argument;
        private GraphQLInputType fieldOrArgumentType;
        private Object fieldOrArgumentValue;
        private ExecutionPath executionPath;
        private ExecutionPath fieldOrArgumentPath = ExecutionPath.rootPath();
        private SourceLocation location;
        private MessageInterpolator interpolator;
        private final Map<Class, Object> contextMap = new HashMap<>();


        public Builder validationRuleEnvironment(ValidationRuleEnvironment ruleEnvironment) {
            this.fieldsContainer = ruleEnvironment.fieldsContainer;
            this.fieldDefinition = ruleEnvironment.fieldDefinition;
            this.argument = ruleEnvironment.argument;
            this.fieldOrArgumentValue = ruleEnvironment.fieldOrArgumentValue;
            this.fieldOrArgumentType = ruleEnvironment.fieldOrArgumentType;
            this.executionPath = ruleEnvironment.executionPath;
            this.fieldOrArgumentPath = ruleEnvironment.fieldOrArgumentPath;
            this.location = ruleEnvironment.location;
            this.interpolator = ruleEnvironment.interpolator;
            return this;
        }

        public Builder dataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
            fieldsContainer(dataFetchingEnvironment.getExecutionStepInfo().getFieldContainer());
            fieldDefinition(dataFetchingEnvironment.getFieldDefinition());
            executionPath(dataFetchingEnvironment.getExecutionStepInfo().getPath());
            location(dataFetchingEnvironment.getField().getSourceLocation());
            return this;
        }

        public Builder context(Class clazz, Object value) {
            this.contextMap.put(clazz, value);
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

        public Builder argument(GraphQLArgument argument) {
            this.argument = argument;
            fieldOrArgumentType(argument.getType());
            fieldOrArgumentPath(ExecutionPath.rootPath().segment(argument.getName()));
            return this;
        }

        public Builder fieldOrArgumentPath(ExecutionPath fieldOrArgumentPath) {
            this.fieldOrArgumentPath = fieldOrArgumentPath;
            return this;
        }

        public Builder fieldOrArgumentType(GraphQLInputType fieldOrArgumentType) {
            this.fieldOrArgumentType = fieldOrArgumentType;
            return this;
        }

        public Builder fieldOrArgumentValue(Object fieldOrArgumentValue) {
            this.fieldOrArgumentValue = fieldOrArgumentValue;
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
            assertNotNull(argument);
            assertNotNull(fieldOrArgumentType);
            assertNotNull(fieldOrArgumentPath);
            return new ValidationRuleEnvironment(this);
        }
    }
}
