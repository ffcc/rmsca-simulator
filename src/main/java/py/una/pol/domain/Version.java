package py.una.pol.domain;


import lombok.Getter;

@Getter
public class Version {

    private final String value;

    private final String description;

    public static final Version ORIGINAL = new Version("1.0.0", "Implementación original");
    public static final Version ORIGINAL_FIX = new Version("1.0.1", "Correcciones a la implementación original");
    public static final Version ORDERED_CORE_BFR = new Version("1.2.0", "Recorrido de los cores ordenado por BFR");
    public static final Version ORIGINAL_FIX_MODULATION = new Version("1.0.3", "Corrección del cálculo " +
            "de la modulación basado en la distancia de cada KSP");
    public static final Version ORDERED_CORE_BFR_PERIPHERAL = new Version("1.2.1", "Recorrido de los cores con los periféricos ordenado por BFR");
    public static final Version ORDERED_CORE_BFR_EVEN_ODD = new Version("1.2.2", "Recorrido de los cores con los periféricos pares primero");

    private Version(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
