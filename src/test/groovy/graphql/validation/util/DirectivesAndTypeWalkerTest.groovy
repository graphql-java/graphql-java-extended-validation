package graphql.validation.util


import graphql.schema.GraphQLObjectType
import graphql.validation.TestUtil
import spock.lang.Specification

class DirectivesAndTypeWalkerTest extends Specification {

    def "can walk self referencing types"() {
        def sdl = """
            input TestInput @deprecated {
                name : String
                list : [TestInput]
                self : TestInput
            }
            
            type Query {
                f(arg : TestInput) : String
            }
        """

        def schema = TestUtil.schema(sdl)
        def arg = (schema.getType("Query") as GraphQLObjectType).getFieldDefinition("f").getArgument("arg")
        def callback = { t, d -> true }
        when:
        def suitable = new DirectivesAndTypeWalker().isSuitable(arg, callback)

        then:
        suitable
    }
}
