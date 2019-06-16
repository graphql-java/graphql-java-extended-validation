package graphql.validation.rules;

import graphql.PublicApi;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;

import java.util.Objects;
import java.util.StringJoiner;

@PublicApi
public class ValidationCoordinates {

    private final String containerType;
    private final String fieldName;
    private final String argName;

    public ValidationCoordinates(String containerType, String fieldName, String argName) {
        this.containerType = Objects.requireNonNull(containerType);
        this.fieldName = Objects.requireNonNull(fieldName);
        this.argName = argName;
    }

    public String getContainerType() {
        return containerType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getArgName() {
        return argName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValidationCoordinates that = (ValidationCoordinates) o;

        return Objects.equals(this.getContainerType(), that.getContainerType()) && Objects.equals(this.getFieldName(), that.getFieldName()) && Objects.equals(this.getArgName(), that.getArgName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContainerType(), getFieldName(), getArgName());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("containerType=" + getContainerType())
                .add("fieldName=" + getFieldName())
                .add("argName=" + getArgName())
                .toString();
    }


    public static ValidationCoordinates newCoordinates(GraphQLFieldsContainer fieldsContainer, GraphQLFieldDefinition fieldDefinition, GraphQLArgument fieldArg) {
        return new ValidationCoordinates(
                fieldsContainer.getName(),
                fieldDefinition.getName(),
                fieldArg.getName()
        );
    }

    public static ValidationCoordinates newCoordinates(GraphQLFieldsContainer fieldsContainer, GraphQLFieldDefinition fieldDefinition) {
        return new ValidationCoordinates(
                fieldsContainer.getName(),
                fieldDefinition.getName(),
                null
        );
    }

    public static ValidationCoordinates newCoordinates(String fieldsContainer, String fieldDefinition, String fieldArg) {
        return new ValidationCoordinates(
                fieldsContainer,
                fieldDefinition,
                fieldArg
        );
    }

    public static ValidationCoordinates newCoordinates(String fieldsContainer, String fieldDefinition) {
        return new ValidationCoordinates(
                fieldsContainer,
                fieldDefinition,
                null
        );
    }
}
