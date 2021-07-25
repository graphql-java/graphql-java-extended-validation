package graphql.validation.constraints

import graphql.validation.constraints.standard.ContainerSizeConstraint
import graphql.validation.constraints.standard.SizeConstraint
import spock.lang.Unroll

class AbstractDirectiveConstraintTest extends BaseConstraintTestSupport {

    @Unroll
    def "complex object argument constraints"() {
        def sizeConstraint = new SizeConstraint()

        def extraSDL = """
            ${sizeConstraint.getDocumentation().getDirectiveSDL()}

            input ProductItem {
                code : String @Size(max : 5)
                price : String @Size(max : 3)
            }

            input Product {
                name : String @Size(max : 7)
                items : [ProductItem!]! # crazy nulls
                crazyItems : [[[ProductItem!]!]] # nuts but can we handle it
            }            
        """

        // this tests that we can walk a complex tree of types via one specific implementation
        // but the same applies to all AbstractDirectiveConstraint classes
        def constraintUnderTest = new ContainerSizeConstraint()

        expect:

        def errors = runValidation(constraintUnderTest, fieldDeclaration, extraSDL, "testArg", argVal)
        errors.size() == eSize

        where:

        fieldDeclaration                                             | argVal                                               | eSize | expectedMessage
        // size can handle list elements
        "field( testArg : [Product!] @ContainerSize(max : 2) ) : ID" | [[:], [:], [:]]                                      | 1     | "graphql.validation.Size.message;path=/testArg;val:[[:], [:], [:]];\t"
        // goes down into input types
        "field( testArg : [Product!] @ContainerSize(max : 2) ) : ID" | [[name: "morethan7"], [:]]                           | 1     | "graphql.validation.Size.message;path=/testArg[0]/name;val:morethan7;\t"
        // shows that it traverses down lists
        "field( testArg : [Product!] @ContainerSize(max : 2) ) : ID" | [[name: "ok"], [name: "notOkHere"]]                  | 1     | "graphql.validation.Size.message;path=/testArg[1]/name;val:notOkHere;\t"
        // shows that it traverses down lists and objects
        "field( testArg : [Product!] @ContainerSize(max : 2) ) : ID" | [[items: [[code: "morethan5", price: "morethan3"]]]] | 2     | "graphql.validation.Size.message;path=/testArg[0]/items[0]/code;val:morethan5;\tgraphql.validation.Size.message;path=/testArg[0]/items[0]/price;val:morethan3;\t"

        // shows that it traverses down crazy lists and objects
        "field( testArg : [Product!] @ContainerSize(max : 2) ) : ID" | [[crazyItems: [[[[code: "morethan5"]]]]]]            | 1     | "graphql.validation.Size.message;path=/testArg[0]/crazyItems[0][0][0]/code;val:morethan5;\t"
    }

}
