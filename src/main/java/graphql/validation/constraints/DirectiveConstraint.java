package graphql.validation.constraints;

import graphql.PublicSpi;
import graphql.validation.rules.ValidationRule;

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
     * @return documentation meta data about this constraint
     */
    Documentation getDocumentation();

}
