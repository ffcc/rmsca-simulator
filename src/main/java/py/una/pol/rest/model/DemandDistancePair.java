package py.una.pol.rest.model;

public class DemandDistancePair implements Comparable<DemandDistancePair> {
    private Demand demand;
    private double distance;
    private String modulation;
    private double fs;

    public DemandDistancePair(Demand demand, double distance, String modulation) {
        this.demand = demand;
        this.distance = distance;
        this.modulation = modulation;
    }

    public void setDemand(Demand demand) {
        this.demand = demand;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getModulation() {
        return modulation;
    }

    public void setModulation(String modulation) {
        this.modulation = modulation;
    }

    public double getDistance() {
        return distance;
    }

    public Demand getDemand() {
        return demand;
    }

    public double getFs() {
        return fs;
    }

    public void setFs(double fs) {
        this.fs = fs;
    }

    @Override
    public int compareTo(DemandDistancePair other) {
        // Orden descendente basado en las distancias
        return Double.compare(other.distance, this.distance);
    }
}
