package graphql.validation.rules;

import graphql.PublicApi;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;

import java.util.Objects;
import java.util.StringJoiner;

@PublicApi
public class ArgumentCoordinates {

    private final String containerType;
    private final String fieldName;
    private final String argName;

    public ArgumentCoordinates(String containerType, String fieldName, String argName) {
        this.containerType = Objects.requireNonNull(containerType);
        this.fieldName = Objects.requireNonNull(fieldName);
        this.argName = Objects.requireNonNull(argName);
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

        ArgumentCoordinates that = (ArgumentCoordinates) o;

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


    public static ArgumentCoordinates newArgumentCoordinates(GraphQLFieldsContainer fieldsContainer, GraphQLFieldDefinition fieldDefinition, GraphQLArgument fieldArg) {
        return new ArgumentCoordinates(
                fieldsContainer.getName(),
                fieldDefinition.getName(),
                fieldArg.getName()
        );
    }

    public static ArgumentCoordinates newArgumentCoordinates(String fieldsContainer, String fieldDefinition, String fieldArg) {
        return new ArgumentCoordinates(
                fieldsContainer,
                fieldDefinition,
                fieldArg
        );
    }
}
