package graphql.validation.constraints;

import graphql.PublicSpi;
import graphql.validation.rules.ValidationRule;

import java.util.List;

/**
 * A DirectiveConstraint is a specialised form of validation rule
 * that assumes it is backed by a SDL directive on fields, field arguments
 * or input type fields.
 */
@PublicSpi
public interface DirectiveConstraint extends ValidationRule {

    String getName();

    String getDescription();

    String getDirectiveDeclarationSDL();

    String getMessageTemplate();

    List<String> getApplicableTypeNames();

}
