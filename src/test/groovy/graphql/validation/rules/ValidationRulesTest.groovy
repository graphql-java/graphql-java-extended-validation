package graphql.validation.rules

import graphql.GraphQL
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.validation.TestUtil
import graphql.validation.constraints.DirectiveConstraints
import graphql.validation.schemawiring.ValidationSchemaWiring
import spock.lang.Specification

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring

class ValidationRulesTest extends Specification {


    def directiveRules = DirectiveConstraints.newDirectiveConstraints().build()

    def sdl = '''

            ''' + directiveRules.directivesSDL + '''

            type Car {
                model : String
                make : String
            }

            input CarFilter {
                model : String @Size(max : 10)
                make : String
                age : Int @Range(max : 5) @Expression(value : "${validatedValue==20}")
            }


            type Query {
                cars(filter : CarFilter) : [Car] @Expression(value : "${false}")
            }
        '''

    DataFetcher carsDF = { env ->
        ValidationRules validationRules = ValidationRules
                .newValidationRules().build()
        def errors = validationRules.runValidationRules(env)
        if (!errors.isEmpty()) {
            return DataFetcherResult.newResult().errors(errors).data(null).build()
        }
        return [
                [model: "Prado", make: "Toyota"]
        ]
    }

    def runtime = RuntimeWiring.newRuntimeWiring()
            .type(newTypeWiring("Query").dataFetcher("cars", carsDF))
            .build()
    def schema = TestUtil.schema(sdl, runtime)
    def graphQL = GraphQL.newGraphQL(schema).build()

    def "run rules for data fetcher environment direct from possible rules"() {

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

        er.errors.size() == 3
        er.errors[0].message == "/cars expression must evaluate to true"
        er.errors[0].path == ["cars"]
        er.errors[1].message == "/cars/filter/age range must be between 0 and 5"
        er.errors[1].path == ["cars"]
        er.errors[2].message == "/cars/filter/model size must be between 0 and 10"
        er.errors[2].path == ["cars"]
    }

    def "issue 17 - type references handled"() {

        def directiveRules = DirectiveConstraints.newDirectiveConstraints().build()

        def sdl = '''

            ''' + directiveRules.directivesSDL + '''

           input NameRequest {
	         # The title associated to the name
            title: String @Size(min : 1, max : 1)
	        # The given name
	        givenName: String! @Size(min : 1, max : 1)
	        # Middle Name
   	        middleName: String
   	        # Last Name
   	        surName: String!
   	        
   	        # recursion
   	        inner : NameRequest
   	        
   	        innerList : [NameRequest!]
          }

            type Query {
                request( arg : NameRequest!) : String
            }
        '''

        ValidationRules validationRules = ValidationRules.newValidationRules()
                .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL).build()
        def validationWiring = new ValidationSchemaWiring(validationRules)

        DataFetcher df = { DataFetchingEnvironment env ->
            return "OK"
        }

        def runtime = RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query").dataFetcher("request", df))
                .directiveWiring(validationWiring)
                .build()
        def graphQLSchema = TestUtil.schema(sdl, runtime)
        def graphQL = GraphQL.newGraphQL(graphQLSchema).build()

        when:

        def er = graphQL.execute('''
            {
                request (
                    arg : { 
                        title : "Mr BRAD", givenName : "BRADLEY" , surName : "BAKER" 
                        inner : { title : "Mr BRAD", givenName : "BRADLEY" , surName : "BAKER" }
                        innerList : [ { title : "Mr BRAD", givenName : "BRADLEY" , surName : "BAKER" } ]
                    }
                )
            }
        ''')
        then:
        er != null
        er.data["request"] == null
        er.errors.size() == 6

        er.errors[0].getMessage() == "/request/arg/givenName size must be between 1 and 1"
        er.errors[1].getMessage() == "/request/arg/inner/givenName size must be between 1 and 1"
        er.errors[2].getMessage() == "/request/arg/inner/title size must be between 1 and 1"
        er.errors[3].getMessage() == "/request/arg/innerList[0]/givenName size must be between 1 and 1"
        er.errors[4].getMessage() == "/request/arg/innerList[0]/title size must be between 1 and 1"
        er.errors[5].getMessage() == "/request/arg/title size must be between 1 and 1"
    }
}
