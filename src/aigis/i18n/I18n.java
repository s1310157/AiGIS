package aigis.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class I18n {

    private static Locale locale = Locale.JAPANESE;

    private static ResourceBundle bundle =
            loadBundle(locale);

    public static void setLocale(Locale newLocale) {
        locale = newLocale;
        bundle = loadBundle(locale);
    }

    private static ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle(
                "aigis.i18n.messages",
                locale,
                new UTF8Control()
        );
    }

    public static String t(String key) {
        return bundle.getString(key);
    }

    public static Locale getLocale() {
        return locale;
    }

    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(
                String baseName,
                Locale locale,
                String format,
                ClassLoader loader,
                boolean reload)
                throws IOException {

            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");

            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) return null;
                return new PropertyResourceBundle(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                );
            }
        }
    }
}
