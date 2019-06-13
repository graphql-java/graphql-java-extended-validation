package graphql.validation.directives;

import graphql.GraphQLError;
import graphql.schema.GraphQLInputType;
import graphql.validation.rules.ValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;

public interface DirectiveValidationRule extends ValidationRule {

    String getName();

    String getDirectiveDeclarationSDL();

    boolean appliesToType(GraphQLInputType inputType);

    @Override
    default List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        return Collections.emptyList();
    }
}
