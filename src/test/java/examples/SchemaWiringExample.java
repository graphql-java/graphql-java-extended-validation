package examples;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.PossibleValidationRules;
import graphql.validation.schemawiring.ValidationSchemaWiring;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnnecessaryLocalVariable")
public class SchemaWiringExample {

    public static void main(String[] args) {

        try {
            GraphQLSchema graphQLSchema = buildSchemaAndWiring();
            GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

            Map<String, Object> variables = new HashMap<>();
            variables.put("x", "y");

            ExecutionInput ei = ExecutionInput.newExecutionInput()
                    .query("{field}")
                    .variables(variables)
                    .build();

            ExecutionResult result = graphQL.execute(ei);

            System.out.println(result);
        } catch (RuntimeException rte) {
            rte.printStackTrace(System.out);
        }

    }

    private static GraphQLSchema buildSchemaAndWiring() {
        //
        // This contains by default the standard library provided @Directive constraints
        //
        PossibleValidationRules possibleRules = PossibleValidationRules.newPossibleRules()
                .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
                .build();
        //
        // This will rewrite your data fetchers when rules apply to them so that validation
        ValidationSchemaWiring schemaWiring = new ValidationSchemaWiring(possibleRules);
        //
        // we add this schema wiring to the graphql runtime
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring().directiveWiring(schemaWiring).build();
        //
        // then pretty much standard graphql-java code to build a graphql schema
        Reader sdl = buildSDL();
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(sdl);
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        
        return graphQLSchema;
    }

    private static Reader buildSDL() {
        InputStream is = SchemaWiringExample.class.getResourceAsStream("/examples/example.graphqls");
        return new InputStreamReader(is);
    }
}
