package graphql.validation.schemawiring


import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.validation.TestUtil
import graphql.validation.constraints.DirectiveConstraints
import graphql.validation.rules.PossibleValidationRules
import spock.lang.Specification

class ValidationSchemaWiringTest extends Specification {


    def "integration test"() {

        def directiveRules = DirectiveConstraints.newDirectiveConstraints().build()

        def sdl = """

            ${directiveRules.directivesSDL}

            type Car {
                model : String
                make : String
            }

            input CarFilter {
                model : String @Size(max : 10)
                make : String
                age : Int @Range(max : 5)
            }


            type Query {
                cars(filter : CarFilter) : [Car]
            }
            
        """


        PossibleValidationRules possibleRules = PossibleValidationRules.newPossibleRules()
                .addRule(directiveRules)
                .build()

        ValidationSchemaWiring schemaWiring = new ValidationSchemaWiring(possibleRules)

        def runtime = RuntimeWiring.newRuntimeWiring().directiveWiring(schemaWiring).build()
        def schema = TestUtil.schema(sdl, runtime)
        def graphQL = GraphQL.newGraphQL(schema).build()

        when:
        def er = graphQL.execute('''
            {
                cars (filter : { model : "Ford OR Toyota", age : 20 }) {
                    model
                    make
                }
            }
        ''')

        then:
        er.errors.size() == 1
        er.errors[0].message == "size must be between 0 and 10"
    }
}