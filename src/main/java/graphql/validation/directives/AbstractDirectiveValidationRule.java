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
import graphql.validation.rules.ValidationRuleEnvironment;
import graphql.validation.util.Util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLTypeUtil.isList;
import static java.util.Collections.singletonList;

public abstract class AbstractDirectiveValidationRule implements DirectiveValidationRule {

    private final String name;

    public AbstractDirectiveValidationRule(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean appliesToType(GraphQLArgument argument, GraphQLFieldDefinition fieldDefinition, GraphQLFieldsContainer fieldsContainer) {
        return appliesToType(Util.unwrapNonNull(argument.getType()));
    }

    protected boolean isOneOfTheseTypes(GraphQLInputType inputType, GraphQLScalarType... scalarTypes) {
        GraphQLInputType unwrappedType = Util.unwrapNonNull(inputType);
        for (GraphQLScalarType scalarType : scalarTypes) {
            if (unwrappedType.getName().equals(scalarType.getName())) {
                return true;
            }
        }
        return false;
    }

    protected int getIntArg(GraphQLDirective directive, String argName) {
        GraphQLArgument argument = directive.getArgument(argName);
        if (argument == null) {
            return assertExpectedArgType(argName, "Int");
        }
        Number value = (Number) argument.getValue();
        if (value == null) {
            value = (Number) argument.getDefaultValue();
            if (value == null) {
                return assertExpectedArgType(argName, "Int");
            }
        }
        return value.intValue();
    }

    protected String getStrArg(GraphQLDirective directive, String argName) {
        GraphQLArgument argument = directive.getArgument(argName);
        if (argument == null) {
            return assertExpectedArgType(argName, "String");
        }
        String value = (String) argument.getValue();
        if (value == null) {
            value = (String) argument.getDefaultValue();
            if (value == null) {
                return assertExpectedArgType(argName, "String");
            }
        }
        return value;
    }

    protected boolean getBoolArg(GraphQLDirective directive, String argName) {
        GraphQLArgument argument = directive.getArgument(argName);
        if (argument == null) {
            return assertExpectedArgType(argName, "Boolean");
        }
        Object value = argument.getValue();
        if (value == null) {
            value = argument.getDefaultValue();
            if (value == null) {
                return assertExpectedArgType(argName, "Boolean");
            }
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    protected String getMessageTemplate(GraphQLDirective directive) {
        String msg = null;
        GraphQLArgument arg = directive.getArgument("message");
        if (arg != null) {
            msg = (String) arg.getValue();
            if (msg == null) {
                msg = (String) arg.getDefaultValue();
            }
        }
        if (msg == null) {
            msg = "graphql.validation." + getName() + ".message";
        }
        return msg;
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

    protected List<GraphQLError> mkError(ValidationRuleEnvironment ruleEnvironment, GraphQLDirective directive, Map<String, Object> msgParams) {
        String messageTemplate = getMessageTemplate(directive);
        GraphQLError error = ruleEnvironment.getInterpolator().interpolate(messageTemplate, msgParams, ruleEnvironment);
        return singletonList(error);
    }

    protected boolean isStringOrListOrMap(GraphQLInputType argumentType) {
        GraphQLInputType unwrappedType = Util.unwrapOneAndAllNonNull(argumentType);
        return Scalars.GraphQLString.equals(unwrappedType) ||
                isList(argumentType) ||
                (unwrappedType instanceof GraphQLInputObjectType);
    }

    @SuppressWarnings("ConstantConditions")
    protected Map asMap(Object value) {
        Assert.assertTrue(value instanceof Map, "The argument value MUST be a Map value");
        return (Map) value;
    }

    protected BigDecimal asBigDecimal(Object value) throws NumberFormatException {
        if (value == null) {
            return Assert.assertShouldNeverHappen("Validation cant handle null objects BigDecimals");
        }
        String bdStr = "";
        if (value instanceof Number) {
            bdStr = value.toString();
        } else if (value instanceof String) {
            bdStr = value.toString();
        } else {
            Assert.assertShouldNeverHappen("Validation cant handle objects of type '%s' as BigDecimals", value.getClass().getSimpleName());
        }
        return new BigDecimal(bdStr);
    }

    protected boolean asBoolean(Object value) {
        if (value == null) {
            return Assert.assertShouldNeverHappen("Validation cant handle null objects Booleans");
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return Assert.assertShouldNeverHappen("Validation cant handle objects of type '%s' as Booleans", value.getClass().getSimpleName());
        }
    }

    protected int getStringOrObjectOrMapLength(GraphQLInputType inputType, Object argumentValue) {
        int valLen;
        if (argumentValue == null) {
            valLen = 0;
        } else if (Scalars.GraphQLString.equals(Util.unwrapNonNull(inputType))) {
            valLen = String.valueOf(argumentValue).length();
        } else if (isList(inputType)) {
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

    private <T> T assertExpectedArgType(String argName, String typeName) {
        return Assert.assertShouldNeverHappen("A validation directive MUST have a '%s' argument of type '%s' with a default value", argName, typeName);
    }

}
