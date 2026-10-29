package kz.itk.access;

public class Profile {
    public final String surname;
    public final String name;
    public final String patronymic;
    public final String iin;

    public Profile(String surname, String name, String patronymic, String iin) {
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.iin = iin;
    }
}
