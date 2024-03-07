package py.una.pol.utils;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import py.una.pol.algorithms.ModulationCalculator;
import py.una.pol.algorithms.ShortestPathFinder;
import py.una.pol.model.Demand;
import py.una.pol.model.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DemandsGenerator {

    private final static int MAX_DISTANCE_THRESHOLD = 8000;

    public static List<Demand> generateAndValidateDemands(int demandsQuantity, Graph<Integer, Link> net, ShortestPathFinder shortestPathFinder) {
        List<Demand> generatedDemands = new ArrayList<>();
        int cantNodos = net.vertexSet().size();
        int rejectedDemandsCount = 0;

        for (int i = 0; i < demandsQuantity; i++) {
            int attempts = 0;

            while (true) {
                // Generar una demanda
                Demand newDemand = generateSingleDemand(cantNodos);

                // Validar la demanda
                if (validateDemand(newDemand, shortestPathFinder)) {
                    // La demanda es válida, agregarla a la lista
                    generatedDemands.add(newDemand);
                    break; // Salir del bucle de intentos
                }

                attempts++;
                rejectedDemandsCount++;

            }  // Repetir hasta generar una demanda válida
        }

        System.out.println("Número de demandas rechazadas: " + rejectedDemandsCount + " Total de demandas: " + demandsQuantity);

        return generatedDemands;
    }

    public static Demand generateSingleDemand(int cantNodos) {
        int source, destination, randomBitRate;
        int[] bitRates = {10, 40, 100, 400};

        Random rand = new Random();
        source = rand.nextInt(cantNodos);
        destination = rand.nextInt(cantNodos);

        // Ensure source and destination are different
        while (source == destination) {
            destination = rand.nextInt(cantNodos);
        }

        // Ajustar la probabilidad de seleccionar tasas de bits entre 10 y 40
        double probability = rand.nextDouble();

        if (probability < 0.7) {
            // El 70% de las veces, seleccionar una tasa de bits entre 10 y 40
            randomBitRate = bitRates[0] + rand.nextInt(2) * 30;
        } else {
            // El 30% de las veces, seleccionar una tasa de bits de todo el conjunto
            randomBitRate = bitRates[rand.nextInt(bitRates.length)];
        }

        return new Demand(source, destination, randomBitRate);
    }


    public static boolean validateDemand(Demand demand, ShortestPathFinder pathFinder) {
        int source = demand.getSource();
        int destination = demand.getDestination();

        // Obtener el camino más corto para la demanda
        GraphPath<Integer, Link> shortestPath = pathFinder.getShortestPath(source, destination);

        // Validar la distancia del camino más corto
        double distance = shortestPath.getWeight();
        if (distance > MAX_DISTANCE_THRESHOLD) {
            System.out.println("La distancia entre nodos es demasiado grande, demanda rechazada.");
            return false;
        }

        ModulationCalculator modulationCalculator = new ModulationCalculator();
        boolean fsCalculated = modulationCalculator.calculateFS(demand);

        if (!fsCalculated) {
            return false;
        }

        // Si pasa todas las validaciones, la demanda es aceptada
        return true;
    }

}
