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

public class PatternRule extends AbstractDirectiveValidationRule {

    public PatternRule() {
        super("Pattern");
    }

    @Override
    public String getDirectiveDeclarationSDL() {
        return String.format("directive @Pattern(pattern : String! =\".*\", message : String = \"%s\") " +
                        "on ARGUMENT_DEFINITION | INPUT_FIELD_DEFINITION",
                "graphql.validation.Pattern.message");
    }

    @Override
    public boolean appliesToType(GraphQLInputType inputType) {
        return isOneOfTheseTypes(inputType,
                Scalars.GraphQLString
        );
    }


    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        Object argumentValue = ruleEnvironment.getFieldOrArgumentValue();

        GraphQLDirective directive = ruleEnvironment.getContextObject(GraphQLDirective.class);

        String patternArg = getStrArg(directive, "pattern");
        Pattern pattern = cachedPattern(patternArg);

        Matcher matcher = pattern.matcher(String.valueOf(argumentValue));
        if (!matcher.matches()) {
            return mkError(ruleEnvironment, directive, mkMessageParams(
                    "pattern", patternArg,
                    "fieldOrArgumentValue", argumentValue));
        }
        return Collections.emptyList();
    }

    private final static Map<String, Pattern> SEEN_PATTERNS = new HashMap<>();

    private Pattern cachedPattern(String patternArg) {
        return SEEN_PATTERNS.computeIfAbsent(patternArg, Pattern::compile);
    }


}
