package rb.ebooklib.util;


public class StringUtil {

    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    public static boolean isBlank(String text) {
        if (isEmpty(text)) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(String text) {
        return (text == null) || (text.length() == 0);
    }

    public static boolean endsWithIgnoreCase(String source, String suffix) {
        if (isEmpty(suffix)) {
            return true;
        }
        if (isEmpty(source)) {
            return false;
        }
        if (suffix.length() > source.length()) {
            return false;
        }
        return source.substring(source.length() - suffix.length())
                .toLowerCase().endsWith(suffix.toLowerCase());
    }

    public static String defaultIfNull(String text) {
        return defaultIfNull(text, "");
    }

    private static String defaultIfNull(String text, final String defaultValue) {
        if (text == null) {
            return defaultValue;
        }
        return text;
    }

    public static boolean equals(String text1, String text2) {
        if (text1 == null) {
            return (text2 == null);
        }
        return text1.equals(text2);
    }

    public static String toString(Object... keyValues) {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(keyValues[i]);
            result.append(": ");
            Object value = null;
            if ((i + 1) < keyValues.length) {
                value = keyValues[i + 1];
            }
            if (value == null) {
                result.append("<null>");
            } else {
                result.append('\'');
                result.append(value);
                result.append('\'');
            }
        }
        result.append(']');
        return result.toString();
    }

    public static int hashCode(String... values) {
        int result = 31;
        for (String value : values) {
            result ^= String.valueOf(value).hashCode();
        }
        return result;
    }

    public static String substringBefore(String text, char separator) {
        if (isEmpty(text)) {
            return text;
        }
        int sepPos = text.indexOf(separator);
        if (sepPos < 0) {
            return text;
        }
        return text.substring(0, sepPos);
    }

    public static String substringBeforeLast(String text, char separator) {
        if (isEmpty(text)) {
            return text;
        }
        int cPos = text.lastIndexOf(separator);
        if (cPos < 0) {
            return text;
        }
        return text.substring(0, cPos);
    }

    public static String substringAfterLast(String text, char separator) {
        if (isEmpty(text)) {
            return text;
        }
        int cPos = text.lastIndexOf(separator);
        if (cPos < 0) {
            return "";
        }
        return text.substring(cPos + 1);
    }

    public static String substringAfter(String text, char c) {
        if (isEmpty(text)) {
            return text;
        }
        int cPos = text.indexOf(c);
        if (cPos < 0) {
            return "";
        }
        return text.substring(cPos + 1);
    }

    public static String startWithCapital(final String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static int nrOfChars(final String str, final char c) {
        int count = 0;
        int i = 0;
        while (i < str.length()) {
            if (str.charAt(i) == c) {
                count++;
            }
            i++;
        }
        return count;
    }

}
