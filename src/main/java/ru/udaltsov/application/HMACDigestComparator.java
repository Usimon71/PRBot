package ru.udaltsov.application;

public class HMACDigestComparator {
    public static boolean compare(String actualHash, String expectedHash) {
        if (actualHash.length() != expectedHash.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < actualHash.length(); i++) {
            result |= actualHash.charAt(i) ^ expectedHash.charAt(i);
        }

        return result == 0;
    }
}
