package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.rules.ValidationEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.isScalar;
import static java.util.Collections.emptyList;

public class PatternConstraint extends AbstractDirectiveConstraint {

    private final static Map<String, Pattern> SEEN_PATTERNS = new HashMap<>();

    public PatternConstraint() {
        super("Pattern");
    }

    @Override
    public Documentation getDocumentation() {
        return Documentation.newDocumentation()
                .messageTemplate(getMessageTemplate())

                .description("The String must match the specified regular expression, which follows the Java regular expression conventions.")

                .example("updateDriver( licencePlate : String @Patttern(regex : \"[A-Z][A-Z][A-Z]-[0-9][0-9][0-9]\") : DriverDetails")

                .applicableTypeNames(Scalars.GraphQLString.getName())

                .directiveSDL("directive @Pattern(regexp : String! =\".*\", message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                Scalars.GraphQLString) || isList(inputType);
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        if (validatedValue == null) {
            return emptyList();
        }

        List<Object> validatedValues;

        if (validatedValue instanceof List) {
            validatedValues = (ArrayList)validatedValue;
        } else {
            validatedValues = Arrays.asList(validatedValue);
        }

        for (Object value : validatedValues) {
            String strValue = String.valueOf(value);

            GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);

            String patternArg = getStrArg(directive, "regexp");
            Pattern pattern = cachedPattern(patternArg);

            Matcher matcher = pattern.matcher(strValue);
            if (!matcher.matches()) {
                return mkError(validationEnvironment, directive,
                    mkMessageParams(validatedValue, validationEnvironment, "regexp", patternArg));
            }
        }
        return emptyList();
    }



    private Pattern cachedPattern(String patternArg) {
        return SEEN_PATTERNS.computeIfAbsent(patternArg, Pattern::compile);
    }


}
