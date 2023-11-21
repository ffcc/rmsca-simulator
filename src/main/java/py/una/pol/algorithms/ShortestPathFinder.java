package py.una.pol.algorithms;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import py.una.pol.rest.model.Link;

import java.util.ArrayList;
import java.util.List;

public class ShortestPathFinder {

    private Graph<Integer, Link> net;
    private KShortestSimplePaths<Integer, Link> ksp;
    private DijkstraShortestPath<Integer, Link> djkt;
    private List<GraphPath<Integer, Link>> kspaths;

    public ShortestPathFinder(Graph<Integer, Link> net) {
        this.net = net;
        this.ksp = new KShortestSimplePaths<>(net);
        this.djkt = new DijkstraShortestPath<>(net);
        this.kspaths = new ArrayList<>();
    }

    public GraphPath<Integer, Link> getShortestPath(int source, int destination) {
        // Obtener y devolver el camino más corto DIJKSTRA
        return (GraphPath<Integer, Link>) djkt.getPath(source, destination);
    }

    public List<GraphPath<Integer, Link>> getKShortestPaths(int source, int destination, int k) {
        // Obtener y devolver los k caminos más cortos KSP
        return ksp.getPaths(source, destination, k);
    }

    public double getShortestDistance(int source, int destination) {
        // Calcular y devolver la distancia más corta entre dos nodos
        GraphPath<Integer, Link> shortestPath = djkt.getPath(source, destination);
        return shortestPath.getWeight();
    }
}
