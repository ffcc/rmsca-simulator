package py.una.pol.rest.model;

public class Demand {
    private int source;
    private int destination;
    private int fs;
    private int bitRate;
    private int timeLife;
    private boolean blocked;

    public Demand(int source, int destination) {
        this.source = source;
        this.destination = destination;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getFs() {
        return fs;
    }

    public void setFs(int fs) {
        this.fs = fs;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getTimeLife() {
        return timeLife;
    }

    public void setTimeLife(int timeLife) {
        this.timeLife = timeLife;
    }

    public boolean getBlocked() {
        return this.blocked;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

}
