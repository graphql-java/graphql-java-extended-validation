package graphql.validation.directives;

import graphql.Assert;
import graphql.GraphQLError;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.validation.rules.ValidationRuleEnvironment;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLTypeUtil.isList;
import static java.util.Collections.singletonList;

public abstract class AbstractDirectiveValidationRule implements DirectiveValidationRule {

    protected final String name;

    public AbstractDirectiveValidationRule(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean appliesToArgument(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        boolean applicable = appliesToType(argument.getType());
        if (!applicable) {
            String argType = argument.getType().getName();
            Assert.assertShouldNeverHappen("The directive %s cannot be placed in arguments of type %s", getName(), argType);
        }
        return true;
    }

    protected abstract boolean appliesToType(GraphQLInputType inputType);


    @Override
    public List<GraphQLError> runValidation(ValidationRuleEnvironment ruleEnvironment) {
        return Collections.emptyList();
    }

    protected boolean appliesToTypes(GraphQLInputType argumentType, GraphQLScalarType... scalarTypes) {
        GraphQLInputType unwrappedType = unwrap(argumentType);
        for (GraphQLScalarType scalarType : scalarTypes) {
            if (unwrappedType.getName().equals(scalarType.getName())) {
                return true;
            }
        }
        return false;

    }


    protected GraphQLDirective getArgDirective(ValidationRuleEnvironment ruleEnvironment, String name) {
        GraphQLDirective directive = ruleEnvironment.getArgument().getDirective(name);
        return Assert.assertNotNull(directive);
    }

    protected int getIntArg(GraphQLDirective directive, String argName, int defaultValue) {
        GraphQLArgument argument = directive.getArgument(argName);
        Assert.assertNotNull(argument);
        Number value = (Number) argument.getValue();
        if (value == null) {
            return defaultValue;
        }
        return value.intValue();
    }

    protected String getStrArg(GraphQLDirective directive, String name) {
        return (String) directive.getArgument(name).getValue();
    }

    protected String getMessageTemplate(GraphQLDirective directive) {
        String msg = getStrArg(directive, "message");
        return Assert.assertNotNull(msg, "A validation directive MUST have a message argument with a default");
    }


    protected Map<String, Object> mkMessageParams(Object... args) {
        Assert.assertTrue(args.length % 2 == 0, "You MUST pass in an even number of arguments");
        Map<String, Object> params = new LinkedHashMap<>();
        for (int ix = 0; ix < args.length; ix = ix + 2) {
            Object key = args[ix];
            Assert.assertTrue(key instanceof String, "You MUST pass in a string key");
            Object val = args[ix + 1];
            params.put(String.valueOf(key), val);
        }

        return params;
    }

    protected GraphQLInputType unwrap(GraphQLInputType inputType) {
        return (GraphQLInputType) GraphQLTypeUtil.unwrapAll(inputType);
    }

    protected List<GraphQLError> mkError(ValidationRuleEnvironment ruleEnvironment, GraphQLDirective directive, Map<String, Object> msgParams) {
        String messageTemplate = getMessageTemplate(directive);
        GraphQLError error = ruleEnvironment.getInterpolator().interpolate(messageTemplate, msgParams);
        return singletonList(error);
    }


    protected boolean isStringOrListOrMap(GraphQLInputType argumentType) {
        GraphQLInputType unwrappedType = unwrap(argumentType);
        return Scalars.GraphQLString.equals(unwrappedType) ||
                isList(argumentType) ||
                (unwrappedType instanceof GraphQLInputObjectType);
    }

    @SuppressWarnings("ConstantConditions")
    protected Map asMap(Object value) {
        Assert.assertTrue(value instanceof Map, "The argument value MUST be a Map value");
        return (Map) value;
    }

    protected BigDecimal asBigDecimal(Object value) {
        String bdStr = "";
        if (value instanceof Number) {
            bdStr = value.toString();
        } else {
            Assert.assertShouldNeverHappen("Validation cant handle objects of type '%s' as BigDecimals", value.getClass().getSimpleName());
        }
        return new BigDecimal(bdStr);
    }


    protected int getStringOrObjectOrMapLength(GraphQLInputType argType, Object argumentValue) {
        GraphQLInputType unwrappedType = unwrap(argType);
        int valLen;
        if (argumentValue == null) {
            valLen = 0;
        } else if (Scalars.GraphQLString.equals(unwrappedType)) {
            valLen = String.valueOf(argumentValue).length();
        } else if (isList(argType)) {
            valLen = getListLength(argumentValue);
        } else {
            valLen = getObjectLen(argumentValue);
        }
        return valLen;
    }

    private int getObjectLen(Object value) {
        if (value == null) {
            return 0;
        }
        Map map = asMap(value);
        return map.size();
    }


    private int getListLength(Object value) {
        if (value instanceof Collection) {
            return ((Collection) value).size();
        } else if (value instanceof Iterable) {
            int len = 0;
            for (Object ignored : ((Iterable) value)) {
                len++;
            }
            return len;
        } else if (value != null && value.getClass().isArray()) {
            return Array.getLength(value);
        }
        return 0;
    }

}
