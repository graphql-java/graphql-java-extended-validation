package graphql.validation.constraints.standard

import graphql.validation.constraints.BaseConstraintTestSupport
import graphql.validation.constraints.DirectiveConstraint
import spock.lang.Unroll

class DecimalMinMaxConstraintTest extends BaseConstraintTestSupport {


    @Unroll
    def "decimal min rule constraints"() {

        DirectiveConstraint ruleUnderTest = new DecimalMinConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                                         | argVal               | expectedMessage
        'field( arg : Int @DecimalMin(value : "100") ) : ID'                     | 50                   | 'DecimalMin;path=/arg;val:50;\t'
        'field( arg : Int @DecimalMin(value : "10") ) : ID'                      | 50                   | ''

        // strings are allowed
        'field( arg : Int @DecimalMin(value : "100") ) : ID'                     | "50"                 | 'DecimalMin;path=/arg;val:50;\t'
        'field( arg : Int @DecimalMin(value : "10") ) : ID'                      | "50"                 | ''

        // longs are allowed
        'field( arg : Int @DecimalMin(value : "100") ) : ID'                     | 50L                  | 'DecimalMin;path=/arg;val:50;\t'
        'field( arg : Int @DecimalMin(value : "10") ) : ID'                      | 50L                  | ''

        // BigIntegers are allowed
        'field( arg : Int @DecimalMin(value : "100") ) : ID'                     | new BigInteger("50") | 'DecimalMin;path=/arg;val:50;\t'
        'field( arg : Int @DecimalMin(value : "10") ) : ID'                      | new BigInteger("50") | ''

        // BigDecimal are allowed
        'field( arg : Int @DecimalMin(value : "100") ) : ID'                     | new BigDecimal("50") | 'DecimalMin;path=/arg;val:50;\t'
        'field( arg : Int @DecimalMin(value : "10") ) : ID'                      | new BigDecimal("50") | ''

        // custom
        'field( arg : Int @DecimalMin(value : "100", message : "custom") ) : ID' | 50                   | 'custom;path=/arg;val:50;\t'

        // edge cases
        'field( arg : Int @DecimalMin(value : "50") ) : ID'                      | 49                   | 'DecimalMin;path=/arg;val:49;\t'
        'field( arg : Int @DecimalMin(value : "50") ) : ID'                      | 50                   | ''
        'field( arg : Int @DecimalMin(value : "50") ) : ID'                      | 51                   | ''

        // exclusive
        'field( arg : Int @DecimalMin(value : "50" inclusive:false) ) : ID'      | 49                   | 'DecimalMin;path=/arg;val:49;\t'
        'field( arg : Int @DecimalMin(value : "50" inclusive:false) ) : ID'      | 50                   | 'DecimalMin;path=/arg;val:50;\t'
        'field( arg : Int @DecimalMin(value : "50" inclusive:false) ) : ID'      | 51                   | ''

        // nulls are valid
        'field( arg : Int @DecimalMin(value : "50" inclusive:false) ) : ID'      | null                 | ''

        // Lists
        'field( arg : [Int] @DecimalMin( value: "50", inclusive: false ) ) : ID' | [50]                 | 'DecimalMin;path=/arg[0];val:50;\t'
        'field( arg : [Int] @DecimalMin( value: "50", inclusive: false ) ) : ID' | [51, 52]             | ''
        'field( arg : [Int] @DecimalMin( value: "50", inclusive: false ) ) : ID' | [null]           | ''
    }

    @Unroll
    def "decimal max rule constraints"() {

        DirectiveConstraint ruleUnderTest = new DecimalMaxConstraint()

        expect:

        def errors = runValidation(ruleUnderTest, fieldDeclaration, "arg", argVal)
        assertErrors(errors, expectedMessage)

        where:

        fieldDeclaration                                                         | argVal     | expectedMessage
        'field( arg : Int @DecimalMax(value : "100") ) : ID'                     | 150        | 'DecimalMax;path=/arg;val:150;\t'
        'field( arg : Int @DecimalMax(value : "100") ) : ID'                     | 50         | ''

        'field( arg : Int @DecimalMax(value : "100", message : "custom") ) : ID' | 150        | 'custom;path=/arg;val:150;\t'
        // edge case
        'field( arg : Int @DecimalMax(value : "50") ) : ID'                      | 51         | 'DecimalMax;path=/arg;val:51;\t'
        'field( arg : Int @DecimalMax(value : "50") ) : ID'                      | 50         | ''
        'field( arg : Int @DecimalMax(value : "50") ) : ID'                      | 49         | ''

        // exclusive
        'field( arg : Int @DecimalMax(value : "50" inclusive:false) ) : ID'      | 50         | 'DecimalMax;path=/arg;val:50;\t'
        'field( arg : Int @DecimalMax(value : "50" inclusive:false) ) : ID'      | 51         | 'DecimalMax;path=/arg;val:51;\t'
        'field( arg : Int @DecimalMax(value : "50" inclusive:false) ) : ID'      | 49         | ''

        // nulls are valid
        'field( arg : Int @DecimalMax(value : "50" inclusive:false) ) : ID'      | null       | ''

        // Lists
        'field( arg : [Int] @DecimalMax( value: "50", inclusive: false ) ) : ID' | [50]       | 'DecimalMax;path=/arg[0];val:50;\t'
        'field( arg : [Int] @DecimalMax( value: "50", inclusive: false ) ) : ID' | [48, 49]   | ''
        'field( arg : [Int] @DecimalMax( value: "50", inclusive: false ) ) : ID' | [null] | ''
    }

}