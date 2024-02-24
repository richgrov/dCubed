package sh.grover.dcubed.util;

public class ArrayUtil {

    public static <T> T loopedIndex(T[] arr, int index) {
        var normalizedIndex = (arr.length + index) % arr.length;
        return arr[normalizedIndex];
    }

    public static int indexOfHighest(int[] arr) {
        var highestValue = 0;
        var highestIndex = -1;
        for (var index = 0; index < arr.length; index++) {
            var value = arr[index];
            if (value > highestValue) {
                highestValue = value;
                highestIndex = index;
            }
        }
        return highestIndex;
    }
}
