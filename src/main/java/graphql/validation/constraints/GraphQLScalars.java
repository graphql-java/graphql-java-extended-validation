package graphql.validation.constraints;

import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import java.util.Arrays;
import java.util.List;

public class GraphQLScalars {
    public static final List<GraphQLScalarType> GRAPHQL_NUMBER_TYPES = Arrays.asList(
            Scalars.GraphQLInt,
            Scalars.GraphQLFloat,
            ExtendedScalars.GraphQLByte,
            ExtendedScalars.GraphQLShort,
            ExtendedScalars.GraphQLLong,
            ExtendedScalars.GraphQLBigDecimal,
            ExtendedScalars.GraphQLBigInteger
    );
}
