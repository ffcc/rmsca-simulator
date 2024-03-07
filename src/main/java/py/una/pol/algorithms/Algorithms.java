package py.una.pol.algorithms;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import py.una.pol.model.*;

import java.util.*;

public class Algorithms {

    /*
     * Algoritmo para establecer rutas utilizando RSA (Routing and Spectrum Assignment).
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
    public static EstablishedRoute findBestRoute(Demand demand, Graph<Integer, Link> network, List<GraphPath<Integer, Link>> shortestPaths, int cores, int capacity, int fsMax) {
        EstablishedRoute establishedRoute = null;
        int k = 0;

        try {
            while (k < shortestPaths.size() && shortestPaths.get(k) != null) {
                GraphPath<Integer, Link> ksp = shortestPaths.get(k);

                if (fsMax < demand.getFs()) {
                    fsMax = demand.getFs();
                }

                for (int i = 0; i <= fsMax - demand.getFs(); i++) {
                    List<Link> enlacesLibres = new ArrayList<>();
                    List<Integer> kspCores = new ArrayList<>();
                    List<BFR> listaBfr = new ArrayList<>();
                    List<Integer> listMsi = new ArrayList<>();

                    for (Link link : ksp.getEdgeList()) {
                        for (int core = 0; core < cores; core++) {
                            // Calcular el índice de fin para evitar desbordamientos
                            int endIndex = Math.min(i + demand.getFs(), link.getCores().get(core).getFs().size());
                            List<FrequencySlot> bloqueFS = link.getCores().get(core).getFs().subList(i, i + endIndex);

                            //principio de continuidad
                            if (isFSBlockFree(bloqueFS)) {

                                /* calcular bfr por nucleo */
                                /* calcular msi por nucleo */
                                /* calcular el crosstalk */
                                /* verificar contiguidad */

                                /* insertar si cumple con todas las condiciones */
                                int j = i + 1;
                            }
                        }

                    }

                }


                fsMax++;
                k++;

            }

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





    public static BFR calculateBFR(Graph<Integer, Link> graph, GraphPath<Integer, Link> path, Demand demand,
                                   int capacity, int core, int totalCores) {
        boolean[] occupiedSlots = new boolean[capacity]; // Representa los fs ocupados del espectro de todos los enlaces.
        BFR bfr = new BFR();
        int msi = -1;

        Arrays.fill(occupiedSlots, false); // Se inicializa todo el espectro como libre

        // Verifica los fs y establece los slots ocupados como true
        for (int i = 0; i < capacity; i++) {
            for (Link link : path.getEdgeList()) {
                FrequencySlot fs = link.getCores().get(core).getFs().get(i);
                if (!fs.isFree()) {
                    occupiedSlots[i] = true;
                }
            }
        }

        // Encuentra la secuencia de slots libres
        int start = 0, count = 0;
        for (int i = 0; i < capacity; i++) {
            count = occupiedSlots[i] ? 0 : count + 1;
            if (count == demand.getFs()) {
                start = i - count + 1;
                break;
            }
        }

        // Cálculo del MSI
        for (int i = 0; i < capacity; i++) {
            if (occupiedSlots[i]) {
                msi = i;
            }
        }

        // Verifica si se encontró una secuencia de slots libres
        if (count == demand.getFs()) {
            bfr.setValue(calculateBFRForPath(path, totalCores, core)); // Calcula el BFR para el GraphPath actual
            bfr.setIndexFs(start); // Establece el índice donde comienza el espacio libre
            bfr.setPath(path);
            bfr.setCore(core);
            bfr.setMsi(msi);
        }

        return bfr;
    }

    public static boolean isCrosstalkAware(BFR bfr, Graph<Integer, Link> graph, Demand demand, int capacity) {
        // Implementa la lógica para verificar si el BFR cumple con la restricción de crosstalk aware
        // Puedes utilizar el BFR, el grafo, la demanda y la capacidad para realizar los cálculos necesarios.
        // Retorna true si el BFR cumple con la restricción, false en caso contrario.
        return true;
    }

    public static double BFRLinks(List<Link> links, int capacity) {
        double ocuppiedSlotCount = 0;
        double freeBlockSize = 0;
        double maxBlock = 0;
        double BFRLinks = 0;
        int contadorCore = 0;
        int contadorLink = 0;
        for (Link link : links) {
            contadorLink++;
            //System.out.println("Link N°: " + contadorLink);
            contadorCore = 0;
            for (Core core : link.getCores()) {
                contadorCore++;
                ocuppiedSlotCount = 0;
                freeBlockSize = 0;
                maxBlock = 0;
                // System.out.println("Core N°: " + contadorCore);
                for (int i = 0; i < capacity; i++) {
                    if (core.getFs().get(i).isFree()) {
                        freeBlockSize++;
                    } else {
                        if (freeBlockSize > maxBlock) {
                            maxBlock = freeBlockSize;
                        }
                        freeBlockSize = 0;
                        ocuppiedSlotCount++;
                    }
                }

                if (freeBlockSize > maxBlock) {
                    maxBlock = freeBlockSize;
                }
                if (capacity != ocuppiedSlotCount) BFRLinks += (1 - maxBlock / (capacity - ocuppiedSlotCount));
            }

        }

        return BFRLinks;
    }

    public static double BFR(Graph g, int capacity) {
        double BFRLinks = 0;
        int cores;

        List<Link> links = new ArrayList<>();
        links.addAll(g.edgeSet());
        cores = links.get(0).getCores().size();
        BFRLinks = BFRLinks(links, capacity);
        return BFRLinks / g.edgeSet().size() * cores;
    }

    public static double graphUsePercentage(Graph graph) {
        double total = 0;
        double occup = 0;
        List<Link> links = new ArrayList<>();
        links.addAll(graph.edgeSet());
        for (Link link : links) {
            for (Core core : link.getCores()) {
                for (int i = 0; i < core.getFs().size(); i++) {
                    if (!core.getFs().get(i).isFree()) occup++;
                    total++;
                }
            }
        }

        return occup / total;
    }

    public static double externalFragmentation(Graph graph, int capacity) {
        double ef = 0;
        int blocksFreeC;
        int maxBlockFree;
        int currentBlockFree;
        List<Link> links = new ArrayList<>();
        links.addAll(graph.edgeSet());
        for (Link link : links) {
            for (Core core : link.getCores()) {
                maxBlockFree = 0;
                blocksFreeC = 0;
                currentBlockFree = 0;
                for (FrequencySlot fs : core.getFs()) {
                    if (fs.isFree()) {
                        //if(currentBlockFree == 0)
                        blocksFreeC++;//Contador global de slots libre
                        currentBlockFree++;//Contador slots libre del bloque actual
                    } else {
                        if (currentBlockFree > maxBlockFree) maxBlockFree = currentBlockFree;
                        currentBlockFree = 0;
                    }
                }
                if (maxBlockFree == 0 && currentBlockFree == capacity)//Para el caso en el que todo el espectro esta libre
                    maxBlockFree = capacity;

                if (maxBlockFree == 0 && blocksFreeC == 0)//Para el caso en el que todo el espectro esta ocupado
                    blocksFreeC = 1;

                if (maxBlockFree == 0 && currentBlockFree != capacity && currentBlockFree != 0)//Para el caso en el que solo se encuentra 1 bloque libre
                    maxBlockFree = currentBlockFree;

                ef += 1 - ((double) maxBlockFree / (double) blocksFreeC);
            }
        }

        return ef / (links.size() * links.get(0).getCores().size());
    }

    public static double shf(Graph graph, int capacity) {
        double shf = 0;
        double sf = 0;
        List<Link> links = new ArrayList<>();
        links.addAll(graph.edgeSet());
        for (Link link : links) {
            for (Core core : link.getCores()) {
                sf = 0;
                for (FrequencySlot fs : core.getFs()) {
                    if (fs.isFree()) sf++;
                    else {
                        if (sf != 0)  //hasta 1   *  [0, 5.86]
                            shf += ((sf / capacity) * Math.log(capacity / sf));
                        sf = 0;
                    }
                }
                sf = sf;
                if (sf != 0) shf += ((sf / capacity) * Math.log(capacity / sf));
            }
        }
        return shf / links.size() * links.get(0).getCores().size();
    }

    public static double calculateBFRForPath(GraphPath gPath, int capacity, int coreIndex) {
        List<Link> links = new ArrayList<>();
        links.addAll(gPath.getEdgeList());

        // Calcula el BFR para cada enlace en el camino y el núcleo específico
        double totalBFR = 0;

        for (Link link : links) {
            if (coreIndex >= 0 && coreIndex < link.getCores().size()) {
                Core core = link.getCores().get(coreIndex);
                double BFR = calculateBFRForLink(core, capacity);
                totalBFR += BFR;
            }
        }

        int numLinksWithCore = getNumLinksWithCore(links, coreIndex);
        double averageBFR = (numLinksWithCore > 0) ? totalBFR / numLinksWithCore : 0;

        return averageBFR;
    }

    public static double calculateBFRForLink(Core core, int capacity) {
        List<FrequencySlot> fs = core.getFs();
        double maxFreeBlockSize = 0;
        double fsFree = 0;
        double freeBlockSize = 0;
        boolean inFreeBlock = false;

        for (FrequencySlot slot : fs) {
            if (slot.isFree()) {
                freeBlockSize++;
                fsFree++;
                inFreeBlock = true;
            } else {
                if (inFreeBlock) {
                    maxFreeBlockSize = Math.max(maxFreeBlockSize, freeBlockSize);
                    freeBlockSize = 0;
                    inFreeBlock = false;
                }
            }
        }

        // Asegura que el último bloque libre se registre si es el más grande
        if (inFreeBlock) {
            maxFreeBlockSize = Math.max(maxFreeBlockSize, freeBlockSize);
        }

        double coreBFR = (fsFree > 0) ? 1 - (maxFreeBlockSize / fsFree) : 0;

        return coreBFR;
    }


    public static int getNumLinksWithCore(List<Link> links, int coreIndex) {
        int numLinks = 0;
        for (Link link : links) {
            if (coreIndex >= 0 && coreIndex < link.getCores().size()) {
                numLinks++;
            }
        }
        return numLinks;
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
