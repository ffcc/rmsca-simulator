package py.una.pol.algorithms;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import py.una.pol.domain.KspPath;
import py.una.pol.domain.Simulation;
import py.una.pol.model.*;
import py.una.pol.utils.DemandsGenerator;
import py.una.pol.utils.Utils;

import java.math.BigDecimal;
import java.util.*;

public class Algorithms {

    /*
     * Algoritmo para establecer rutas utilizando RMSCA (Routing, Modulation, Spectrum and Core Assignment) personzalizado.
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
    public static EstablishedRoute findBestRoute(Simulation simulation, Demand demand,
                                                 List<GraphPath<Integer, Link>> shortestPaths, int cores, int capacity,
                                                 int fsMax, BigDecimal maxCrosstalk, Double crosstalkIndividual) {
        var simulationKspPaths = simulation.getDemand(demand.getId()).getKspPaths();
        EstablishedRoute establishedRoute = null;
        List<GraphPath<Integer, Link>> kspPlaced = new ArrayList<>();
        List<List<Integer>> kspPlacedCores = new ArrayList<>();
        Integer fsIndexBegin = null;
        Integer selectedIndex = null;
        int k = 0;

        try {
            while (k < shortestPaths.size() && shortestPaths.get(k) != null) {
                GraphPath<Integer, Link> ksp = shortestPaths.get(k);
                var simulationKspPath = simulationKspPaths.get(k);

                //calcula la modulacion y fs de la demanda mediante k
                boolean isModulationValid =  DemandsGenerator.calculateValidModulationAndDemandFs(simulation, demand, ksp, simulationKspPath);

                // Si la modulación no es válida, pasar al siguiente k
                if (!isModulationValid) {
                    k++;
                    simulationKspPath.setStatus(KspPath.KspPathStatus.REJECTED);
                    continue;  // Saltar al siguiente k si isModulationValid es falso
                }

                // Actualizar fsMax si es menor que la demanda
                fsMax = Math.max(fsMax, demand.getFs());

                boolean foundPath = false;

                while (!foundPath && fsMax <= capacity - demand.getFs()) {
                    for (int i = 0; i <= fsMax; i++) {
                        List<Link> freeLinks = new ArrayList<>();
                        List<Integer> kspCores = new ArrayList<>();

                        for (Link link : ksp.getEdgeList()) {
                            for (int core = 0; core < cores; core++) {
                                int endIndex = Math.min(i + demand.getFs(), link.getCores().get(core).getFs().size());
                                List<FrequencySlot> fsBlock = link.getCores().get(core).getFs().subList(i, endIndex);

                                if (isFSBlockFree(fsBlock)
                                        && isFsBlockCrosstalkFree(link, core, i, fsBlock.size(), crosstalkIndividual, maxCrosstalk)
                                        && isNeighborFsBlockCrosstalkFree(link, maxCrosstalk, core, i, demand.getFs(), crosstalkIndividual)) {
                                    freeLinks.add(link);
                                    kspCores.add(core);
                                    fsIndexBegin = i;
                                    selectedIndex = k;
                                    core = cores;
                                    if (freeLinks.size() == ksp.getEdgeList().size()) {
                                        kspPlaced.add(shortestPaths.get(selectedIndex));
                                        kspPlacedCores.add(kspCores);
                                        i = capacity;
                                        foundPath = true;
                                        simulationKspPath.setStatus(KspPath.KspPathStatus.CANDIDATE);
                                        simulationKspPath.getCores().addAll(kspCores);
                                    }
                                }
                            }
                        }
                    }

                    fsMax++;
                }

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

                    var kspPath = simulationKspPaths.get(pathIndex);
                    kspPath.setBfr(pathBFR);
                    kspPath.setMsi(pathMSI);

                    if (pathBFR < bestBFR || (pathBFR == bestBFR && pathMSI < bestMSI)) {
                        bestBFR = pathBFR;
                        bestMSI = pathMSI;
                        bestPathIndex = pathIndex;
                    }
                }

                if (bestPathIndex != -1) {
                    simulationKspPaths.get(bestPathIndex).setStatus(KspPath.KspPathStatus.ESTABLISHED);
                    establisedRoute = new EstablishedRoute(kspPlaced.get(bestPathIndex).getEdgeList(),
                            fsIndexBegin, demand.getFs(), demand.getSource(), demand.getDestination(),
                            kspPlacedCores.get(bestPathIndex), fsMax, bestBFR, bestMSI);
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

    private static Boolean isFsBlockCrosstalkFree(Link link, int core, int fsBlockStartIndex, int fsBlockSize,
                                                  Double crosstalkLinealIndividual,
                                                  BigDecimal maxCrosstalk) {
        List<Integer> neighborCores = Utils.getCoreVecinos(core);
        for (int i = fsBlockStartIndex; i < (fsBlockStartIndex + fsBlockSize); i++) {
            int neighborCoresActives = 0;
            for (int neighborCore : neighborCores) {
                neighborCoresActives += link.getCores().get(neighborCore).getFs().get(i).isFree() ? 0 : 1;
            }

            BigDecimal fsCrosstalkTotal = Utils.toDB(Utils.XT(neighborCoresActives, crosstalkLinealIndividual, link.getDistance()));
            if (fsCrosstalkTotal.compareTo(maxCrosstalk) > 0) {
                return false;
            }
        }
        return true;
    }


    private static Boolean isNeighborFsBlockCrosstalkFree(Link link, BigDecimal maxCrosstalk, Integer core,
                                                          int fsIndexBegin, int fsWidth, Double crosstalkIndividual) {
        List<Integer> neighborCores = Utils.getCoreVecinos(core);
        for (Integer neighborCore : neighborCores) {
            List<FrequencySlot> neighborFsList = link.getCores().get(neighborCore).getFs();
            for (int i = fsIndexBegin; i < fsIndexBegin + fsWidth; i++) {
                FrequencySlot neighborFs = neighborFsList.get(i);
                if (!neighborFs.isFree()) {
                    BigDecimal crosstalkASumar = Utils.toDB(Utils.XT(1, crosstalkIndividual, link.getDistance()));
                    BigDecimal crosstalk = neighborFs.getCrosstalk().add(crosstalkASumar);
                    if (crosstalk.compareTo(maxCrosstalk) >= 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public static double calculateBFRForCore(List<FrequencySlot> frequencySlotList) {
        int maxFreeBlockSize = 0; // Inicialmente no hay bloques libres
        int totalFSBusy = 0;

        int currentFreeBlockSize = 0; // Para rastrear el tamaño del bloque libre actual

        for (int i = 0; i < frequencySlotList.size(); i++) {
            FrequencySlot fs = frequencySlotList.get(i);
            if (fs.isFree()) {
                // Si la ranura de frecuencia está libre
                currentFreeBlockSize++; // Aumentar el tamaño del bloque libre actual

                // Actualizar la cantidad máxima de bloques libres si es necesario
                maxFreeBlockSize = Math.max(maxFreeBlockSize, currentFreeBlockSize);
            } else {
                // Si la ranura de frecuencia está ocupada
                currentFreeBlockSize = 0;
                totalFSBusy++;
            }
        }

        // Calcular el BFR
        return 1.0 - (double) maxFreeBlockSize / (frequencySlotList.size() - totalFSBusy);
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
