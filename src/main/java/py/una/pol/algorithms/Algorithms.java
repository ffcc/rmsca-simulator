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
                    Candidates bestCandidate = null;
                    List<BigDecimal> crosstalkBlockList = new ArrayList<>();

                    // Se inicializa la lista de valores de crosstalk para cada slot de frecuencia del bloque
                    for (int fsCrosstalkIndex = 0; fsCrosstalkIndex < demand.getFs(); fsCrosstalkIndex++) {
                        crosstalkBlockList.add(BigDecimal.ZERO);
                    }

                    for (Link link : ksp.getEdgeList()) {

                        List<Candidates> candidates = new ArrayList<>();
                        for (int core = 0; core < cores; core++) {
                            // Calcular el índice de fin para evitar desbordamientos
                            int endIndex = Math.min(i + demand.getFs(), link.getCores().get(core).getFs().size());
                            List<FrequencySlot> fsBlock = link.getCores().get(core).getFs().subList(i, i + endIndex);

                            //principio de continuidad, verifica si el bloque tiene todos los FS libres
                            if (isFSBlockFree(fsBlock)) {
                                //verifica el crosstalk en el bloque actual
                                if (isFsBlockCrosstalkFree(fsBlock, maxCrosstalk, crosstalkBlockList))
                                    //verifica el crosstalk en los vecinos
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

                                //calculamos el BFR para el nucleo actual
                                //Candidates candidate = calculateBFRForCore(link.getCores().get(core).getFs());
                                //calculamos el crosstalk para el nucleo actual
                                // candidate.setCrosstalk(calculateCrosstalk());


                                //candidates.add(candidate);


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

                fsMax++;
                k++;

            }

            EstablishedRoute establisedRoute;
            if (fsIndexBegin != null && !kspPlaced.isEmpty()) {
                establisedRoute = new EstablishedRoute(kspPlaced.get(0).getEdgeList(),
                        fsIndexBegin, demand.getFs(), demand.getSource(), demand.getDestination(), kspPlacedCores.get(0));
            } else {
                //System.out.println("Bloqueo");
                establisedRoute = null;
            }
            return establisedRoute;

            /* establecer ruta o retornar null */


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
            for (Integer i = fsIndexBegin; i < fsIndexBegin + fsWidth; i++) {
                FrequencySlot fsVecino = link.getCores().get(coreVecino).getFs().get(i);
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

    public static Candidates calculateBFRForCore(List<FrequencySlot> frequencySlotList) {
        Candidates metrics = new Candidates();

        double maxFreeBlockSize = 0; // Inicialmente no hay bloques libres
        double totalFreeSlots = 0; // Inicialmente no hay ranuras libres
        int maxOccupiedSlotIndex = -1; // Inicialmente no hay ranuras ocupadas

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

                // Actualizar el índice del mayor slot ocupado si es necesario
                maxOccupiedSlotIndex = Math.max(maxOccupiedSlotIndex, i);
            }
        }

        // Asignar los valores calculados al objeto BFR
        metrics.setBfr(1 - maxFreeBlockSize / totalFreeSlots);
        metrics.setMsi(maxOccupiedSlotIndex);

        return metrics;
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
