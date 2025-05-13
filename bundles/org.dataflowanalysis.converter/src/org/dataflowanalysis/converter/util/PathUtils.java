package org.dataflowanalysis.converter.util;

public class PathUtils {
    /**
     * Normalizes the given path string by removing quotes and adding the correct extension, if the input does not have a
     * normalized path string
     * @param input Given input path string
     * @param extension Correct extension the path should have
     * @return Normalized path string without quotes and a file extension
     */
    public static String normalizePathString(String input, String extension) {
        input = PathUtils.normalizePathString(input);
        if (!input.endsWith(extension))
            input = input + extension;
        return input;
    }

    /**
     * Normalizes the given path string by removing quotes
     * @param input Given input path string
     * @return Normalized path string without quotes
     */
    public static String normalizePathString(String input) {
        if (input.startsWith("\""))
            input = input.substring(1);
        if (input.endsWith("\""))
            input = input.substring(0, input.length() - 1);
        return input;
    }
}
