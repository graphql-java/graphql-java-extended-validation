package graphql.validation.directives;

import graphql.GraphQLError;
import graphql.PublicSpi;
import graphql.validation.rules.ValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.List;

/**
 * A DirectiveValidationRule is a specialised form of validation rule
 * that assumes it is backed by a SDL directive on fields, field arguments
 * or input type fields.
 */
@PublicSpi
public interface DirectiveValidationRule extends ValidationRule {

    String getName();

    String getDescription();

    String getDirectiveDeclarationSDL();

    List<String> getApplicableTypeNames();

    @Override
    default List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        return Collections.emptyList();
    }
}
