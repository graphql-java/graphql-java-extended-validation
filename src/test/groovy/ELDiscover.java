import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import javax.el.ELContext;
import javax.el.ELManager;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import javax.el.ValueExpression;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Playing with El and Hibernate Message Interpolation
 */
public class ELDiscover {

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


    private static void elDirect() {
        ExpressionFactory expressionFactory = ELManager.getExpressionFactory();
        ELContext elContext = new StandardELContext(expressionFactory);

        ValueExpression valueExpression = expressionFactory.createValueExpression(
                "TheValueValue",
                Object.class
        );
        elContext.getVariableMapper().setVariable("validatedValue", valueExpression);


        runElExpression("${validatedValue}", expressionFactory, elContext);
        runElExpression("${validatedValue.length()}", expressionFactory, elContext);
        runElExpression("#{cube=(x->x*x*x);cube(2)}", expressionFactory, elContext);
        runElExpression("${cube=(x->x*x*x);cube(2)}", expressionFactory, elContext);
        runElExpression("${! empty validatedValue}", expressionFactory, elContext);
        runElExpression("${['1','2','3']}", expressionFactory, elContext);
        runElExpression("${{'one':'1','two':'2','three':'3'}}", expressionFactory, elContext);
        runElExpression("${['1','2','3'].stream().filter(x-> x>1).toList()}", expressionFactory, elContext);

    }

    public static void main(String[] args) {

        elDirect();

        Map<String, Object> attributes = new HashMap<>();
        ConstraintAnnotationDescriptor.Builder<NotNull> constraintBuilder
                = new ConstraintAnnotationDescriptor.Builder<>(NotNull.class, attributes);

//        ConstraintDescriptorImpl<NotNull> constraintDescriptor = new ConstraintDescriptorImpl<>(
//                new ConstraintHelper(),
//                null,
//                constraintBuilder.build(),
//                ElementType.FIELD
//        );

        ConstraintDescriptorImpl<NotNull> constraintDescriptor = new ConstraintDescriptorImpl<>(
                new ConstraintHelper(),
                null,
                constraintBuilder.build(),
                ConstraintLocation.ConstraintLocationKind.FIELD,
                ConstraintDescriptorImpl.ConstraintType.GENERIC
        );

        //        ConstraintDescriptorImpl<NotNull> constraintDescriptor = new ConstraintDescriptorImpl<>(
//                new ConstraintHelper(),
//                null,
//                constraintBuilder.build(),
//                ElementType.FIELD
//        );

        User user = new User();
        user.setAge(18);

        ResourceBundleMessageInterpolator messageInterpolator = new ResourceBundleMessageInterpolator();

        PathImpl rootPath = PathImpl.createRootPath();

        MessageInterpolator.Context context = new MessageInterpolatorContext(
                constraintDescriptor,
                user,
                null,
                rootPath,
                Collections.<String, Object>emptyMap(),
                Collections.<String, Object>emptyMap()
        );


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
}
