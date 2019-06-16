package graphql.validation

import graphql.validation.constraints.DirectiveConstraint
import graphql.validation.constraints.DirectiveConstraints


/**
 * Useful for printing out the readme.md
 */
class DocHelper {

    static void main(String[] args) {
        ArrayList<DirectiveConstraint> standardRules = sorted()
        standardRules.forEach({ it -> printConstraint(it) })

        //standardRules.forEach({ it -> printMessage(it) })
    }


    private static ArrayList<DirectiveConstraint> sorted() {
        List<DirectiveConstraint> standardRules = new ArrayList<>(DirectiveConstraints.STANDARD_CONSTRAINTS);
        standardRules.sort(Comparator.comparing({ dvr -> dvr.getName() }))
        standardRules
    }

    private static void printConstraint(DirectiveConstraint r) {
        def appliesTo = r.getDocumentation().getApplicableTypeNames().collect({ s -> "`" + s + "`" }).join(", ")

        PrintStream out = System.out
        out.printf("""
### @${r.getName()}

${r.getDocumentation().getDescription()}

- Example : `${r.getDocumentation().getExample()}`

- Applies to : ${appliesTo}

- SDL : ${r.getDocumentation().getDirectiveSDL()}

- Message : `${r.getDocumentation().getMessageTemplate()}`

"""
        )
    }


    private static void printMessage(DirectiveConstraint r) {
        PrintStream out = System.out
        out.printf("""
        ${r.getDocumentation().getMessageTemplate()} = 
        """)
    }

}
