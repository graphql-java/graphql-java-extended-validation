package graphql.validation.constraints;

import graphql.schema.GraphQLNamedType;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Documentation {

    private final String description;
    private final String example;
    private final String directiveSDL;
    private final TypeDefinitionRegistry directiveDeclaration;
    private final String messageTemplate;
    private final List<String> applicableTypeNames;

    private Documentation(Builder builder) {
        this.description = builder.description;
        this.example = builder.example;
        this.directiveSDL = builder.directiveSDL;
        this.directiveDeclaration = new SchemaParser().parse(builder.directiveSDL);
        this.messageTemplate = builder.messageTemplate;
        this.applicableTypeNames = builder.applicableTypeNames;
    }

    public static Builder newDocumentation() {
        return new Builder();
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

    public String getDirectiveSDL() {
        return directiveSDL;
    }

    public TypeDefinitionRegistry getDirectiveDeclaration() {
        return directiveDeclaration;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public List<String> getApplicableTypeNames() {
        return applicableTypeNames;
    }

    public static class Builder {
        private String description;
        private String example;
        private String directiveSDL;
        private String messageTemplate;
        private List<String> applicableTypeNames;


        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder example(String example) {
            this.example = example;
            return this;
        }

        public Builder directiveSDL(String format, Object... args) {
            this.directiveSDL = String.format(format, args);
            return this;
        }

        public Builder messageTemplate(String messageTemplate) {
            this.messageTemplate = messageTemplate;
            return this;
        }

        public Builder applicableTypes(List<? extends GraphQLNamedType> applicableTypes) {
            this.applicableTypeNames = applicableTypes.stream().map(GraphQLNamedType::getName).collect(Collectors.toList());
            return this;
        }

        public Builder applicableTypes(GraphQLNamedType... applicableTypes) {
            return applicableTypes(Arrays.asList(applicableTypes));
        }


        public Builder applicableTypeNames(List<String> applicableTypeNames) {
            this.applicableTypeNames = applicableTypeNames;
            return this;
        }

        public Builder applicableTypeNames(String... applicableTypeNames) {
            return applicableTypeNames(Arrays.asList(applicableTypeNames));
        }

        public Documentation build() {
            return new Documentation(this);
        }

    }
}
