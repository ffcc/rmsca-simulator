package py.una.pol.rest.model;

public class FrecuencySlot {
    private int lifetime;
    private boolean free;
    private double fsWidh;

    public FrecuencySlot(double fsWidh) {
        this.fsWidh = fsWidh;
        this.lifetime = 0;
        this.free = true;
    }
}
