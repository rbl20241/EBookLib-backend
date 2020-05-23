package rb.ebooklib.util;

import java.util.Collection;
import java.util.Map;

/**
 * Helper class to check if an object is nullOrEmpty
 */
public class NullOrEmptyUtil {

    /**
     * Helper method to check if an object is null or empty
     *
     * @param item the item needed to be checked
     * @return Returns true if the object is null or empty
     */
    public static <T> boolean isNullOrEmpty(T item) {
        return item == null || item.toString().trim().isEmpty();
    }

    /**
     * Helper method to check if a Collection is null or empty
     * Lists and Sets are subSections of a collection
     *
     * @param item the item needed to be checked
     * @return Returns true if the set is null or empty
     */
    public static <T> boolean isNullOrEmpty(Collection<T> item) {
        return item == null || item.isEmpty();
    }

    /**
     * Helper method to check if a ObjectList is null or empty
     *
     * @param item the item needed to be checked
     * @return Returns true if the objectList is null or empty
     */
    public static <T> boolean isNullOrEmpty(T[] item) {
        return item == null || item.length <= 0;
    }

    /**
     * Helper method to check if a map is null or empty
     *
     * @param item the item needed to be checked
     * @return Returns true if the set is null or empty
     */
    public static <T,V> boolean isNullOrEmpty(Map<T,V> item) {
        return item == null || item.isEmpty();
    }
}
