package py.una.pol.model;

import java.math.BigDecimal;

public class FrequencySlot {
    private boolean free;
    private double fsWidh;
    private BigDecimal crosstalk;

    public FrequencySlot(double fsWidh) {
        this.fsWidh = fsWidh;
        this.free = true;
        this.crosstalk = BigDecimal.ZERO;
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

    public BigDecimal getCrosstalk() {
        return crosstalk;
    }

    public void setCrosstalk(BigDecimal crosstalk) {
        this.crosstalk = crosstalk;
    }
}
