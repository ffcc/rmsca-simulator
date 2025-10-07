package py.una.pol.exception;

public class ModulationNotFoundException extends Exception {

    public ModulationNotFoundException(int bitRate, int distance) {
        super(String.format("Modulation not found for bitRate %d and distance %d", bitRate, distance));
    }
}
