package py.una.pol.exception;

public class ModulationNotSupportedException extends Exception {

    public ModulationNotSupportedException(int fsRequired) {
        super(String.format("Required FS (%d) not supported", fsRequired));
    }
}
