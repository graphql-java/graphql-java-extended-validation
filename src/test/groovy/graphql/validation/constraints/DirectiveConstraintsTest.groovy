package graphql.validation.constraints

import graphql.AssertException
import graphql.validation.constraints.standard.SizeConstraint
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Unroll

class DirectiveConstraintsTest extends BaseConstraintTestSupport {

    def "basic building"() {
        when:
        def rules = DirectiveConstraints.newDirectiveConstraints().build()

        then:
        rules.getConstraints().size() == 19

        when:
        rules = DirectiveConstraints.newDirectiveConstraints().clearRules().build()

        then:
        rules.getConstraints().size() == 0
    }


    @Unroll
    def "can check that it applies to arg types"() {

        def extraSDL = '''
            input ProductItem {
                code : String @Size(max : 5)
                price : String @Size(max : 3)
            }

            input Product {
                name : String @Size(max : 7)
                items : [ProductItem!]! # crazy nulls
                crazyItems : [[[ProductItem!]!]] # nuts but can we handle it
            }

            input NoSizeDirectives {
                age : String @Range(max : 7)
            }

        '''

        def directiveValidationRules = DirectiveConstraints.newDirectiveConstraints().build()

        expect:

        def schema = buildSchema(directiveValidationRules.getDirectivesSDL(), fieldDeclaration, extraSDL)

        ValidationEnvironment validationEnvironment = buildEnv("Size", schema, "testArg", null)

        def argument = validationEnvironment.getArgument()
        def fieldDefinition = validationEnvironment.getFieldDefinition()
        def container = validationEnvironment.getFieldsContainer()
        def appliesTo = directiveValidationRules.whichApplyTo(argument, fieldDefinition, container)

        def names = appliesTo.collect({ it.getName() }).sort().join(",")

        assert expected == names, "expected " + expected + " for " + fieldDeclaration + " but got " + names

        where:

        fieldDeclaration                                                | expected
        "field( testArg : String ) : ID"                                | ""

        "field( testArg : String @Size(max : 2) @Range(min : 10)) : ID" | "Range,Size"

        "field( testArg : NoSizeDirectives ) : ID"                      | "Range"

        "field( testArg : [Product!] @ContainerSize(max : 2) ) : ID"    | "ContainerSize,Size"
        "field( testArg : Product! @ContainerSize(max : 2) ) : ID"      | "ContainerSize,Size"
        "field( testArg : [Product!] ) : ID"                            | "Size"
    }


    @Unroll
    def "exception if constraint on the wrong type"() {


        def directiveValidationRules = DirectiveConstraints.newDirectiveConstraints()
                .clearRules()
                .addRule(new SizeConstraint())
                .build()

        expect:

        def schema = buildSchema(directiveValidationRules.getDirectivesSDL(), fieldDeclaration, "")

        ValidationEnvironment validationEnvironment = buildEnv("Size", schema, "testArg", null)

        def argument = validationEnvironment.getArgument()
        def fieldDefinition = validationEnvironment.getFieldDefinition()
        def container = validationEnvironment.getFieldsContainer()

        try {
            directiveValidationRules.whichApplyTo(argument, fieldDefinition, container)
            assert false, "This should assert"
        } catch (AssertException ignored) {
            println ignored.message
        }

        where:

        fieldDeclaration                              | _
        "field( testArg : Int! @Size(max : 2) ) : ID" | _ // not allowed for Size
    }

    def "can combine as type registry"() {

        def directiveValidationRules = DirectiveConstraints.newDirectiveConstraints()
                .build()

        when:
        def declaration = directiveValidationRules.getDirectivesDeclaration()
        then:
        declaration != null
        declaration.getDirectiveDefinitions().size() == 19

    }
}
