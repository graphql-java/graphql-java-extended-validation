import jakarta.el.ELContext;
import jakarta.el.ELManager;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Playing with ELSupport and Hibernate Message Interpolation
 */
public class ELDiscover {

    public static boolean eitherImpl(Object x, Object y) {
        if (x == null) {
            return y != null;
        }
        if (y == null) {
            return true;
        }
        return false;
    }

    private static void elDirect() throws NoSuchMethodException {
        ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
        StandardELContext elContext = new StandardELContext(expressionFactory);

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("arg1", "argVal1");
        args.put("arg2", "argVal2");
        bindVariable(expressionFactory, elContext, "validatedValue", "Value");
        bindVariable(expressionFactory, elContext, "args", args);

        Method method = ELDiscover.class.getMethod("eitherImpl", Object.class, Object.class);

        elContext.getFunctionMapper().mapFunction("", "either", method);

        runElExpression("${either(2,null)}", expressionFactory, elContext);
        runElExpression("${either(2,3)}", expressionFactory, elContext);
        runElExpression("${either(null,3)}", expressionFactory, elContext);


        runElExpression("${either=((x,y)->! empty x && empty y); either(2,null)}", expressionFactory, elContext);


        runElExpression("${validatedValue}", expressionFactory, elContext);


        runElExpression("${validatedValue.length()}", expressionFactory, elContext);
        runElExpression("#{cube=(x->x*x*x);cube(2)}", expressionFactory, elContext);
        runElExpression("${cube=(x->x*x*x);cube(2)}", expressionFactory, elContext);
        runElExpression("${! empty validatedValue}", expressionFactory, elContext);
        runElExpression("${['1','2','3']}", expressionFactory, elContext);
        runElExpression("${{'one':'1','two':'2','three':'3'}}", expressionFactory, elContext);
        runElExpression("${['1','2','3'].stream().filter(x-> x>1).toList()}", expressionFactory, elContext);

    }

    private static ValueExpression bindVariable(ExpressionFactory expressionFactory, ELContext elContext, String variableName, Object variableValue) {
        ValueExpression valueExpression = expressionFactory.createValueExpression(
                variableValue,
                Object.class
        );
        return elContext.getVariableMapper().setVariable(variableName, valueExpression);
    }

    public static void main(String[] args) throws Exception {

        elDirect();

        Map<String, Object> attributes = new HashMap<>();
        ConstraintAnnotationDescriptor.Builder<NotNull> constraintBuilder
                = new ConstraintAnnotationDescriptor.Builder<>(NotNull.class, attributes);

        ConstraintDescriptorImpl<NotNull> constraintDescriptor = new ConstraintDescriptorImpl<>(
                ConstraintHelper.forAllBuiltinConstraints(),
                null,
                constraintBuilder.build(),
                ConstraintLocationKind.FIELD,
                ConstraintType.GENERIC
        );


        User user = new User();
        user.setAge(18);

        ResourceBundleMessageInterpolator messageInterpolator = new ResourceBundleMessageInterpolator();

        PathImpl rootPath = PathImpl.createRootPath();

        ExpressionLanguageFeatureLevel level = ExpressionLanguageFeatureLevel.BEAN_METHODS;
        MessageInterpolator.Context context = new MessageInterpolatorContext(
                constraintDescriptor, user, null, rootPath,
                Collections.emptyMap(), Collections.emptyMap(),
                level,
                true);


        print("${validatedValue.age}", messageInterpolator, context);
        print("${validatedValue}", messageInterpolator, context);
        print("[1,2,3,4].stream().sum()", messageInterpolator, context);
        print("${4.0>= 3}", messageInterpolator, context);
        print("${! empty validatedValue}", messageInterpolator, context);
        print("#{cube=(x->x*x*x);cube(2)}", messageInterpolator, context);

    }

    private static void runElExpression(String expression, ExpressionFactory expressionFactory, ELContext elContext) {
        ValueExpression result = expressionFactory.createValueExpression(elContext, expression, String.class);

        String resolvedExpression = (String) result.getValue(elContext);
        System.out.printf("EL -> %s : %s\n", expression, resolvedExpression);
    }

    private static void print(String expression, ResourceBundleMessageInterpolator messageInterpolator, MessageInterpolator.Context context) {
        try {
            String s = messageInterpolator.interpolate(expression, context);
            System.out.printf("MsgInterpoltation -> %s : %s\n", expression, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class EitherEL {
        Object x, y;

        public EitherEL() {
        }


    }

    public static class User {
        @Email
        private String email;

        @Range(min = 18, max = 21)
        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
