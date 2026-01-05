package aigis.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {

    private static Locale locale = Locale.JAPANESE;
    private static ResourceBundle bundle =
            ResourceBundle.getBundle("i18n.messages", locale);

    public static void setLocale(Locale newLocale) {
        locale = newLocale;
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public static String t(String key) {
        return bundle.getString(key);
    }

    public static Locale getLocale() {
        return locale;
    }
}
