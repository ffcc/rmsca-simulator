package py.una.pol.rest.model;

public class FrecuencySlot {
    private boolean free;
    private double fsWidh;

    public FrecuencySlot(double fsWidh) {
        this.fsWidh = fsWidh;
        this.free = true;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public double getFsWidh() {
        return fsWidh;
    }

    public void setFsWidh(double fsWidh) {
        this.fsWidh = fsWidh;
    }

}
