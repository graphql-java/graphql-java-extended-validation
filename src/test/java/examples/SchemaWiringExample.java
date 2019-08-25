package examples;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.ValidationRules;
import graphql.validation.schemawiring.ValidationSchemaWiring;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static graphql.validation.util.Util.mkMap;

@SuppressWarnings("UnnecessaryLocalVariable")
public class SchemaWiringExample {

    public static void main(String[] args) {

        try {
            GraphQLSchema graphQLSchema = buildSchemaAndWiring();
            GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

            Object applications = Arrays.asList(
                    mkMap("name", "Brad"),
                    mkMap("name", "Andi"),
                    mkMap("name", "Bill"),
                    mkMap("name", repeatString("x", 200)) // too long
            );
            Map<String, Object> variables = mkMap("applications", applications);

            ExecutionInput ei = ExecutionInput.newExecutionInput()
                    .query("query X($applications : [Application!]) {" +
                            "   hired(applications : $applications)" +
                            "}")
                    .variables(variables)
                    .build();

            ExecutionResult result = graphQL.execute(ei);

            for (GraphQLError error : result.getErrors()) {
                System.out.println(error.getMessage());
            }
        } catch (RuntimeException rte) {
            rte.printStackTrace(System.out);
        }

    }

    private static GraphQLSchema buildSchemaAndWiring() {
        //
        // This contains by default the standard library provided @Directive constraints
        //
        ValidationRules validationRules = ValidationRules.newValidationRules()
                .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
                .build();
        //
        // This will rewrite your data fetchers when rules apply to them so that validation
        ValidationSchemaWiring schemaWiring = new ValidationSchemaWiring(validationRules);
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

    private static String repeatString(String s, int n) {
        return String.join("", Collections.nCopies(n, s));
    }

    private static Reader buildSDL() {
        InputStream is = SchemaWiringExample.class.getResourceAsStream("/examples/example.graphqls");
        return new InputStreamReader(is);
    }
}
