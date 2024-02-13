package sh.grover.dcubed.util;

public class ArrayUtil {

    public static <T> T loopedIndex(T[] arr, int index) {
        var normalizedIndex = (arr.length + index) % arr.length;
        return arr[normalizedIndex];
    }
}
