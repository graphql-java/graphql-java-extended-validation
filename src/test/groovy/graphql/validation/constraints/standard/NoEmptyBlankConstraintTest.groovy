package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class NoEmptyBlankConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "not blank rule constraints"() {

        DirectiveConstraint ruleUnderTest = new NotBlankRule()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                       | argVal     | expectedMessage
        // strings
        'field( arg : String @NotBlank ) : ID' | "\t\n\r "  | 'NotBlank;path=/arg;val:\t\n\r ;\t'
        'field( arg : String @NotBlank ) : ID' | ""         | 'NotBlank;path=/arg;val:;\t'
        'field( arg : String @NotBlank ) : ID' | "\t\n\r X" | ''
        'field( arg : String @NotBlank ) : ID' | null       | ''

        // IDs
        'field( arg : ID @NotBlank ) : ID' | "\t\n\r "  | 'NotBlank;path=/arg;val:\t\n\r ;\t'
        'field( arg : ID @NotBlank ) : ID' | ""         | 'NotBlank;path=/arg;val:;\t'
        'field( arg : ID @NotBlank ) : ID' | "\t\n\r X" | ''
        'field( arg : ID @NotBlank ) : ID' | null       | ''
    }

    @Unroll
    def "not empty rule constraints"() {

        DirectiveConstraint ruleUnderTest = new NotEmptyRule()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                            | argVal      | expectedMessage
        // strings
        'field( arg : String @NotEmpty ) : ID'      | ""          | 'NotEmpty;path=/arg;val:;\t'
        'field( arg : String @NotEmpty ) : ID'      | null        | ''
        'field( arg : String @NotEmpty ) : ID'      | "\t\n\r"    | ''
        'field( arg : String @NotEmpty ) : ID'      | "ABC"       | ''

        // IDs
        'field( arg : ID @NotEmpty ) : ID'      | ""          | 'NotEmpty;path=/arg;val:;\t'
        'field( arg : ID @NotEmpty ) : ID'      | null        | ''
        'field( arg : ID @NotEmpty ) : ID'      | "\t\n\r"    | ''
        'field( arg : ID @NotEmpty ) : ID'      | "ABC"       | ''


        // objects
        'field( arg : InputObject @NotEmpty ) : ID' | [:]         | 'NotEmpty;path=/arg;val:[:];\t'
        'field( arg : InputObject @NotEmpty ) : ID' | null        | ''
        'field( arg : InputObject @NotEmpty ) : ID' | [name: "x"] | ''

        // lists
        'field( arg : [String] @NotEmpty ) : ID'    | []          | 'NotEmpty;path=/arg;val:[];\t'
        'field( arg : [String] @NotEmpty ) : ID'    | null        | ''
        'field( arg : [String] @NotEmpty ) : ID'    | ["x"]       | ''
        'field( arg : [ID] @NotEmpty ) : ID'    | []          | 'NotEmpty;path=/arg;val:[];\t'
        'field( arg : [ID] @NotEmpty ) : ID'    | null        | ''
        'field( arg : [ID] @NotEmpty ) : ID'    | ["x"]       | ''

    }

}