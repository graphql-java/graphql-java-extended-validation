package graphql.validation.el


import spock.lang.Specification
import spock.lang.Unroll

class ELSupportTest extends Specification {

    @Unroll
    def "containsOneOf"() {
        def el = new ELSupport(Locale.getDefault())

        expect:
        assert expected == el.evaluateBoolean(expression, values), "failed on " + expression + "with " + values

        where:

        expression                                  | expected | values
        '''${args.containsOneOf('a','b')}'''        | true     | [args: [a: true]]
        '''${args.containsOneOf('a','b','c')}'''    | true     | [args: [a: true]]
        '''${args.containsOneOf('a','b','c')}'''    | true     | [args: [c: "x"]]
        '''${args.containsOneOf('a','b','c')}'''    | false    | [args: [a: true, b: 2]]
        '''${args.containsOneOf('a','b','c')}'''    | false    | [args: [a: true, b: 2, c: true]]
        '''${args.containsOneOf('a','b','c')}'''    | false    | [args: [:]]

        // relay style
        '''${args.containsOneOf('first','last')}''' | false    | [args: [first: 1, last: 3]]
        '''${args.containsOneOf('first','last')}''' | true     | [args: [first: 1, last: null]]
        '''${args.containsOneOf('first','last')}''' | true     | [args: [first: 1]]
    }

    @Unroll
    def "containsAllOf"() {
        def el = new ELSupport(Locale.getDefault())

        expect:
        expected == el.evaluateBoolean(expression, values)

        where:

        expression                               | expected | values
        '''${args.containsAllOf('a','b')}'''     | false    | [args: [a: true]]
        '''${args.containsAllOf('a','b','c')}''' | false    | [args: [a: true]]
        '''${args.containsAllOf('a','b','c')}''' | false    | [args: [c: "x"]]
        '''${args.containsAllOf('a','b','c')}''' | false    | [args: [a: true, b: 2]]
        '''${args.containsAllOf('a','b','c')}''' | true     | [args: [a: true, b: 2, c: true]]
        '''${args.containsAllOf('a','b','c')}''' | false    | [args: [:]]

    }

    @Unroll
    def "basic expression support"() {
        def el = new ELSupport(Locale.getDefault())

        expect:
        expected == el.evaluateBoolean(expression, values)

        where:

        expression         | expected | values
        '''${value==20}''' | true     | [value: 20]
        '''${value!=20}''' | false    | [value: 20]
    }
}
