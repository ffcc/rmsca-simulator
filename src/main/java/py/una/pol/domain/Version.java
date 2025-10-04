package py.una.pol.domain;


import lombok.Getter;

@Getter
public class Version {

    private final String value;

    private final String description;

    public static final Version ORIGINAL = new Version("1.0.0", "Implementación original");
    public static final Version ORIGINAL_FIX = new Version("1.0.1", "Correcciones a la implementación original");
    public static final Version ORIGINAL_FIX_CROSSTALK = new Version("1.0.2", "Corrección al cálculo del crosstalk");
    public static final Version ORIGINAL_FIX_MODULATION = new Version("1.0.3", "Corrección del cálculo " +
            "de la modulación basado en la distancia de cada KSP");

    private Version(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
