package gg.modl.minecraft.core.locale;

import java.util.Map;

public final class YamlMessageResolver {

    public YamlMessageResolver() {
    }

    @SuppressWarnings("unchecked")
    public static Object lookup(Map<String, Object> root, String path) {
        Object current = root;
        for (String key : path.split("\\.")) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(key);
        }
        return current;
    }

    public static String applyPlaceholders(String template, Map<String, String> placeholders) {
        if (placeholders == null) return template;
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public static String render(String template, Map<String, String> placeholders) {
        return applyPlaceholders(template, placeholders);
    }
}