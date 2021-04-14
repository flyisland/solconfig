package com.solace.tools.solconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    public static ObjectMapper objectMapper = new ObjectMapper();

    // TODO: move to SempSpec class
    public static String getCollectionNameFromUri(String uri){
        String[] items = uri.split("/");
        return items[items.length-1].split("\\?")[0];
    }

    public static Optional<String> getFirstMatch(String input, Pattern re) {
        var m = re.matcher(input);
        if (m.find()) {
            return Optional.of(m.group(1));
        }else {
            return Optional.empty();
        }
    }

    public static void log(String text) {
        System.err.println(text);
    }

    public static void err(String format, Object... args) {
        System.err.printf(format, args);
    }

    public static void errPrintlnAndExit(String format, Object... args) {
        errPrintlnAndExit(null, format, args);
    }

    public static void errPrintlnAndExit(Exception e, String format, Object... args) {
        err(format, args);
        err("%n");
        if (Objects.nonNull(e)) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public static Set<Map.Entry<String, Object>> symmetricDiff(Set<Map.Entry<String, Object>> s1, Set<Map.Entry<String, Object>> s2) {
        var symmetricDiff = new HashSet<>(s1);
        symmetricDiff.addAll(s2);
        var tmp = new HashSet<>(s1);
        tmp.retainAll(s2);
        symmetricDiff.removeAll(tmp);

        return symmetricDiff;
    }

    public static String toPrettyJson(Object obj) {
        String result = null;
        try {
            result = Utils.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            errPrintlnAndExit(e, "Unable to convert the object into the json format.");
        }
        return result;
    }

    public static String toPrettyJsonMultiLineArray(Object obj) {
        String result = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            result = objectMapper.writer(prettyPrinter).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            errPrintlnAndExit(e, "Unable to convert the object into the json format.");
        }
        return result;
    }
}
