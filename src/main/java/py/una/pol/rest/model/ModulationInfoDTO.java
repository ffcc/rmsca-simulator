package py.una.pol.rest.model;

public class ModulationInfoDTO {
    private double anchoDeBanda;
    private int cantidadDeFs;

    // Getters and setters

    public double getAnchoDeBanda() {
        return anchoDeBanda;
    }

    public void setAnchoDeBanda(double anchoDeBanda) {
        this.anchoDeBanda = anchoDeBanda;
    }

    public int getCantidadDeFs() {
        return cantidadDeFs;
    }

    public void setCantidadDeFs(int cantidadDeFs) {
        this.cantidadDeFs = cantidadDeFs;
    }
}