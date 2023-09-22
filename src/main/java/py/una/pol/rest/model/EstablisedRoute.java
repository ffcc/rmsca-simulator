package py.una.pol.rest.model;

import java.util.List;

public class EstablisedRoute {
    private int fsIndexBegin;
    private int fs;
    private int from;
    private int to;
    private List<Link> path;
    private int core;

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

    public int getFsIndexEnd() {
        return (this.fsIndexBegin + this.fs) - 1;
    }

    public String printDemandNodes() {
        StringBuilder pathString = new StringBuilder("PATH: " + from);

        int previousNode = from;

        for (Link link : path) {
            int currentNode = link.getTo();
            if (previousNode != currentNode) {
                pathString.append(" --> ").append(currentNode);
                previousNode = currentNode;
            }
        }

        // Asegurarse de que el último nodo coincida con 'to' en EstablisedRoute
        if (!path.isEmpty()) {
            Link lastLink = path.get(path.size() - 1);
            if (lastLink.getTo() != to) {
                pathString.append(" --> ").append(to);
            }
        }

        System.out.println(pathString.toString()); // Imprime el camino
        return pathString.toString(); // Retorna el camino como una cadena
    }

}
