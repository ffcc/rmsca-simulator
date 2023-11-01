package py.una.pol.rest.model;

import java.util.ArrayList;
import java.util.List;

public class EstablisedRoute {
    private int fsIndexBegin;
    private int fs;
    private int from;
    private int to;
    private List<Link> path;
    private int core;
    private int fsMax;

    public EstablisedRoute() {
    }
    public EstablisedRoute(List path, int fsIndexBegin, int fs, int from, int to, int core) {
        this.path = path;
        this.fsIndexBegin = fsIndexBegin;
        this.fs = fs;
        this.from = from;
        this.to = to;
        this.core = core;
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

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public int getFsMax() {
        return this.fsMax = this.getFsIndexBegin() + this.getFs();
    }

    public int getFsIndexEnd() {
        return (this.fsIndexBegin + this.fs) - 1;
    }

    public String printDemandNodes() {
        List<Integer> pathNodes = new ArrayList<>();
        pathNodes.add(from);

        for (Link link : path) {
            if (link.getFrom() == pathNodes.get(pathNodes.size() - 1)) {
                pathNodes.add(link.getTo());
            } else {
                // Si el enlace no sigue el orden, agrégalo al principio para invertirlo
                pathNodes.add(0, link.getFrom());
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
