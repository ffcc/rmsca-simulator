package py.una.pol.model;

import org.jgrapht.GraphPath;

public class BFR {
    private GraphPath path;
    private Double value;
    private int indexFs;
    private int core;
    private boolean used;
    private int msi;

    public BFR() {
    }

    public BFR(GraphPath path, Double value, int indexFs, int core, int msi) {
        this.path = path;
        this.value = value;
        this.indexFs = indexFs;
        this.core = core;
        this.msi = msi;
    }

    public GraphPath getPath() {
        return path;
    }

    public void setPath(GraphPath path) {
        this.path = path;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public int getIndexFs() {
        return indexFs;
    }

    public void setIndexFs(int indexFs) {
        this.indexFs = indexFs;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public int getMsi() {
        return msi;
    }

    public void setMsi(int msi) {
        this.msi = msi;
    }
}


