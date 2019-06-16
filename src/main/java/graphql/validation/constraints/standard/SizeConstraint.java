package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SizeConstraint extends AbstractDirectiveConstraint {

    public SizeConstraint() {
        super("Size");
    }


    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Size(min : Int = 0, max : Int = %d, message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                Integer.MAX_VALUE, getMessageTemplate());
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isStringOrListOrMap(inputType);
    }

    @Override
    public List<String> getApplicableTypeNames() {
        return Arrays.asList(Scalars.GraphQLString.getName(), "Lists", "Input Objects");
    }

    @Override
    public String getDescription() {
        return "The element size must be between the specified `min` and `max` boundaries (inclusive).";
    }

    @Override
    public String getExample() {
        return "updateDriver( drivingNote : String @Size( min : 1000, max : 100000)) : DriverDetails";
    }


    @Override
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        GraphQLInputType argType = validationEnvironment.getFieldOrArgumentType();

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        int min = getIntArg(directive, "min");
        int max = getIntArg(directive, "max");


        int size = getStringOrObjectOrMapLength(argType, validatedValue);

        if (size < min) {
            return mkError(validationEnvironment, directive, mkParams(validatedValue, validationEnvironment, min, max, size));
        }
        if (size > max) {
            return mkError(validationEnvironment, directive, mkParams(validatedValue, validationEnvironment, min, max, size));
        }
        return Collections.emptyList();
    }

    private Map<String, Object> mkParams(Object validatedValue, ValidationEnvironment validationEnvironment, int min, int max, int size) {
        return mkMessageParams(validatedValue, validationEnvironment,
                "min", min,
                "max", max,
                "size", size
        );
    }
}
