package py.una.pol.algorithms;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import py.una.pol.model.*;
import py.una.pol.utils.Utils;

import java.math.BigDecimal;
import java.util.*;

public class Algorithms {

    /*
     * Algoritmo para establecer rutas utilizando RSA (Routing and Spectrum Assignment) personzalizado.
     *
     * @param demand: Objeto que representa la demanda de la red.
     * @param net: Grafo de la topología de la red.
     * @param kspaths: Lista de k caminos más cortos en el grafo.
     * @param fsMax: Número máximo de slots de frecuencia disponibles.
     * @param cores: Número de núcleos para la asignación de espectro.
     * @param capacity: Capacidad total del enlace.
     *
     * @return EstablisedRoute: Objeto que representa la ruta establecida con asignación de espectro.
     */
    public static EstablishedRoute findBestRoute(Demand demand, Graph<Integer, Link> network, List<GraphPath<Integer, Link>> shortestPaths, int cores, int capacity, int fsMax, BigDecimal maxCrosstalk, Double crosstalkPerUnitLength) {
        EstablishedRoute establishedRoute = null;
        List<GraphPath<Integer, Link>> kspPlaced = new ArrayList<>();
        List<List<Integer>> kspPlacedCores = new ArrayList<>();
        Integer fsIndexBegin = null;
        Integer selectedIndex = null;
        int k = 0;

        try {
            while (k < shortestPaths.size() && shortestPaths.get(k) != null) {
                GraphPath<Integer, Link> ksp = shortestPaths.get(k);

                if (fsMax < demand.getFs()) {
                    fsMax = demand.getFs();
                }

                for (int i = 0; i <= fsMax - demand.getFs(); i++) {
                    List<Link> freeLinks = new ArrayList<>();
                    List<Integer> kspCores = new ArrayList<>();
                    List<BigDecimal> crosstalkBlockList = new ArrayList<>();

                    for (Link link : ksp.getEdgeList()) {
                        for (int core = 0; core < cores; core++) {
                            int endIndex = Math.min(i + demand.getFs(), link.getCores().get(core).getFs().size());
                            List<FrequencySlot> fsBlock = link.getCores().get(core).getFs().subList(i, i + endIndex);

                            crosstalkBlockList.clear();
                            for (int fsCrosstalkIndex = 0; fsCrosstalkIndex < fsBlock.size(); fsCrosstalkIndex++) {
                                crosstalkBlockList.add(BigDecimal.ZERO);
                            }

                            if (isFSBlockFree(fsBlock)) {
                                if (isFsBlockCrosstalkFree(fsBlock, maxCrosstalk, crosstalkBlockList))
                                    if (isNextToCrosstalkFreeCores(link, maxCrosstalk, core, i, demand.getFs(), crosstalkPerUnitLength)) {
                                        freeLinks.add(link);
                                        kspCores.add(core);
                                        fsIndexBegin = i;
                                        selectedIndex = k;

                                        for (int crosstalkFsListIndex = 0; crosstalkFsListIndex < crosstalkBlockList.size(); crosstalkFsListIndex++) {
                                            BigDecimal crosstalkRuta = crosstalkBlockList.get(crosstalkFsListIndex);
                                            crosstalkRuta = crosstalkRuta.add(Utils.toDB(Utils.XT(Utils.getCantidadVecinos(core), crosstalkPerUnitLength, link.getDistance())));
                                            crosstalkBlockList.set(crosstalkFsListIndex, crosstalkRuta);
                                        }
                                        core = cores;
                                        if (freeLinks.size() == ksp.getEdgeList().size()) {
                                            kspPlaced.add(shortestPaths.get(selectedIndex));
                                            kspPlacedCores.add(kspCores);
                                            k = shortestPaths.size();
                                            i = capacity;
                                        }
                                    }
                            }
                        }
                    }
                }

                fsMax++;
                k++;
            }

            EstablishedRoute establisedRoute;
            if (fsIndexBegin != null && !kspPlaced.isEmpty()) {
                double bestBFR = Double.MAX_VALUE;
                int bestMSI = Integer.MAX_VALUE;
                int bestPathIndex = -1;

                for (int pathIndex = 0; pathIndex < kspPlaced.size(); pathIndex++) {
                    List<Link> pathLinks = kspPlaced.get(pathIndex).getEdgeList();
                    List<Integer> pathCores = kspPlacedCores.get(pathIndex);
                    double pathBFR = Double.MAX_VALUE;
                    int pathMSI = Integer.MAX_VALUE;

                    for (int linkIndex = 0; linkIndex < pathLinks.size(); linkIndex++) {
                        Link link = pathLinks.get(linkIndex);
                        int coreIndex = pathCores.get(linkIndex);
                        double bfr = calculateBFRForCore(link.getCores().get(coreIndex).getFs());
                        int msi = calculateMSIForCore(link.getCores().get(coreIndex).getFs());

                        pathBFR = Math.min(pathBFR, bfr);
                        pathMSI = Math.min(pathMSI, msi);
                    }

                    if (pathBFR < bestBFR || (pathBFR == bestBFR && pathMSI < bestMSI)) {
                        bestBFR = pathBFR;
                        bestMSI = pathMSI;
                        bestPathIndex = pathIndex;
                    }
                }

                if (bestPathIndex != -1) {
                    establisedRoute = new EstablishedRoute(
                            kspPlaced.get(bestPathIndex).getEdgeList(),
                            fsIndexBegin, demand.getFs(), demand.getSource(), demand.getDestination(), kspPlacedCores.get(bestPathIndex)
                    );
                } else {
                    establisedRoute = null;
                }
            } else {
                establisedRoute = null;
            }
            return establisedRoute;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return establishedRoute;
    }


    private static Boolean isFSBlockFree(List<FrequencySlot> bloqueFS) {
        for (FrequencySlot fs : bloqueFS) {
            if (!fs.isFree()) {
                return false;
            }
        }
        return true;
    }

    private static Boolean isFsBlockCrosstalkFree(List<FrequencySlot> fss, BigDecimal maxCrosstalk, List<BigDecimal> crosstalkRuta) {
        for (int i = 0; i < fss.size(); i++) {
            if (i >= crosstalkRuta.size()) {  // Comprobación de desbordamiento
                System.out.println("Desbordamiento del índice: i = " + i + ", crosstalkRuta.size() = " + crosstalkRuta.size());
                continue;  // Saltar a la siguiente iteración
            }

            BigDecimal fsCrosstalk = fss.get(i).getCrosstalk();
            BigDecimal crosstalkActual = crosstalkRuta.get(i).add(fsCrosstalk);

            if (crosstalkActual.compareTo(maxCrosstalk) > 0) {
                return false;
            }
        }
        return true;
    }



    private static Boolean isNextToCrosstalkFreeCores(Link link, BigDecimal maxCrosstalk, Integer core, int fsIndexBegin, int fsWidth, Double crosstalkPerUnitLength) {
        List<Integer> vecinos = Utils.getCoreVecinos(core);
        for (Integer coreVecino : vecinos) {
            List<FrequencySlot> fsVecinoList = link.getCores().get(coreVecino).getFs();
            for (Integer i = fsIndexBegin; i < fsIndexBegin + fsWidth; i++) {
                if (i < 0 || i >= fsVecinoList.size()) {  // Comprobación de desbordamiento
                    System.out.println("Desbordamiento del índice: coreVecino = " + coreVecino + ", i = " + i + ", fsVecinoList.size() = " + fsVecinoList.size());
                    continue;  // Saltar a la siguiente iteración
                }
                FrequencySlot fsVecino = fsVecinoList.get(i);
                if (!fsVecino.isFree()) {
                    BigDecimal crosstalkASumar = Utils.toDB(Utils.XT(Utils.getCantidadVecinos(core), crosstalkPerUnitLength, link.getDistance()));
                    BigDecimal crosstalk = fsVecino.getCrosstalk().add(crosstalkASumar);
                    //BigDecimal crosstalkDB = Utils.toDB(crosstalk.doubleValue());
                    if (crosstalk.compareTo(maxCrosstalk) >= 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public static double calculateBFRForCore(List<FrequencySlot> frequencySlotList) {
        double maxFreeBlockSize = 0; // Inicialmente no hay bloques libres
        double totalFreeSlots = 0; // Inicialmente no hay ranuras libres

        int currentFreeBlockSize = 0; // Para rastrear el tamaño del bloque libre actual

        for (int i = 0; i < frequencySlotList.size(); i++) {
            FrequencySlot fs = frequencySlotList.get(i);
            if (fs.isFree()) {
                // Si la ranura de frecuencia está libre
                totalFreeSlots++; // Aumentar la cantidad total de ranuras libres
                currentFreeBlockSize++; // Aumentar el tamaño del bloque libre actual

                // Actualizar la cantidad máxima de bloques libres si es necesario
                maxFreeBlockSize = Math.max(maxFreeBlockSize, currentFreeBlockSize);
            } else {
                // Si la ranura de frecuencia está ocupada
                currentFreeBlockSize = 0;
            }
        }

        // Calcular el BFR
        return 1 - maxFreeBlockSize / totalFreeSlots;
    }

    public static int calculateMSIForCore(List<FrequencySlot> frequencySlotList) {
        int maxOccupiedSlotIndex = -1; // Inicialmente no hay ranuras ocupadas

        for (int i = 0; i < frequencySlotList.size(); i++) {
            FrequencySlot fs = frequencySlotList.get(i);
            if (!fs.isFree()) {
                // Actualizar el índice del mayor slot ocupado si es necesario
                maxOccupiedSlotIndex = Math.max(maxOccupiedSlotIndex, i);
            }
        }

        return maxOccupiedSlotIndex;
    }

    // Método para imprimir los caminos en kspPlaced
    private static void imprimirCaminos(List<GraphPath> kspPlaced) {
        for (int i = 0; i < kspPlaced.size(); i++) {
            GraphPath<Integer, DefaultWeightedEdge> path = kspPlaced.get(i);
            List<Integer> vertices = path.getVertexList();
            System.out.print("Camino " + (i + 1) + ": ");
            for (int j = 0; j < vertices.size(); j++) {
                System.out.print(vertices.get(j));
                if (j < vertices.size() - 1) {
                    System.out.print(" --> ");
                }
            }
            System.out.println();
        }
    }


}
