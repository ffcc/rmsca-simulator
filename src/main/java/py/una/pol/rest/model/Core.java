package py.una.pol.rest.model;

import java.util.ArrayList;
import java.util.List;

public class Core {
    private double bandwidth;
    private List<FrecuencySlot> fs;

    public Core(double bandwidth, int fs) {
        this.bandwidth = bandwidth;
        this.fs = new ArrayList<>();
        for (int i = 0; i < fs; i++){
            this.fs.add(new FrecuencySlot(bandwidth/fs));
        }
    }

    public Core(double bandwidth, List<FrecuencySlot> fs) {
        this.bandwidth = bandwidth;
        this.fs = fs;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public List<FrecuencySlot> getFs() {
        return fs;
    }

    public void setFs(List<FrecuencySlot> fs) {
        this.fs = fs;
    }

}
