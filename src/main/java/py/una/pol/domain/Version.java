package py.una.pol.domain;


import lombok.Getter;

@Getter
public class Version {

    private final String value;

    private final String name;

    private final String description;

    public static final Version ORIGINAL = new Version("1.0.0", "ORI00", "Implementación original");
    public static final Version ORIGINAL_FIX = new Version("1.0.1", "ORI01", "Correcciones a la implementación original");

    private Version(String value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }
}
