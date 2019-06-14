package graphql.validation;

import graphql.validation.directives.DirectiveValidationRule;
import graphql.validation.directives.DirectiveValidationRules;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Useful for printing out the readme.md
 */
public class DocHelper {

    public static void main(String[] args) {
        printConstraints();
    }

    private static void printConstraints() {
        List<DirectiveValidationRule> standardRules = new ArrayList<>(DirectiveValidationRules.STANDARD_RULES);
        standardRules.sort(Comparator.comparing(DirectiveValidationRule::getName));
        standardRules.forEach(DocHelper::printConstraint);
    }

    private static void printConstraint(DirectiveValidationRule r) {
        PrintStream out = System.out;
        out.print("\n" +
                String.format("### %s\n", r.getName())
        );
        out.print("\n" +
                String.format("%s", r.getDescription())
        );

        out.print("\n\n" +
                String.format("- SDL :  `%s`", r.getDirectiveDeclarationSDL())
        );
        List<String> names = r.getApplicableTypeNames().stream().map(s -> "`" + s + "`").collect(Collectors.toList());
        out.print("\n" +
                String.format("- Applies to :  %s", String.join(", ", names))
        );
        out.println();
    }
}
