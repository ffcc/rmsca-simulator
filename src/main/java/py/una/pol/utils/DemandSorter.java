package py.una.pol.utils;

import py.una.pol.rest.model.Demand;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DemandSorter {

    // Ordenar en orden ascendente por distancia
    public static void sortByDistanceAscending(List<Demand> demands) {
        Collections.sort(demands, Comparator.comparingInt(Demand::getDistance));
    }

    // Ordenar en orden descendente por distancia
    public static void sortByDistanceDescending(List<Demand> demands) {
        Collections.sort(demands, Comparator.comparingInt(Demand::getDistance).reversed());
    }
}
