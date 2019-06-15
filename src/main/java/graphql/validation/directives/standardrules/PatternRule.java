package graphql.validation.directives.standardrules;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputType;
import graphql.validation.directives.AbstractDirectiveValidationRule;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

public class PatternRule extends AbstractDirectiveValidationRule {

    public PatternRule() {
        super("Pattern");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Pattern(regexp : String! =\".*\", message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.Pattern.message");
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
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object validatedValue = ruleEnvironment.getValidatedValue();
        if (validatedValue == null) {
            return emptyList();
        }
        String strValue = String.valueOf(validatedValue);

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);

        String patternArg = getStrArg(directive, "regexp");
        Pattern pattern = cachedPattern(patternArg);

        Matcher matcher = pattern.matcher(strValue);
        if (!matcher.matches()) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "regexp", patternArg,
                    "validatedValue", validatedValue));
        }
        return emptyList();
    }

    private final static Map<String, Pattern> SEEN_PATTERNS = new HashMap<>();

    private Pattern cachedPattern(String patternArg) {
        return SEEN_PATTERNS.computeIfAbsent(patternArg, Pattern::compile);
    }


}
