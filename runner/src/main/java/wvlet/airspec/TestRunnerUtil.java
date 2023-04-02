package wvlet.airspec;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class TestRunnerUtil {
    // from ServiceMessage
    private static final String FORMAT_WITHOUT_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(FORMAT_WITHOUT_TZ);

    public static void reportMessage(String message) {
        //new line prefix needed cause there can be some user unflushed output
        System.out.println("\n" + message);
    }

    public static String escapeString(String str) {
        if (str == null) return "";
        return str
                .replaceAll("[|]", "||")
                .replaceAll("[']", "|'")
                .replaceAll("[\n]", "|n")
                .replaceAll("[\r]", "|r")
                .replaceAll("]","|]")
                .replaceAll("\\[","|[");
    }

    public static String formatCurrentTimestamp() {
        Date date = new Date();
        return formatTimestamp(date);
    }

    public static String formatTimestamp(Date date) {
        return TIMESTAMP_FORMAT.format(date);
    }

//    public static String actualExpectedAttrs(String actual, String expected) {
//        return " expected='" + escapeString(expected) + "' actual='" + escapeString(actual) + "' ";
//    }

    public static String getStacktrace(Throwable t) {
        StringWriter out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        return out.toString();
    }
}
