package py.una.pol.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class FrequencySlot {
    private boolean free;
    private double fsWidth;
    private BigDecimal crosstalk;
    private Map<Integer, BigDecimal> accumulatedCrosstalk;

    public FrequencySlot(double fsWidth) {
        this.fsWidth = fsWidth;
        this.free = true;
        this.crosstalk = BigDecimal.ZERO;
    }

    public void accumulateCrosstalk(int fsIndex, BigDecimal crosstalk) {
        accumulatedCrosstalk.put(fsIndex, accumulatedCrosstalk.get(fsIndex).add(crosstalk));
    }
}
