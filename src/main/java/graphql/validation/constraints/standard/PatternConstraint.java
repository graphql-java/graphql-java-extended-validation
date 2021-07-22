package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.constraints.Documentation;
import graphql.validation.rules.ValidationEnvironment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static graphql.schema.GraphQLTypeUtil.isList;
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
                .example("updateDriver( licencePlate : String @Pattern(regexp : \"[A-Z][A-Z][A-Z]-[0-9][0-9][0-9]\") : DriverDetails")
                .applicableTypeNames(Scalars.GraphQLString.getName(), Scalars.GraphQLID.getName(), "Lists")
                .directiveSDL("directive @Pattern(regexp : String! =\".*\", message : String = \"%s\") " +
                                "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                        getMessageTemplate())
                .build();
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isStringOrID(inputType) || isList(inputType);
    }

    @Override
    protected List<GraphQLError> runConstraint(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();

        String strValue = String.valueOf(validatedValue);

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);

        String patternArg = getStrArg(directive, "regexp");
        Pattern pattern = cachedPattern(patternArg);

        Matcher matcher = pattern.matcher(strValue);
        if (!matcher.matches()) {
            return mkError(validationEnvironment, "regexp", patternArg);
        }

        return emptyList();
    }

    private Pattern cachedPattern(String patternArg) {
        return SEEN_PATTERNS.computeIfAbsent(patternArg, Pattern::compile);
    }


}
