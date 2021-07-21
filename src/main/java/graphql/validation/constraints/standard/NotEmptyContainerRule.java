package graphql.validation.constraints.standard;

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.Documentation;
import static graphql.schema.GraphQLTypeUtil.isList;

public class NotEmptyContainerRule extends AbstractNotEmptyRule {

    public NotEmptyContainerRule() {
        super("NotEmptyContainer");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())
                .description("The container must have a non-zero size")
                .example("updateAccident( accidentNotes : [Notes]! @NotEmptyContainer) : DriverDetails")
                .applicableTypeNames("Lists", "Input Objects")
                .directiveSDL("directive @NotEmptyContainer(message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isList(inputType) || inputType instanceof GraphQLInputObjectType;
    }
}
