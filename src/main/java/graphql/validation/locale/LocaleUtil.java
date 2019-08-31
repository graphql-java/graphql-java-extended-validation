package graphql.validation.locale;

import graphql.Internal;
import graphql.schema.DataFetchingEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocaleUtil {

    /**
     * This will try to determine the Locale from the data fetching env in a number of ways, searching
     * via the context and source objects and the data fetching environment itself.  This plugs a gap while
     * graphql-java does not have a getLocale on ExecutionInput / DataFetchingEnvironment
     *
     * @param environment   the fetching env
     * @param defaultLocale the default to use
     *
     * @return a Locale
     */
    public static Locale determineLocale(DataFetchingEnvironment environment, Locale defaultLocale) {
        //
        // in a future version of graphql java the DFE will have the Locale but in the mean time
        Locale locale;
        locale = extractLocale(environment);
        if (locale == null) {
            locale = extractLocale(environment.getContext());
            if (locale == null) {
                locale = extractLocale(environment.getSource());
                if (locale == null) {
                    locale = extractLocale(environment.getRoot());
                    if (locale == null) {
                        locale = defaultLocale;
                    }
                }
            }
        }
        return locale;
    }

    private static Locale extractLocale(Object object) {
        if (object != null) {
            if (object instanceof LocaleProvider) {
                return ((LocaleProvider) object).getLocale();
            }
            return reflectGetLocale(object);
        }
        return null;
    }

    private static final Map<Class, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class, Class> FAILED_CLASS_CACHE = new ConcurrentHashMap<>();

    @Internal
    public static void clearMethodCaches() {
        METHOD_CACHE.clear();
        FAILED_CLASS_CACHE.clear();
    }

    private static Locale reflectGetLocale(Object object) {
        Class<?> clazz = object.getClass();
        if (FAILED_CLASS_CACHE.containsKey(clazz)) {
            return null;
        }
        try {
            Method getLocaleMethod = METHOD_CACHE.get(clazz);
            if (getLocaleMethod == null) {
                getLocaleMethod = clazz.getMethod("getLocale");
                if (Locale.class.equals(getLocaleMethod.getReturnType())) {
                    METHOD_CACHE.put(clazz, getLocaleMethod);
                } else {
                    getLocaleMethod = null; // wat - very tricksy hobbit??
                }
            }
            if (getLocaleMethod != null) {
                return (Locale) getLocaleMethod.invoke(object);
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
        }
        FAILED_CLASS_CACHE.put(clazz, clazz);
        return null;
    }


}
