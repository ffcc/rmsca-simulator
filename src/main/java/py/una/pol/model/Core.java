package py.una.pol.model;

import java.util.ArrayList;
import java.util.List;

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

    public Core(double bandwidth, List<FrequencySlot> fs) {
        this.bandwidth = bandwidth;
        this.fs = fs;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public List<FrequencySlot> getFs() {
        return fs;
    }

    public void setFs(List<FrequencySlot> fs) {
        this.fs = fs;
    }

}
