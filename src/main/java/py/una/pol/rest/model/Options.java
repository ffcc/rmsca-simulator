package py.una.pol.rest.model;

public class Options {
    private int time;
    private String topology;
    private float fsWidth;
    private int capacity;
    private int fsRangeMin;
    private int fsRangeMax;
    private int cores;
    private String metricaDesfrag;
    private String routingAlg;
    private int demandsQuantity;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTopology() {
        return topology;
    }

    public void setTopology(String topology) {
        this.topology = topology;
    }

    public float getFsWidth() {
        return fsWidth;
    }

    public void setFsWidth(float fsWidth) {
        this.fsWidth = fsWidth;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFsRangeMin() {
        return fsRangeMin;
    }

    public void setFsRangeMin(int fsRangeMin) {
        this.fsRangeMin = fsRangeMin;
    }

    public int getFsRangeMax() {
        return fsRangeMax;
    }

    public void setFsRangeMax(int fsRangeMax) {
        this.fsRangeMax = fsRangeMax;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public String getMetricaDesfrag() {
        return metricaDesfrag;
    }

    public void setMetricaDesfrag(String metricaDesfrag) {
        this.metricaDesfrag = metricaDesfrag;
    }

    public String getRoutingAlg() {
        return routingAlg;
    }

    public void setRoutingAlg(String routingAlg) {
        this.routingAlg = routingAlg;
    }

    public int getDemandsQuantity() {
        return demandsQuantity;
    }

    public void setDemandsQuantity(int demandsQuantity) {
        this.demandsQuantity = demandsQuantity;
    }
}
