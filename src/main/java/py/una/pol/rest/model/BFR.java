package py.una.pol.rest.model;

import org.jgrapht.GraphPath;

public class BFR {
    private GraphPath path;
    private Double value;
    private int indexFs;
    private int core;

    public BFR() {
    }

    public BFR(GraphPath path, Double value, int indexFs, int core) {
        this.path = path;
        this.value = value;
        this.indexFs = indexFs;
        this.core = core;
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
}


