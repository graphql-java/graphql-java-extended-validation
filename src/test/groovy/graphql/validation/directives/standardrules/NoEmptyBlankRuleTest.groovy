package graphql.validation.directives.standardrules


import graphql.validation.directives.BaseDirectiveRuleTest
import graphql.validation.directives.DirectiveValidationRule
import spock.lang.Unroll

class NoEmptyBlankRuleTest extends BaseDirectiveRuleTest {


    @Unroll
    def "not blank rule constraints"() {

        DirectiveValidationRule ruleUnderTest = new NotBlankRule()

        expect:

        def errors = runRules(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                       | argVal     | expectedMessage
        'field( arg : String @NotBlank ) : ID' | "\t\n\r "  | 'NotBlank;path=/arg;val:\t\n\r ;\t'
        'field( arg : String @NotBlank ) : ID' | ""         | 'NotBlank;path=/arg;val:;\t'
        'field( arg : String @NotBlank ) : ID' | "\t\n\r X" | ''

        // nulls are INVALID
        'field( arg : String @NotBlank ) : ID' | null       | 'NotBlank;path=/arg;val:null;\t'
    }

    @Unroll
    def "not empty rule constraints"() {

        DirectiveValidationRule ruleUnderTest = new NotEmptyRule()

        expect:

        def errors = runRules(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                            | argVal      | expectedMessage
        // strings
        'field( arg : String @NotEmpty ) : ID'      | ""          | 'NotEmpty;path=/arg;val:;\t'
        'field( arg : String @NotEmpty ) : ID'      | null        | 'NotEmpty;path=/arg;val:null;\t'
        'field( arg : String @NotEmpty ) : ID'      | "\t\n\r"    | ''
        'field( arg : String @NotEmpty ) : ID'      | "ABC"       | ''

        // objects
        'field( arg : InputObject @NotEmpty ) : ID' | [:]         | 'NotEmpty;path=/arg;val:[:];\t'
        'field( arg : InputObject @NotEmpty ) : ID' | null        | 'NotEmpty;path=/arg;val:null;\t'
        'field( arg : InputObject @NotEmpty ) : ID' | [name: "x"] | ''

        // lists
        'field( arg : [String] @NotEmpty ) : ID'    | []          | 'NotEmpty;path=/arg;val:[];\t'
        'field( arg : [String] @NotEmpty ) : ID'    | null        | 'NotEmpty;path=/arg;val:null;\t'
        'field( arg : [String] @NotEmpty ) : ID'    | ["x"]       | ''

    }

}