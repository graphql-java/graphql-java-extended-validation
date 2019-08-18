package graphql.validation.interpolation;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.ExecutionPath;
import graphql.schema.GraphQLDirective;
import graphql.validation.el.StandardELVariables;
import graphql.validation.rules.ValidationEnvironment;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This message interpolator will try to convert message templates into I18N messages and then run message property replacement
 * and expression interpolation.
 * <p>
 *
 * By default this looks for a resource bundle file called "ValidationMessages.properties" on the class path but you can can
 * override {@link #getResourceBundle(java.util.Locale)} to provide your own resource bundle
 * <p>
 * If it finds no resources then it will use the message template as is and do parameter and expression replacement
 * on it
 * <p>
 * This class is heavily inspired by the Hibernate Validator projects ResourceBundleMessageInterpolator implementation and in fact
 * uses that in its implementation and hence the standard facilities such as well known "parameters" like "validatedValue" and "format"
 * are available.
 * <p>
 * See the <a href="https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-interpolation-with-message-expressions">Hibernate Validation documentation </a> for more details
 */
public class ResourceBundleMessageInterpolator implements MessageInterpolator {

    private ResourceBundleLocator userResourceBundleLocator = new PlatformResourceBundleLocator("ValidationMessages");
    private ResourceBundleLocator systemResourceBundleLocator = new PlatformResourceBundleLocator("graphql.validation.ValidationMessages");
    private Locale defaultLocale = Locale.getDefault();

    /**
     * Override this method to build your own ErrorClassification
     *
     * @param messageTemplate       the message template
     * @param messageParams         the parameters
     * @param validationEnvironment the rule environment
     *
     * @return an ErrorClassification
     */
    @SuppressWarnings("unused")
    protected ErrorClassification buildErrorClassification(String messageTemplate, Map<String, Object> messageParams, ValidationEnvironment validationEnvironment) {
        ExecutionPath fieldOrArgumentPath = validationEnvironment.getValidatedPath();
        GraphQLDirective directive = validationEnvironment.getContextObject(GraphQLDirective.class);
        return new ValidationErrorType(fieldOrArgumentPath, directive);
    }

    /**
     * You can override this to provide your own resource bundles for a given locale
     *
     * @param locale the locale in question
     *
     * @return a resource bundle OR null if you don't have one
     */
    @SuppressWarnings("unused")
    protected ResourceBundle getResourceBundle(Locale locale) {
        return null;
    }

    @Override
    public GraphQLError interpolate(String messageTemplate, Map<String, Object> messageParams, ValidationEnvironment validationEnvironment) {

        ErrorClassification errorClassification = buildErrorClassification(messageTemplate, messageParams, validationEnvironment);
        String message = interpolateMessageImpl(messageTemplate, messageParams, validationEnvironment);

        GraphqlErrorBuilder errorBuilder = GraphqlErrorBuilder.newError()
                .message(message)
                .errorType(errorClassification)
                .path(validationEnvironment.getExecutionPath());
        if (validationEnvironment.getLocation() != null) {
            errorBuilder.location(validationEnvironment.getLocation());
        }
        return errorBuilder.build();
    }

    private String interpolateMessageImpl(String messageTemplate, Map<String, Object> messageParams, ValidationEnvironment validationEnvironment) {
        Locale locale = validationEnvironment.getLocale() == null ? defaultLocale : validationEnvironment.getLocale();
        messageTemplate = loadMessageResource(messageTemplate, locale);

        MessageInterpolatorContext context = buildHibernateContext(messageParams, validationEnvironment);
        if (locale == null) {
            // let hibernate code do the local defaulting
            return hibernateInterpolator().interpolate(messageTemplate, context);
        } else {
            return hibernateInterpolator().interpolate(messageTemplate, context, locale);
        }
    }

    private String loadMessageResource(String messageTemplate, Locale locale) {
        ResourceBundle resourceBundle = getResourceBundle(locale);
        Optional<String> bundleMessage = loadMessageFromBundle(messageTemplate, resourceBundle);
        if (!bundleMessage.isPresent()) {
            bundleMessage = loadMessageFromBundle(messageTemplate, userResourceBundleLocator.getResourceBundle(locale));
            if (!bundleMessage.isPresent()) {
                bundleMessage = loadMessageFromBundle(messageTemplate, systemResourceBundleLocator.getResourceBundle(locale));
            }
        }
        return bundleMessage.orElse(messageTemplate);
    }

    private Optional<String> loadMessageFromBundle(String messageTemplate, ResourceBundle resourceBundle) {
        if (resourceBundle == null) {
            return Optional.empty();
        }
        try {
            //noinspection ConstantConditions
            return Optional.ofNullable(resourceBundle.getString(messageTemplate));
        } catch (MissingResourceException ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private MessageInterpolatorContext buildHibernateContext(Map<String, Object> messageParams, ValidationEnvironment validationEnvironment) {

        Object validatedValue = validationEnvironment.getValidatedValue();

        ConstraintAnnotationDescriptor<BridgeAnnotation> annotationDescriptor
                = new ConstraintAnnotationDescriptor.Builder<>(BridgeAnnotation.class).build();

        ConstraintDescriptorImpl<BridgeAnnotation> constraintDescriptor
                = new ConstraintDescriptorImpl<>(
                new ConstraintHelper(), null,
                annotationDescriptor, ElementType.FIELD
        );

        Map<String, Object> expressionVariables = StandardELVariables.standardELVars(validationEnvironment);

        Class<?> rootBeanType = null;
        return new MessageInterpolatorContext(
                constraintDescriptor, validatedValue, rootBeanType,
                messageParams, expressionVariables);
    }

    private org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator hibernateInterpolator() {
        return new org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator();
    }

    /// we just need an annotation to compile - we never use its
    @SuppressWarnings("unused")
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Constraint(validatedBy = {})
    private @interface BridgeAnnotation {

        String message() default "";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    private static class ValidationErrorType implements ErrorClassification {
        private final ExecutionPath fieldOrArgumentPath;
        private final GraphQLDirective directive;

        public ValidationErrorType(ExecutionPath fieldOrArgumentPath, GraphQLDirective directive) {
            this.fieldOrArgumentPath = fieldOrArgumentPath;
            this.directive = directive;
        }

        @Override
        public Object toSpecification(GraphQLError error) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", "ExtendedValidationError");
            map.put("validatedPath", fieldOrArgumentPath);
            if (directive != null) {
                map.put("constraint", "@" + directive.getName());
            }
            return map;
        }
    }
}
