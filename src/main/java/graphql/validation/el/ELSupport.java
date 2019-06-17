package graphql.validation.el;

import graphql.Internal;
import org.hibernate.validator.internal.engine.messageinterpolation.FormatterWrapper;

import javax.el.ELContext;
import javax.el.ELManager;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import javax.el.ValueExpression;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

@Internal
@SuppressWarnings("unused")
public class ELSupport {
    private static final ExpressionFactory expressionFactory = loadExpressionSupport();
    private final StandardELContext elContext;

    public ELSupport(Locale locale) {
        elContext = new StandardELContext(expressionFactory);
        elContext.setLocale(locale);
        elContext.addELResolver(new BetterMapELResolver());
        // put in standard functions and variables here
        bindVariable(elContext, "formatter", new FormatterWrapper(locale));
    }


    private static ExpressionFactory loadExpressionSupport() {
        //
        // Do we need fancy class loading support.  The Hibernate Validator code jumps though incredible
        // class loader tricks so should we?  Do they know something we don't?
        //
        // For now lets keep it simple
        //
        return ELManager.getExpressionFactory();
    }

    private void bindMethod(String bindName, String methodName, Class<?>... args) {
        elContext.getFunctionMapper().mapFunction("", bindName, loadMethod(methodName, args));
    }

    private Method loadMethod(String name, Class<?>... args) {
        try {
            return ELSupport.class.getMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private ValueExpression bindVariable(ELContext elContext, String variableName, Object variableValue) {
        ValueExpression valueExpression = expressionFactory.createValueExpression(
                variableValue,
                Object.class
        );
        return elContext.getVariableMapper().setVariable(variableName, valueExpression);
    }

    public boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        return evaluateImpl(expression, variables, Boolean.class);

    }

    public Object evaluate(String expression, Map<String, Object> variables) {
        return evaluateImpl(expression, variables, Object.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T evaluateImpl(String expression, Map<String, Object> variables, Class<T> expectedResultClass) {
        StandardELContext context = new StandardELContext(elContext);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            bindVariable(context, entry.getKey(), entry.getValue());
        }
        ValueExpression result = expressionFactory.createValueExpression(context, expression, expectedResultClass);
        return (T) result.getValue(context);
    }


}
