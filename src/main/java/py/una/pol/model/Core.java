package py.una.pol.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Core {
    private double bandwidth;
    private List<FrequencySlot> fs;
    private double bfr;

    public Core(double bandwidth, int fs) {
        this.bandwidth = bandwidth;
        this.fs = new ArrayList<>();
        for (int i = 0; i < fs; i++){
            this.fs.add(new FrequencySlot(bandwidth/fs));
        }
    }
}
