package kz.itk.access;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Validator {

    private static final String LETTERS = "А-ЯЁӘҒҚҢӨҰҮҺІа-яёәғқңөұүһі";
    private static final Pattern NAME_PATTERN =
            Pattern.compile("[" + LETTERS + "]{2,}(-[" + LETTERS + "]{2,})?");
    private static final Pattern IIN_PATTERN = Pattern.compile("\\d{12}");
    private static final int[] FIRST_WEIGHTS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private static final int[] SECOND_WEIGHTS = {3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2};

    private Validator() {
    }

    public static Profile parseScan(String raw, List<String> errors) {
        String[] parts = raw.trim().split(";", -1);
        if (parts.length != 4) {
            errors.add("QR-код не того формата: нужно 4 поля через ';'");
            return null;
        }

        String surname = parts[0].trim();
        String name = parts[1].trim();
        String patronymic = parts[2].trim();
        String iin = parts[3].trim();

        checkName("Фамилия", surname, errors);
        checkName("Имя", name, errors);
        checkName("Отчество", patronymic, errors);
        String iinError = checkIin(iin);
        if (iinError != null) {
            errors.add("ИИН: " + iinError);
        }

        if (!errors.isEmpty()) {
            return null;
        }
        return new Profile(capitalize(surname), capitalize(name), capitalize(patronymic), iin);
    }

    public static String checkIin(String iin) {
        if (!IIN_PATTERN.matcher(iin).matches()) {
            return "должен состоять ровно из 12 цифр";
        }

        int[] digits = new int[12];
        for (int i = 0; i < 12; i++) {
            digits[i] = iin.charAt(i) - '0';
        }

        int century;
        switch (digits[6]) {
            case 1: case 2: century = 1800; break;
            case 3: case 4: century = 1900; break;
            case 5: case 6: century = 2000; break;
            default: return "7-я цифра (век рождения и пол) должна быть от 1 до 6";
        }

        int year = century + Integer.parseInt(iin.substring(0, 2));
        int month = Integer.parseInt(iin.substring(2, 4));
        int day = Integer.parseInt(iin.substring(4, 6));
        try {
            LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            return "первые 6 цифр не являются настоящей датой рождения";
        }

        int control = weightedSum(digits, FIRST_WEIGHTS) % 11;
        if (control == 10) {
            control = weightedSum(digits, SECOND_WEIGHTS) % 11;
            if (control == 10) {
                return "такой ИИН не может существовать";
            }
        }

        if (control != digits[11]) {
            return "неверная контрольная цифра (опечатка в номере?)";
        }
        return null;
    }

    public static String checkKey(String key) {
        if (key.length() < 4) {
            return "ключ должен быть не короче 4 символов";
        }
        if (key.contains(" ")) {
            return "в ключе не должно быть пробелов";
        }
        return null;
    }

    private static void checkName(String title, String value, List<String> errors) {
        if (!NAME_PATTERN.matcher(value).matches()) {
            errors.add(title + ": только буквы (можно через дефис), минимум 2");
        }
    }

    private static int weightedSum(int[] digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += digits[i] * weights[i];
        }
        return sum;
    }

    private static String capitalize(String value) {
        StringBuilder result = new StringBuilder();
        for (String part : value.split("-")) {
            if (result.length() > 0) {
                result.append('-');
            }
            String lower = part.toLowerCase(Locale.ROOT);
            result.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
        }
        return result.toString();
    }
}
