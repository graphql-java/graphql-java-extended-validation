package graphql.validation.locale;

import java.util.Locale;

/**
 * An object that can give back a locale
 */
public interface LocaleProvider {

    /**
     * @return a locale to be used by validation rules
     */
    Locale getLocale();
}
