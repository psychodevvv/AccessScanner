package kz.itk.access;

public enum Field {
    SURNAME("Фамилия"),
    NAME("Имя"),
    PATRONYMIC("Отчество"),
    MESSAGES("Сообщения"),
    IIN("ИИН");

    private final String title;

    Field(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
