package kz.itk.access;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public enum Role {
    OWNER("Я (владелец)", true,
            Field.SURNAME, Field.NAME, Field.PATRONYMIC, Field.MESSAGES, Field.IIN),
    SOMEONE("Кто-то", false,
            Field.SURNAME, Field.NAME, Field.PATRONYMIC),
    GOV("Гос. орган", false,
            Field.SURNAME, Field.NAME, Field.PATRONYMIC, Field.IIN);

    private final String title;
    private final boolean needsBiometrics;
    private final Set<Field> access;

    Role(String title, boolean needsBiometrics, Field... fields) {
        this.title = title;
        this.needsBiometrics = needsBiometrics;
        this.access = EnumSet.copyOf(Arrays.asList(fields));
    }

    public String getTitle() {
        return title;
    }

    public boolean needsBiometrics() {
        return needsBiometrics;
    }

    public boolean canSee(Field field) {
        return access.contains(field);
    }
}
