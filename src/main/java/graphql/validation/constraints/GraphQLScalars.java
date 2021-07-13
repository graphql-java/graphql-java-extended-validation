package graphql.validation.constraints;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import java.util.Arrays;
import java.util.List;

public class GraphQLScalars {
    public static final List<GraphQLScalarType> GRAPHQL_NUMBER_TYPES = Arrays.asList(
            Scalars.GraphQLByte,
            Scalars.GraphQLShort,
            Scalars.GraphQLInt,
            Scalars.GraphQLLong,
            Scalars.GraphQLBigDecimal,
            Scalars.GraphQLBigInteger,
            Scalars.GraphQLFloat
    );
}
