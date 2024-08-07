package py.una.pol.model;

import java.math.BigDecimal;

public class Options {
    private String topology;
    private float fsWidth;
    private int capacity;
    private int cores;
    private String shortestAlg;
    private int demandsQuantity;
    private String sortingDemands;
    private BigDecimal maxCrosstalk;
    private Double crosstalkPerUnitLenght;

    public Options(String topology, float fsWidth, int capacity, int cores, String shortestAlg, int demandsQuantity, String sortingDemands, BigDecimal maxCrosstalk, Double crosstalkPerUnitLenght) {
        this.topology = topology;
        this.fsWidth = fsWidth;
        this.capacity = capacity;
        this.cores = cores;
        this.shortestAlg = shortestAlg;
        this.demandsQuantity = demandsQuantity;
        this.sortingDemands = sortingDemands;
        this.maxCrosstalk = maxCrosstalk;
        this.crosstalkPerUnitLenght = crosstalkPerUnitLenght;
    }

    public String getTopology() {
        return topology;
    }

    public float getFsWidth() {
        return fsWidth;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCores() {
        return cores;
    }

    public String getShortestAlg() {
        return shortestAlg;
    }

    public int getDemandsQuantity() {
        return demandsQuantity;
    }

    public String getSortingDemands() {
        return sortingDemands;
    }

    public BigDecimal getMaxCrosstalk() {
        return maxCrosstalk;
    }

    public Double getCrosstalkPerUnitLenght() {
        return crosstalkPerUnitLenght;
    }

}
