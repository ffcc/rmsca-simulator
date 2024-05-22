package py.una.pol.model;

import org.jgrapht.GraphPath;

public class Candidates {
    private GraphPath path;
    private int indexFs;
    private int core;
    private double bfr;
    private int msi;
    private double crosstalk;

    public Candidates() {
    }

    public Candidates(GraphPath path, int indexFs, int core, double bfr, int msi, double crosstalk) {
        this.path = path;
        this.indexFs = indexFs;
        this.core = core;
        this.bfr = bfr;
        this.msi = msi;
        this.crosstalk = crosstalk;
    }

    public GraphPath getPath() {
        return path;
    }

    public void setPath(GraphPath path) {
        this.path = path;
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

    public double getBfr() {
        return bfr;
    }

    public void setBfr(double bfr) {
        this.bfr = bfr;
    }

    public int getMsi() {
        return msi;
    }

    public void setMsi(int msi) {
        this.msi = msi;
    }

    public double getCrosstalk() {
        return crosstalk;
    }

    public void setCrosstalk(double crosstalk) {
        this.crosstalk = crosstalk;
    }
}


