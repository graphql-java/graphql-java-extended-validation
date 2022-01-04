package graphql.validation.constraints;

import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final List<GraphQLScalarType> GRAPHQL_NUMBER_AND_STRING_TYPES = Stream.concat(
            Stream.of(Scalars.GraphQLString, Scalars.GraphQLID),
            GraphQLScalars.GRAPHQL_NUMBER_TYPES.stream()
    ).collect(Collectors.toList());
}
