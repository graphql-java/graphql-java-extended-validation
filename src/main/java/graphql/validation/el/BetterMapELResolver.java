package graphql.validation.el;

import graphql.Internal;

import javax.el.ELContext;
import javax.el.MapELResolver;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Internal
public class BetterMapELResolver extends MapELResolver {
    public static boolean containsOneOf(Map map, List<Object> keys) {
        int count = 0;
        for (Object key : keys) {
            if (map.get(key) != null) {
                count++;
            }
        }
        return count == 1;
    }

    public static boolean containsAllOf(Map map, List<Object> keys) {
        for (Object key : keys) {
            if (map.get(key) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (base instanceof Map) {
            Map map = (Map) base;
            if ("containsOneOf" .equals(method)) {
                context.setPropertyResolved(true);
                return containsOneOf(map, Arrays.asList(params));
            }
            if ("containsAllOf" .equals(method)) {
                context.setPropertyResolved(true);
                return containsAllOf(map, Arrays.asList(params));
            }
        }
        // delegate back to underlying Map resolver
        return super.invoke(context, base, method, paramTypes, params);
    }
}
