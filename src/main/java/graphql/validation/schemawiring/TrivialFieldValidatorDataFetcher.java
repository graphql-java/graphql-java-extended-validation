package graphql.validation.schemawiring;

import graphql.TrivialDataFetcher;
import graphql.schema.DataFetcher;
import graphql.validation.interpolation.MessageInterpolator;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.ValidationRules;

import java.util.Locale;

public class TrivialFieldValidatorDataFetcher extends FieldValidatorDataFetcher implements TrivialDataFetcher<Object> {
    public TrivialFieldValidatorDataFetcher(OnValidationErrorStrategy errorStrategy, MessageInterpolator messageInterpolator, DataFetcher<?> defaultDataFetcher, Locale defaultLocale, ValidationRules validationRules) {
        super(errorStrategy, messageInterpolator, defaultDataFetcher, defaultLocale, validationRules);
    }
}
