package graphql.validation.util;

import graphql.Internal;
import graphql.schema.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Internal
public class DirectivesAndTypeWalker {

    private final Map<String, Boolean> seenTypes = new HashMap<>();

    public boolean isSuitable(GraphQLArgument argument, BiFunction<GraphQLInputType, GraphQLAppliedDirective, Boolean> isSuitable) {
        GraphQLInputType inputType = argument.getType();
        List<GraphQLAppliedDirective> directives = argument.getAppliedDirectives();
        return walkInputType(inputType, directives, isSuitable);
    }

    private boolean walkInputType(GraphQLInputType inputType, List<GraphQLAppliedDirective> directives, BiFunction<GraphQLInputType, GraphQLAppliedDirective, Boolean> isSuitable) {
        String typeName = GraphQLTypeUtil.unwrapAll(inputType).getName();
        GraphQLInputType unwrappedInputType = Util.unwrapNonNull(inputType);
        for (GraphQLAppliedDirective directive : directives) {
            if (isSuitable.apply(unwrappedInputType, directive)) {
                return seen(typeName,true);
            }
        }
        if (unwrappedInputType instanceof GraphQLInputObjectType) {
            GraphQLInputObjectType inputObjType = (GraphQLInputObjectType) unwrappedInputType;
            if (seenTypes.containsKey(typeName)) {
                return seenTypes.get(typeName);
            }
            seen(typeName,false);

            for (GraphQLInputObjectField inputField : inputObjType.getFieldDefinitions()) {
                inputType = inputField.getType();
                directives = inputField.getAppliedDirectives();

                if (walkInputType(inputType, directives, isSuitable)) {
                    return seen(typeName,true);
                }
            }
        }
        if (unwrappedInputType instanceof GraphQLList) {
            GraphQLInputType innerListType = Util.unwrapOneAndAllNonNull(unwrappedInputType);
            if (innerListType instanceof GraphQLDirectiveContainer) {
                directives = ((GraphQLDirectiveContainer) innerListType).getAppliedDirectives();
                if (walkInputType(innerListType, directives, isSuitable)) {
                    return seen(typeName,true);
                }
            }
        }
        return seen(typeName,false);
    }

    private boolean seen(String typeName, boolean flag) {
        seenTypes.put(typeName, flag);
        return flag;
    }

}
