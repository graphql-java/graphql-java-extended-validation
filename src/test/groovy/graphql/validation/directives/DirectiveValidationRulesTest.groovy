package graphql.validation.directives

import graphql.AssertException
import graphql.validation.directives.standardrules.SizeRule
import graphql.validation.rules.ValidationRuleEnvironment
import spock.lang.Unroll

class DirectiveValidationRulesTest extends BaseDirectiveRuleTest {

    def "basic building"() {
        when:
        def rules = DirectiveValidationRules.newDirectiveValidationRules().build()

        then:
        rules.getDirectiveRules().size() == 16

        when:
        rules = DirectiveValidationRules.newDirectiveValidationRules().clearRules().build()

        then:
        rules.getDirectiveRules().size() == 0
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

        def directiveValidationRules = DirectiveValidationRules.newDirectiveValidationRules().build()

        expect:

        def schema = buildSchema(directiveValidationRules.getDirectivesDeclarationSDL(), fieldDeclaration, extraSDL)

        ValidationRuleEnvironment ruleEnvironment = buildEnv("Size", schema, "testArg", argVal)

        def errors = directiveValidationRules.runValidation(ruleEnvironment)
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

        def directiveValidationRules = DirectiveValidationRules.newDirectiveValidationRules()
                .clearRules()
                .addRule(new SizeRule()) // only size in effect - not Range
                .build()

        expect:

        def schema = buildSchema(directiveValidationRules.getDirectivesDeclarationSDL(), fieldDeclaration, extraSDL)

        ValidationRuleEnvironment ruleEnvironment = buildEnv("Size", schema, "testArg", null)

        def argument = ruleEnvironment.getArgument()
        def fieldDefinition = ruleEnvironment.getFieldDefinition()
        def container = ruleEnvironment.getFieldsContainer()
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


        def directiveValidationRules = DirectiveValidationRules.newDirectiveValidationRules()
                .clearRules()
                .addRule(new SizeRule())
                .build()

        expect:

        def schema = buildSchema(directiveValidationRules.getDirectivesDeclarationSDL(), fieldDeclaration, "")

        ValidationRuleEnvironment ruleEnvironment = buildEnv("Size", schema, "testArg", null)

        def argument = ruleEnvironment.getArgument()
        def fieldDefinition = ruleEnvironment.getFieldDefinition()
        def container = ruleEnvironment.getFieldsContainer()

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
