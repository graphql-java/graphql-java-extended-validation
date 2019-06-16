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

    /**
     * @return the name of the constraint
     */
    String getName();

    /**
     * @return a description for documentation
     */
    String getDescription();

    /**
     * @return a description for documentation
     */
    String getExample();

    /**
     * @return the graphql SDL directive declaration syntax
     */
    String getDirectiveDeclarationSDL();

    /**
     * @return the message template name
     */
    String getMessageTemplate();

    /**
     * @return a list of the names of applicable types
     */
    List<String> getApplicableTypeNames();

}
