package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class AssertTrueFalseConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "assert true rule constraints"() {

        DirectiveConstraint ruleUnderTest = new AssertTrueConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                             | argVal        | expectedMessage
        'field( arg : Boolean @AssertTrue ) : ID'                    | false         | 'AssertTrue;path=/arg;val:false;\t'
        'field( arg : Boolean @AssertTrue ): ID'                     | true          | ''

        'field( arg : Boolean @AssertTrue(message : "custom")) : ID' | false         | 'custom;path=/arg;val:false;\t'

        // nulls are valid
        'field( arg : Boolean @AssertTrue ) : ID'                    | null          | ''

        // Lists
        'field( arg : [Boolean] @AssertTrue ) : ID'                  | [true, true]  | ''
        'field( arg : [Boolean] @AssertTrue ) : ID'                  | [true, false] | 'AssertTrue;path=/arg[1];val:false;\t'
        'field( arg : [Boolean] @AssertTrue ) : ID'                  | [null]        | ''
    }

    @Unroll
    def "assert false rule constraints"() {

        DirectiveConstraint ruleUnderTest = new AssertFalseConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                              | argVal         | expectedMessage
        'field( arg : Boolean @AssertFalse ) : ID'                    | true           | 'AssertFalse;path=/arg;val:true;\t'
        'field( arg : Boolean @AssertFalse ): ID'                     | false          | ''

        'field( arg : Boolean @AssertFalse(message : "custom")) : ID' | true           | 'custom;path=/arg;val:true;\t'

        // nulls are valid
        'field( arg : Boolean @AssertFalse ) : ID'                    | null           | ''

        // Lists
        'field( arg : [Boolean] @AssertFalse ) : ID'                  | [false, false] | ''
        'field( arg : [Boolean] @AssertFalse ) : ID'                  | [false, true]  | 'AssertFalse;path=/arg[1];val:true;\t'
        'field( arg : [Boolean] @AssertFalse ) : ID'                  | [null]         | ''
    }
}