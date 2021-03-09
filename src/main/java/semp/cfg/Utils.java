package semp.cfg;

public class Utils {
    public static String getCollectionNameFromUri(String uri){
        String[] items = uri.split("/");
        return items[items.length-1].split("\\?")[0];
    }
}
