package Utils;

public class ConversionHelper {
    public ConversionHelper() {}

    public static String sanitizeEnumName(String fieldName) {
        fieldName = fieldName.replace("&", "__AND__");
        fieldName = fieldName.replace("/", "__FS__");
        fieldName = fieldName.replace("-", "__MINUS__");
        fieldName = fieldName.replace("+", "__PLUS__");
        fieldName = fieldName.replace("(", "__FRB__");
        fieldName = fieldName.replace(")", "__BRB__");
        fieldName = fieldName.replace("[", "__FSB__");
        fieldName = fieldName.replace("]", "__BSB__");
        fieldName = fieldName.replace(" ", "__SP__");
        fieldName = fieldName.replace("#", "__SHARP__");
        fieldName = fieldName.replace("!", "__BANG__");
        fieldName = fieldName.replace("ó", "__OI__");
        fieldName = fieldName.replace("í", "__II__");
        fieldName = fieldName.replace(".", "__DOT__");
        fieldName = fieldName.replace("'", "__APO__");

        return fieldName;
    }

    public static String formatEnumEntryName(String enumEntryName) {
        return enumEntryName;
    }

    public static String formatMessageEntryName(String messageEntryName) {
        return messageEntryName;
    }
}
