package graphql.validation.constraints

import graphql.AssertException
import graphql.validation.constraints.standard.SizeConstraint
import graphql.validation.rules.ValidationEnvironment
import spock.lang.Unroll

class DirectiveConstraintsTest extends BaseConstraintTest {

    def "basic building"() {
        when:
        def rules = DirectiveConstraints.newDirectiveConstraints().build()

        then:
        rules.getConstraints().size() == 17

        when:
        rules = DirectiveConstraints.newDirectiveConstraints().clearRules().build()

        then:
        rules.getConstraints().size() == 0
    }


    @Unroll
    def "complex object argument constraints"() {

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

        '''

        def directiveValidationRules = DirectiveConstraints.newDirectiveConstraints().build()

        expect:

        def schema = buildSchema(directiveValidationRules.getDirectivesSDL(), fieldDeclaration, extraSDL)

        ValidationEnvironment validationEnvironment = buildEnv("Size", schema, "testArg", argVal)

        def errors = directiveValidationRules.runValidation(validationEnvironment)
        errors.size() == eSize

        where:

        fieldDeclaration                                    | argVal                                               | eSize | expectedMessage
        // size can handle list elements
        "field( testArg : [Product!] @Size(max : 2) ) : ID" | [[:], [:], [:]]                                      | 1     | "graphql.validation.Size.message;path=/testArg;val:[[:], [:], [:]];\t"
        // goes down into input types
        "field( testArg : [Product!] @Size(max : 2) ) : ID" | [[name: "morethan7"], [:]]                           | 1     | "graphql.validation.Size.message;path=/testArg[0]/name;val:morethan7;\t"
        // shows that it traverses down lists
        "field( testArg : [Product!] @Size(max : 2) ) : ID" | [[name: "ok"], [name: "notOkHere"]]                  | 1     | "graphql.validation.Size.message;path=/testArg[1]/name;val:notOkHere;\t"
        // shows that it traverses down lists and objects
        "field( testArg : [Product!] @Size(max : 2) ) : ID" | [[items: [[code: "morethan5", price: "morethan3"]]]] | 2     | "graphql.validation.Size.message;path=/testArg[0]/items[0]/code;val:morethan5;\tgraphql.validation.Size.message;path=/testArg[0]/items[0]/price;val:morethan3;\t"

        // shows that it traverses down crazy lists and objects
        "field( testArg : [Product!] @Size(max : 2) ) : ID" | [[crazyItems: [[[[code: "morethan5"]]]]]]            | 1     | "graphql.validation.Size.message;path=/testArg[0]/crazyItems[0][0][0]/code;val:morethan5;\t"
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

        def directiveValidationRules = DirectiveConstraints.newDirectiveConstraints()
                .clearRules()
                .addRule(new SizeConstraint()) // only size in effect - not Range
                .build()

        expect:

        def schema = buildSchema(directiveValidationRules.getDirectivesSDL(), fieldDeclaration, extraSDL)

        ValidationEnvironment validationEnvironment = buildEnv("Size", schema, "testArg", null)

        def argument = validationEnvironment.getArgument()
        def fieldDefinition = validationEnvironment.getFieldDefinition()
        def container = validationEnvironment.getFieldsContainer()
        def appliesTo = directiveValidationRules.appliesTo(argument, fieldDefinition, container)

        assert expected == appliesTo, "expected " + expected + " for " + fieldDeclaration

        where:

        fieldDeclaration                                    | expected
        "field( testArg : NoSizeDirectives ) : ID"          | false
        "field( testArg : String ) : ID"                    | false

        "field( testArg : [Product!] @Size(max : 2) ) : ID" | true
        "field( testArg : Product! @Size(max : 2) ) : ID"   | true
        "field( testArg : [Product!] ) : ID"                | true
        "field( testArg : String @Size(max : 2) ) : ID"     | true
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
            directiveValidationRules.appliesTo(argument, fieldDefinition, container)
            assert false, "This should assert"
        } catch (AssertException ignored) {
        }

        where:

        fieldDeclaration                              | _
        "field( testArg : Int! @Size(max : 2) ) : ID" | _ // not allowed for Size
    }

}
