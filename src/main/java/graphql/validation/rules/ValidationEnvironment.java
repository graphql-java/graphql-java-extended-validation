package graphql.validation.rules;

import graphql.PublicApi;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.validation.interpolation.MessageInterpolator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The environment in which validation runs
 */
@PublicApi
public class ValidationEnvironment {

    /**
     * The type of element being validated
     */
    public enum ValidatedElement {
        /**
         * A output field is being validated
         */
        FIELD,
        /**
         * An argument on a graphql output field is being validated
         */
        ARGUMENT,
        /**
         * A input type field is being validated
         */
        INPUT_OBJECT_FIELD
    }

    private final GraphQLFieldsContainer fieldsContainer;
    private final GraphQLFieldDefinition fieldDefinition;
    private final GraphQLArgument argument;
    private final ExecutionPath executionPath;
    private final ExecutionPath validatedPath;
    private final SourceLocation location;
    private final MessageInterpolator interpolator;
    private final Map<Class, Object> contextMap;
    private final Locale locale;
    private final Map<String, Object> argumentValues;
    private final Object validatedValue;
    private final GraphQLInputType validatedType;
    private final ValidatedElement validatedElement;
    private final List<GraphQLDirective> directives;

    private ValidationEnvironment(Builder builder) {
        this.argument = builder.argument;
        this.argumentValues = Collections.unmodifiableMap(builder.argumentValues);
        this.contextMap = Collections.unmodifiableMap(builder.contextMap);
        this.fieldDefinition = builder.fieldDefinition;
        this.executionPath = builder.executionPath;
        this.validatedPath = builder.validatedPath;
        this.validatedType = builder.validatedType;
        this.fieldsContainer = builder.fieldsContainer;
        this.interpolator = builder.interpolator;
        this.locale = builder.locale;
        this.location = builder.location;
        this.validatedValue = builder.validatedValue;
        this.validatedElement = builder.validatedElement;
        this.directives = builder.directives;
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

    public SourceLocation getLocation() {
        return location;
    }

    public ExecutionPath getValidatedPath() {
        return validatedPath;
    }

    public ExecutionPath getExecutionPath() {
        return executionPath;
    }

    public GraphQLInputType getValidatedType() {
        return validatedType;
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

    public ValidatedElement getValidatedElement() {
        return validatedElement;
    }

    public List<GraphQLDirective> getDirectives() {
        return directives;
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
        private GraphQLFieldDefinition fieldDefinition;
        private ExecutionPath validatedPath = ExecutionPath.rootPath();
        private ExecutionPath executionPath;
        private GraphQLFieldsContainer fieldsContainer;
        private MessageInterpolator interpolator;
        private Locale locale;
        private SourceLocation location;
        private Object validatedValue;
        private GraphQLInputType validatedType;
        private ValidatedElement validatedElement;
        private List<GraphQLDirective> directives = Collections.emptyList();

        public Builder validationEnvironment(ValidationEnvironment validationEnvironment) {
            this.argument = validationEnvironment.argument;
            this.argumentValues = validationEnvironment.argumentValues;
            this.contextMap.putAll(validationEnvironment.contextMap);
            this.fieldDefinition = validationEnvironment.fieldDefinition;
            this.executionPath = validationEnvironment.executionPath;
            this.validatedPath = validationEnvironment.validatedPath;
            this.validatedType = validationEnvironment.validatedType;
            this.fieldsContainer = validationEnvironment.fieldsContainer;
            this.interpolator = validationEnvironment.interpolator;
            this.locale = validationEnvironment.locale;
            this.location = validationEnvironment.location;
            this.validatedValue = validationEnvironment.validatedValue;
            this.validatedElement = validationEnvironment.validatedElement;
            this.directives = validationEnvironment.directives;
            return this;
        }

        public Builder dataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
            fieldsContainer(dataFetchingEnvironment.getExecutionStepInfo().getFieldContainer());
            fieldDefinition(dataFetchingEnvironment.getFieldDefinition());
            directives(dataFetchingEnvironment.getFieldDefinition().getDirectives());
            executionPath(dataFetchingEnvironment.getExecutionStepInfo().getPath());
            validatedPath(dataFetchingEnvironment.getExecutionStepInfo().getPath());
            location(dataFetchingEnvironment.getField().getSourceLocation());
            argumentValues(dataFetchingEnvironment.getArguments());
            validatedElement(ValidatedElement.FIELD);
            return this;
        }

        public Builder argument(GraphQLArgument argument) {
            this.argument = argument;
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

        public Builder executionPath(ExecutionPath executionPath) {
            this.executionPath = executionPath;
            return this;
        }

        public Builder fieldDefinition(GraphQLFieldDefinition fieldDefinition) {
            this.fieldDefinition = fieldDefinition;
            return this;
        }

        public Builder validatedElement(ValidatedElement validatedElement) {
            this.validatedElement = validatedElement;
            return this;
        }

        public Builder validatedType(GraphQLInputType validatedType) {
            this.validatedType = validatedType;
            return this;
        }

        public Builder validatedValue(Object validatedValue) {
            this.validatedValue = validatedValue;
            return this;
        }

        public Builder validatedPath(ExecutionPath validatedPath) {
            this.validatedPath = validatedPath;
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

        public Builder directives(List<GraphQLDirective> directives) {
            this.directives = directives;
            return this;
        }

        public ValidationEnvironment build() {
            return new ValidationEnvironment(this);
        }
    }
}
