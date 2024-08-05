package py.una.pol.model;

import java.util.ArrayList;
import java.util.List;

public class EstablishedRoute {
    private int fsIndexBegin;
    private int fs;
    private int from;
    private int to;
    private List<Link> path;
    private List<Integer> pathCores;
    private int fsMax;
    private double bfr;
    private int msi;


    public EstablishedRoute() {
    }
    public EstablishedRoute(List path, int fsIndexBegin, int fs, int from, int to, List<Integer> pathCores, double bfr, int msi) {
        this.path = path;
        this.fsIndexBegin = fsIndexBegin;
        this.fs = fs;
        this.from = from;
        this.to = to;
        this.pathCores = pathCores;
        this.bfr = bfr;
        this.msi = msi;
    }

    public int getFsIndexBegin() {
        return fsIndexBegin;
    }

    public void setFsIndexBegin(int fsIndexBegin) {
        this.fsIndexBegin = fsIndexBegin;
    }

    public int getFs() {
        return fs;
    }

    public void setFs(int fs) {
        this.fs = fs;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public List<Link> getPath() {
        return path;
    }

    public void setPath(List<Link> path) {
        this.path = path;
    }

    public List<Integer> getPathCores() {
        return pathCores;
    }

    public void setPathCores(List<Integer> pathCores) {
        this.pathCores = pathCores;
    }

    public void setFsMax(int fsMax) {
        this.fsMax = fsMax;
    }

    public int getFsMax() {
        return this.fsMax = this.getFsIndexBegin() + this.getFs();
    }

    public int getFsIndexEnd() {
        return (this.fsIndexBegin + this.fs) - 1;
    }

    public double getBfr() {
        return bfr;
    }

    public void setBfr(double bfr) {
        this.bfr = bfr;
    }

    public double getMsi() {
        return msi;
    }

    public void setMsi(int msi) {
        this.msi = msi;
    }


    public String printDemandNodes() {
        List<Integer> pathNodes = new ArrayList<>();
        pathNodes.add(from);

        for (Link link : path) {
            if (link.getFrom() == pathNodes.get(pathNodes.size() - 1)) {
                pathNodes.add(link.getTo());
            } else {
                // Si el enlace no sigue el orden, agrégalo al final para mantener el orden
                pathNodes.add(link.getFrom());
            }
        }

        StringBuilder pathString = new StringBuilder("PATH: ");
        for (int i = 0; i < pathNodes.size(); i++) {
            pathString.append(pathNodes.get(i));
            if (i < pathNodes.size() - 1) {
                pathString.append(" --> ");
            }
        }

        System.out.println(pathString.toString()); // Imprime el camino
        return pathString.toString(); // Retorna el camino como una cadena
    }


}
