package graphql.validation.constraints.standard;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.constraints.AbstractDirectiveConstraint;
import graphql.validation.rules.ValidationEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

public class PatternConstraint extends AbstractDirectiveConstraint {

    private final static Map<String, Pattern> SEEN_PATTERNS = new HashMap<>();

    public PatternConstraint() {
        super("Pattern");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Pattern(regexp : String! =\".*\", message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                getMessageTemplate());
    }

    @Override
    public String getDescription() {
        return "The String must match the specified regular expression, which follows the Java regular expression conventions.";
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                Scalars.GraphQLString
        );
    }

    @Override
    public List<String> getApplicableTypeNames() {
        return Collections.singletonList(Scalars.GraphQLString.getName());
    }

    @Override
    public List<GraphQLError> runValidation(ValidationEnvironment validationEnvironment) {
        Object validatedValue = validationEnvironment.getValidatedValue();
        if (validatedValue == null) {
            return emptyList();
        }
        String strValue = String.valueOf(validatedValue);

        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);

        String patternArg = getStrArg(directive, "regexp");
        Pattern pattern = cachedPattern(patternArg);

        Matcher matcher = pattern.matcher(strValue);
        if (!matcher.matches()) {
            return mkError(validationEnvironment, directive, mkMessageParams(validatedValue, validationEnvironment,
                    "regexp", patternArg
            ));
        }
        return emptyList();
    }

    private Pattern cachedPattern(String patternArg) {
        return SEEN_PATTERNS.computeIfAbsent(patternArg, Pattern::compile);
    }


}
