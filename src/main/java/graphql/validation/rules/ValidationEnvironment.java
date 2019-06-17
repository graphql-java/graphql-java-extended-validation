package graphql.validation.rules;

import graphql.PublicApi;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.validation.interpolation.MessageInterpolator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

@PublicApi
public class ValidationEnvironment {

    private final GraphQLFieldsContainer fieldsContainer;
    private final GraphQLFieldDefinition fieldDefinition;
    private final GraphQLArgument argument;
    private final GraphQLInputType fieldOrArgumentType;
    private final Object validatedValue;
    private final ExecutionPath fieldOrArgumentPath;
    private final ExecutionPath executionPath;
    private final SourceLocation location;
    private final MessageInterpolator interpolator;
    private final Map<Class, Object> contextMap;
    private final Locale locale;
    private final Map<String, Object> argumentValues;

    private ValidationEnvironment(Builder builder) {
        this.argument = builder.argument;
        this.argumentValues = Collections.unmodifiableMap(builder.argumentValues);
        this.contextMap = Collections.unmodifiableMap(builder.contextMap);
        this.executionPath = builder.executionPath;
        this.fieldDefinition = builder.fieldDefinition;
        this.fieldOrArgumentPath = builder.fieldOrArgumentPath;
        this.fieldOrArgumentType = builder.fieldOrArgumentType;
        this.fieldsContainer = builder.fieldsContainer;
        this.interpolator = builder.interpolator;
        this.locale = builder.locale;
        this.location = builder.location;
        this.validatedValue = builder.validatedValue;
    }

    public static Builder newValidationEnvironment() {
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

    public Object getValidatedValue() {
        return validatedValue;
    }

    public Map<String, Object> getArgumentValues() {
        return argumentValues;
    }

    public MessageInterpolator getInterpolator() {
        return interpolator;
    }

    public Locale getLocale() {
        return locale;
    }

    public ValidationEnvironment transform(Consumer<Builder> builderConsumer) {
        Builder builder = newValidationEnvironment().validationEnvironment(this);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static class Builder {
        private final Map<Class, Object> contextMap = new HashMap<>();
        private GraphQLArgument argument;
        private Map<String, Object> argumentValues = new HashMap<>();
        private ExecutionPath executionPath;
        private GraphQLFieldDefinition fieldDefinition;
        private ExecutionPath fieldOrArgumentPath = ExecutionPath.rootPath();
        private GraphQLInputType fieldOrArgumentType;
        private GraphQLFieldsContainer fieldsContainer;
        private MessageInterpolator interpolator;
        private Locale locale;
        private SourceLocation location;
        private Object validatedValue;

        public Builder validationEnvironment(ValidationEnvironment validationEnvironment) {
            this.argument = validationEnvironment.argument;
            this.argumentValues = validationEnvironment.argumentValues;
            this.contextMap.putAll(validationEnvironment.contextMap);
            this.executionPath = validationEnvironment.executionPath;
            this.fieldDefinition = validationEnvironment.fieldDefinition;
            this.fieldOrArgumentPath = validationEnvironment.fieldOrArgumentPath;
            this.fieldOrArgumentType = validationEnvironment.fieldOrArgumentType;
            this.fieldsContainer = validationEnvironment.fieldsContainer;
            this.interpolator = validationEnvironment.interpolator;
            this.locale = validationEnvironment.locale;
            this.location = validationEnvironment.location;
            this.validatedValue = validationEnvironment.validatedValue;
            return this;
        }

        public Builder dataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
            fieldsContainer(dataFetchingEnvironment.getExecutionStepInfo().getFieldContainer());
            fieldDefinition(dataFetchingEnvironment.getFieldDefinition());
            executionPath(dataFetchingEnvironment.getExecutionStepInfo().getPath());
            location(dataFetchingEnvironment.getField().getSourceLocation());
            argumentValues(dataFetchingEnvironment.getArguments());
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

        public Builder validatedValue(Object validatedValue) {
            this.validatedValue = validatedValue;
            return this;
        }

        public Builder argumentValues(Map<String, Object> argumentValues) {
            this.argumentValues = argumentValues;
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

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public ValidationEnvironment build() {
            return new ValidationEnvironment(this);
        }
    }
}
