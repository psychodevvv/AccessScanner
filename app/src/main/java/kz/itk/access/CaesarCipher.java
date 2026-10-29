package kz.itk.access;

public final class CaesarCipher {

    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyz"
                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "аәбвгғдеёжзийкқлмнңоөпрстуұүфхһцчшщъыіьэюя"
                    + "АӘБВГҒДЕЁЖЗИЙКҚЛМНҢОӨПРСТУҰҮФХҺЦЧШЩЪЫІЬЭЮЯ"
                    + "0123456789";

    private static final int SHIFT = 7;

    private CaesarCipher() {
    }

    public static String encrypt(String text) {
        return shift(text, SHIFT);
    }

    public static String decrypt(String text) {
        return shift(text, -SHIFT);
    }

    private static String shift(String text, int shift) {
        StringBuilder result = new StringBuilder();
        int size = ALPHABET.length();
        for (char c : text.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index == -1) {
                result.append(c);
            } else {
                result.append(ALPHABET.charAt(Math.floorMod(index + shift, size)));
            }
        }
        return result.toString();
    }
}
