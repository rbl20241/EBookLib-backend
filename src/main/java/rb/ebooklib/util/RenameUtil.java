package rb.ebooklib.util;

import java.util.ArrayList;

public class RenameUtil {

    public static ArrayList<SeparatorTable> getSeparators() {
        ArrayList<SeparatorTable> separators = new ArrayList<>();

        separators.add(createSeparator("HYPHEN", "-"));
        separators.add(createSeparator("COMMA", ","));
        separators.add(createSeparator("SPACE", " "));

        return separators;
    }

    public static ArrayList<FormatTable> getFormats() {
        ArrayList<FormatTable> formatTables = new ArrayList<>();

        formatTables.add(createFormat("tva", "<titel> <voornaam> <achternaam>"));
        formatTables.add(createFormat("tav", "<titel> <achternaam> <voornaam>"));
        formatTables.add(createFormat("vat", "<voornaam> <achternaam> <titel>"));
        formatTables.add(createFormat("avt", "<achternaam> <voornaam> <titel>"));

        return formatTables;
    }

    private static SeparatorTable createSeparator(final String name, final String value) {
        SeparatorTable sep = new SeparatorTable();
        sep.name = name;
        sep.value = value;
        return sep;
    }

    private static FormatTable createFormat(final String name, final String value) {
        FormatTable format = new FormatTable();
        format.name = name;
        format.value = value;
        return format;
    }

}
