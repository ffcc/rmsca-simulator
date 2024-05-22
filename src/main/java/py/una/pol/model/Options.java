package py.una.pol.model;

import java.math.BigDecimal;

public class Options {
    private String topology;
    private float fsWidth;
    private int capacity;
    private int fsRangeMin;
    private int fsRangeMax;
    private int cores;
    private String sortestAlg;
    private String routingAlg;
    private int demandsQuantity;
    private String sortingDemands;
    private BigDecimal maxCrosstalk;
    private Double crosstalkPerUnitLenght;

    public Options(String topology, float fsWidth, int capacity, int fsRangeMin, int fsRangeMax, int cores, String sortestAlg, String routingAlg, int demandsQuantity, String sortingDemands, BigDecimal maxCrosstalk, Double crosstalkPerUnitLenght) {
        this.topology = topology;
        this.fsWidth = fsWidth;
        this.capacity = capacity;
        this.fsRangeMin = fsRangeMin;
        this.fsRangeMax = fsRangeMax;
        this.cores = cores;
        this.sortestAlg = sortestAlg;
        this.routingAlg = routingAlg;
        this.demandsQuantity = demandsQuantity;
        this.sortingDemands = sortingDemands;
        this.maxCrosstalk = maxCrosstalk;
        this.crosstalkPerUnitLenght = crosstalkPerUnitLenght;
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

    public String getSortestAlg() {
        return sortestAlg;
    }

    public void setSortestAlg(String sortestAlg) {
        this.sortestAlg = sortestAlg;
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

    public String getSortingDemands() {
        return sortingDemands;
    }

    public void setSortingDemands(String sortingDemands) {
        this.sortingDemands = sortingDemands;
    }

    public BigDecimal getMaxCrosstalk() {
        return maxCrosstalk;
    }

    public void setMaxCrosstalk(BigDecimal maxCrosstalk) {
        this.maxCrosstalk = maxCrosstalk;
    }

    public Double getCrosstalkPerUnitLenght() {
        return crosstalkPerUnitLenght;
    }

    public void setCrosstalkPerUnitLenght(Double crosstalkPerUnitLenght) {
        this.crosstalkPerUnitLenght = crosstalkPerUnitLenght;
    }
}
