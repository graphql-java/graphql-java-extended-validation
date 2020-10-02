package graphql.validation.schemawiring;

import graphql.GraphQLError;
import graphql.schema.*;
import graphql.validation.interpolation.MessageInterpolator;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.TargetedValidationRules;
import graphql.validation.rules.ValidationRule;
import graphql.validation.rules.ValidationRules;
import graphql.validation.util.Util;

import java.util.List;
import java.util.Locale;

public class FieldValidatorDataFetcher implements DataFetcher<Object> {
    private final OnValidationErrorStrategy errorStrategy;
    private final MessageInterpolator messageInterpolator;
    private final DataFetcher<?> defaultDataFetcher;
    private final Locale defaultLocale;
    private final ValidationRules validationRules;
    private TargetedValidationRules applicableRules;

    public FieldValidatorDataFetcher(OnValidationErrorStrategy errorStrategy,
                                     MessageInterpolator messageInterpolator,
                                     DataFetcher<?> defaultDataFetcher,
                                     Locale defaultLocale,
                                     ValidationRules validationRules) {
        this.errorStrategy = errorStrategy;
        this.messageInterpolator = messageInterpolator;
        this.defaultDataFetcher = defaultDataFetcher;
        this.defaultLocale = defaultLocale;
        this.validationRules = validationRules;
        this.applicableRules = null;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        if (!wereApplicableRulesFetched()) {
            fetchApplicableRules(environment);
        }

        // When no validation is performed, this data fetcher is a pass-through
        if (applicableRules.isEmpty()) {
            return defaultDataFetcher.get(environment);
        }

        List<GraphQLError> errors = applicableRules.runValidationRules(environment, messageInterpolator, defaultLocale);
        if (!errors.isEmpty()) {
            if (!errorStrategy.shouldContinue(errors, environment)) {
                return errorStrategy.onErrorValue(errors, environment);
            }
        }

        Object returnValue = defaultDataFetcher.get(environment);
        if (errors.isEmpty()) {
            return returnValue;
        }
        return Util.mkDFRFromFetchedResult(errors, returnValue);
    }

    private void fetchApplicableRules(DataFetchingEnvironment environment) {
        final GraphQLFieldDefinition field = environment.getFieldDefinition();
        final GraphQLFieldsContainer container = asContainer(environment);

        applicableRules = validationRules.buildRulesFor(field, container);
    }

    private GraphQLFieldsContainer asContainer(DataFetchingEnvironment environment) {
        final GraphQLType parent = environment.getParentType();
        if (parent == null) {
            return null;
        }
        return (GraphQLFieldsContainer) environment.getParentType();
    }

    private boolean wereApplicableRulesFetched() {
        return applicableRules != null;
    }
}
