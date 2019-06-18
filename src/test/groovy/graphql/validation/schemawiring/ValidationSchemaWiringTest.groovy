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
        def specification = er.toSpecification()
        specification != null

        er.errors.size() == 2
        er.errors[0].message == "/cars/filter/age range must be between 0 and 5"
        er.errors[1].message == "/cars/filter/model size must be between 0 and 10"
    }
}