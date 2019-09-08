package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.relay.Relay;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Collections;
import java.util.List;

public class RelayIdConstraint extends AbstractDirectiveConstraint {

    private static final Relay RELAY = new Relay();

    public RelayIdConstraint() {
        super("RelayID");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The ID must be a valid relay global ID, There is an optional types parameter that restricts which id types are valid")

                .example("node( id : ID @RelayID) : Node")

                .applicableTypeNames(Scalars.GraphQLString.getName())

                .directiveSDL("directive @RelayID(types : [String!] = [],  message : String = \"%s\" ) " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION", getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType, Scalars.GraphQLID);
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        List<String> types = directive.getArgument("types") != null ? getStrListArg(directive, "types") : Collections.emptyList();



        if (validatedValue != null && !isValidRelayIds(validatedValue, types)) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment,
                    "any", types.isEmpty(),
                    "types", types.toString()));

        }
        return Collections.emptyList();
    }

    private boolean isValidRelayIds(Object validatedValue, List<String> types) {
        if (validatedValue instanceof List) {
            for(Object val: (List)validatedValue){
                boolean isValid = isValidRelayId(val.toString(), types);
                if (!isValid) {
                    return false;
                }
            }

            return true;
        } else {
            // Single argument version
            return isValidRelayId(validatedValue.toString(), types);
        }
    }

    private boolean isValidRelayId(String value, List<String> types) {
        try {
            Relay.ResolvedGlobalId resolvedGlobalId = RELAY.fromGlobalId(value);

            if (types.isEmpty()) {
                return true;
            } else {
                if (!types.contains(resolvedGlobalId.getType())) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
