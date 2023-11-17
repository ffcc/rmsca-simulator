package py.una.pol.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.web.bind.annotation.*;
import py.una.pol.algorithms.Algorithms;
import py.una.pol.algorithms.ModulationCalculator;
import py.una.pol.rest.model.*;
import py.una.pol.utils.ResourceReader;
import py.una.pol.utils.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class SimuladorController {

    @PostMapping(path = "/simular")
    public List<Response> simular(@RequestBody Options options) throws Exception {
        List<Demand> demands;
        List<EstablisedRoute> establishedRoutes = new ArrayList<>();
        //se crea la topoligia con los parámetros seleccionados
        Graph<Integer, Link> net = createTopology(options.getTopology(), options.getCores(), options.getFsWidth(), options.getCapacity());
        List<BFR> listaBfr;
        int fsMax = 0;
        int previousFSMax = 0;
        int demandsQ = 0, blocksQ = 0;

        //se generan aleatoriamente las demandas, de acuerdo a la cantidad proporcionadas por parámetro
        demands = Utils.generateDemands(options.getDemandsQuantity(), options.getFsRangeMin(), options.getFsRangeMax(), net.vertexSet().size());

        //se carga la red en ksp - dijkstra, con todos los nodos, pares de nodos
        KShortestSimplePaths ksp = new KShortestSimplePaths(net);
        DijkstraShortestPath<Integer, Link> djkt = new DijkstraShortestPath<>(net);
        //colector que va a almacenar los k caminos mas cortos
        List<GraphPath<Integer, Link>> kspaths = new ArrayList<>();


        //ORDENAMOS LAS DEMANDAS ASCENDENTE - DESCENDENTE - ALEATORIO NO HACER NADA
        List<DemandDistancePair> demandDistances = new ArrayList<>();
        int rejectedDemandsCount = 0;

        List<Demand> demandsToRemove;
        List<Demand> demandsToAdd;

        do {
            demandsToRemove = new ArrayList<>();
            demandsToAdd = new ArrayList<>();
            rejectedDemandsCount = 0;

            for (Demand demand : demands) {
                int source = demand.getSource();
                int destination = demand.getDestination();

                GraphPath<Integer, Link> shortestPath = djkt.getPath(source, destination);
                double distance = shortestPath.getWeight();

                DemandDistancePair demandDistancePair = new DemandDistancePair(demand, distance, "");

                // Calcular la modulación para una demanda con una distancia específica
                ModulationCalculator modulationCalculator = new ModulationCalculator();
                boolean fsCalculated = modulationCalculator.calculateFS(demandDistancePair);

                if (!fsCalculated) {
                    // FS calculation failed, regenerate demand
                    rejectedDemandsCount++;
                    demandsToRemove.add(demand);
                    demandsToAdd.add(Utils.generateSingleDemand(net.vertexSet().size()));
                } else {
                    demandDistances.add(demandDistancePair);
                }
            }

            // Remove and Add rejected demands after the iteration
            demands.removeAll(demandsToRemove);
            demands.addAll(demandsToAdd);

        } while (demandsToRemove.size() > 0 && demands.size() < options.getDemandsQuantity());

        System.out.println("Número de demandas rechazadas: " + rejectedDemandsCount + " Total de demandas: " + demands.size());


        // Ordenar en función del parámetro ascendente, descendente y aleatorio seria como viene
        if (options.getSortingDemands().equalsIgnoreCase("ASC")) {
            Collections.sort(demandDistances); // Orden ascendente (por defecto)
        } else if (options.getSortingDemands().equalsIgnoreCase("DESC")) {
            Collections.sort(demandDistances, Collections.reverseOrder()); // Orden descendente
        }

        //Procesamos las demandas
        List<Response> responses = new ArrayList<>();
        for (DemandDistancePair demand : demandDistances) {

            Response response = new Response();
            response.setBitrate(demand.getDemand().getBitRate());
            response.setModulation(demand.getModulation());
            response.setFs(demand.getDemand().getFs());
            previousFSMax = fsMax;

            System.out.println("-------PROCESANDO NUEVA DEMANDA----------");
            response.setNroDemanda(demandsQ);
            response.setCantRutasActivas(establishedRoutes.size());
            response.setOrigen(demand.getDemand().getSource());
            response.setDestino(demand.getDemand().getDestination());
            System.out.println("Demanda: " + response.getNroDemanda() + ", Origen: " + demand.getDemand().getSource() + ", Destino: " + demand.getDemand().getDestination() + ", Cantidad de rutas en uso: " + establishedRoutes.size());
            demandsQ++;
            kspaths.clear();

            //se ejecuta dijkstra - ksp como sortest-Algorithm
            if (options.getSortestAlg().equals("Dijkstra")) {
                // Retorna el camino más corto de fuente a destino
                GraphPath<Integer, Link> shortestPath = djkt.getPath(demand.getDemand().getSource(), demand.getDemand().getDestination());
                // Agrega el camino a la lista kspaths
                kspaths.add(shortestPath);
            } else {
                // Retorna los 5 caminos más cortos de fuente a destino
                List<GraphPath<Integer, Link>> kShortestPaths = ksp.getPaths(demand.getDemand().getSource(), demand.getDemand().getDestination(), 5);
                for (GraphPath<Integer, Link> path : kShortestPaths) {
                    // Agrega cada camino a la lista kspaths
                    kspaths.add(path);
                }
            }

            //busqueda de caminos disponibles, para establecer los enlaces
            try {
                while (true) {
                    listaBfr = new ArrayList<>();
                    //iteramos los mejores caminos de origen a destino
                    for (GraphPath<Integer, Link> path : kspaths) {
                        if (fsMax < demand.getDemand().getFs()) {
                            fsMax = demand.getDemand().getFs();
                        }

                        //iteramos los nucleos
                        for (int i = 0; i < options.getCores(); i++) {
                            //cumple principios de eon
                            //calcular bfr
                            listaBfr.add(Algorithms.customRsa(net, path, demand.getDemand(), fsMax, i, options.getCapacity()));
                        }
                    }

                    // Filtra los valores nulos de la lista de BFR
                    listaBfr.removeIf(Objects::isNull);

                    if (!listaBfr.isEmpty()) {
                        // Ordena la lista de BFR en orden ascendente
                        listaBfr.sort(Comparator.comparingDouble(BFR::getMsi));

                        // Ordena la lista de BFR en orden ascendente según el valor de MSI y, en caso de empate, el BFR.
                        listaBfr.sort(Comparator.comparing(BFR::getMsi).thenComparing(BFR::getValue));


                        // Obtén el BFR más pequeño (el primero en la lista)
                        BFR mejorBfr = listaBfr.get(0);

                        // Agregar aqui si cumple con el umbral de la diafonia

                        System.out.println("Elegimos el BFR: " + mejorBfr.getValue() + ", y el MSI: " + mejorBfr.getMsi() + " en nucleo: " + mejorBfr.getCore() + ", Distancia: " + mejorBfr.getPath().getWeight());

                        EstablisedRoute establisedRoute = new EstablisedRoute(mejorBfr.getPath().getEdgeList(), mejorBfr.getIndexFs(), demand.getDemand().getFs(), demand.getDemand().getSource(), demand.getDemand().getDestination(), mejorBfr.getCore());

                        establishedRoutes.add((EstablisedRoute) establisedRoute);
                        Utils.assignFs((EstablisedRoute) establisedRoute);
                        response.setCore(((EstablisedRoute) establisedRoute).getCore());
                        response.setFsIndexBegin(((EstablisedRoute) establisedRoute).getFsIndexBegin());
                        response.setFsMax(((EstablisedRoute) establisedRoute).getFsMax());
                        //imprimimos el path de origen a destino
                        //((EstablisedRoute) establisedRoute).printDemandNodes();
                        response.setPath(((EstablisedRoute) establisedRoute).printDemandNodes());
                        System.out.println("Imprimiendo BFR de la Red: " + Algorithms.BFR(net, options.getCapacity()));


                        break;
                    }

                    if (fsMax >= options.getCapacity() && listaBfr.isEmpty()) {
                        response.setBlock(true);
                        System.out.println("Demanda " + demandsQ + " BLOQUEADA ");
                        //response.setSlotBlock(demand.getFs());
                        //blocked = true;
                        demand.getDemand().setBlocked(true);
                        //slotsBlocked += demand.getFs();
                        blocksQ++;
                        fsMax = previousFSMax; // Restablecer FSMAX al valor anterior

                        break;
                    }

                    if (listaBfr.isEmpty())
                        fsMax++;
                }
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }

            responses.add(response);
        }

        Map<String, Boolean> map = new LinkedHashMap<>();
        map.put("end", true);
        System.out.println("Resumen general del simulador");
        System.out.println("Cantidad de demandas: " + demandsQ);
        System.out.println("Cantidad de bloqueos: " + blocksQ);
        System.out.println("FSMAX: " + fsMax);
        //System.out.println("Cantidad de defragmentaciones: " + defragsQ);
        //System.out.println("Cantidad de desfragmentaciones fallidas: " + defragsF);
        System.out.println("Fin Simulación");

        // Llama al método para escribir en el archivo CSV
        writeResponsesToCSV(responses);

        // Al final de tu método simular
        printFSEntryStatus(net, options.getCores(), options.getCapacity());

        // Al final de tu método simular
        countFreeFS(net, options.getCores());


        //System.out.println(obtenerDatosParaNucleo(responses, options.getCapacity()));


        //int maxDistance = findMaxDistance(net);

        //System.out.println("La distancia máxima entre dos nodos es: " + maxDistance);


        return responses;
    }

    private int getCore(int limit, boolean[] tested) {
        Random r = new Random();
        int core = r.nextInt(limit);
        while (tested[core]) {
            core = r.nextInt(limit);
        }
        tested[core] = true;
        return core;
    }

    private Graph createTopology(String fileName, int numberOfCores, double fsWidh, int numberOffs) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Graph<Integer, Link> g = new SimpleWeightedGraph<>(Link.class);
            InputStream is = ResourceReader.getFileFromResourceAsStream(fileName);
            JsonNode object = objectMapper.readTree(is);

            //se agregan los vertices
            for (int i = 0; i < object.get("network").size(); i++) {
                g.addVertex(i);
            }
            int vertex = 0;
            for (JsonNode node : object.get("network")) {
                for (int i = 0; i < node.get("connections").size(); i++) {
                    int connection = node.get("connections").get(i).intValue();
                    int distance = node.get("distance").get(i).intValue();
                    List<Core> cores = new ArrayList<>();

                    for (int j = 0; j < numberOfCores; j++) {
                        Core core = new Core(fsWidh, numberOffs);
                        cores.add(core);
                    }

                    Link link = new Link(distance, cores, vertex, connection);
                    g.addEdge(vertex, connection, link);
                    g.setEdgeWeight(link, distance);
                }
                vertex++;
            }
            return g;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void writeResponsesToCSV(List<Response> responses) {
        String filePath = "src\\main\\resources\\salida\\salida.csv"; // Ruta del archivo CSV en la carpeta resources\salida

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Escribir encabezados
            writer.write("nroDemanda,cantRutasActivas,origen,destino,core,fsIndexBegin,fs,path,bitrate,modulation,block,slotBlock,MSI");
            writer.newLine();

            // Escribir datos de cada respuesta
            for (Response response : responses) {
                writer.write(response.getNroDemanda() + "," + response.getCantRutasActivas() + "," + response.getOrigen() + "," + response.getDestino() + "," + response.getCore() + "," + response.getFsIndexBegin() + "," + response.getFs() + "," + response.getPath() + "," + response.getBitrate() + "," + response.getModulation() + "," + response.isBlock() + "," + response.getSlotBlock() + "," + response.getMSI());
                writer.newLine();
            }

            System.out.println("Los datos se han guardado en el archivo CSV: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printFSStatus(Options options, List<EstablisedRoute> establishedRoutes) {
        for (int coreIndex = 0; coreIndex < options.getCores(); coreIndex++) {
            boolean[] coreSlots = new boolean[options.getCapacity()];
            Arrays.fill(coreSlots, false);

            for (EstablisedRoute route : establishedRoutes) {
                if (route.getCore() == coreIndex) {
                    for (int fsIndex = route.getFsIndexBegin(); fsIndex <= route.getFsIndexEnd(); fsIndex++) {
                        coreSlots[fsIndex] = true;
                    }
                }
            }

            System.out.print("Núcleo " + coreIndex + ": ");
            for (int fsIndex = 0; fsIndex < options.getCapacity(); fsIndex++) {
                System.out.print(coreSlots[fsIndex] ? "█" : "░");
            }
            System.out.println();
        }
    }


    private String obtenerDatosParaNucleo(List<Response> responses, int capacity) {
        StringBuilder datos = new StringBuilder();
        for (Response response : responses) {
            datos.append("- Núcleo ").append(response.getCore()).append("\n");
            datos.append("  - Camino ").append(response.getOrigen()).append(" --> ").append(response.getDestino()).append(":\n");
            // Agregar los detalles de cada enlace excluyendo el atributo "core"
            datos.append("    - Número de Demanda: ").append(response.getNroDemanda()).append("\n");
            // Calcular y agregar la representación de la barra de utilización de FS con capacidad
            String barraFS = generarBarraFS(response.getFsIndexBegin(), response.getFs(), capacity);
            datos.append("    - Barra de utilización de FS: ").append(barraFS).append("\n");
            // Calcular y agregar la representación de puntos de FS
            String puntosFS = generarPuntosFS(response.getFsIndexBegin(), response.getFs());
            datos.append("    - Puntos de FS: ").append(puntosFS).append("\n");
            // Excluir el atributo "core" del resultado
            // Ejemplo:
            // datos.append("    - Bitrate: ").append(response.getBitrate()).append("\n");
        }
        return datos.toString();
    }


    private String generarPuntosFS(int fsIndexBegin, int fs) {
        StringBuilder puntos = new StringBuilder("[");
        for (int i = 0; i < fsIndexBegin; i++) {
            puntos.append(". "); // Punto para FS no ocupados
        }
        for (int i = 0; i < fs; i++) {
            puntos.append("█ "); // Carácter █ para FS ocupados
        }
        puntos.append("]");
        return puntos.toString();
    }

    private String generarBarraFS(int fsIndexBegin, int fs, int capacity) {
        StringBuilder barra = new StringBuilder("[");
        int fsEnd = fsIndexBegin + fs;

        for (int i = 0; i < 10; i++) { // Siempre muestra 10 ranuras
            if (i >= fsIndexBegin && i < fsEnd) {
                if (i - fsIndexBegin < capacity) {
                    barra.append("█ "); // Carácter █ para FS ocupados dentro de la capacidad
                } else {
                    barra.append(". "); // Punto para FS ocupados fuera de la capacidad
                }
            } else {
                barra.append("░ "); // Punto para FS no ocupados
            }
        }

        barra.append("]");
        return barra.toString();
    }

    public void countFreeFS(Graph<Integer, Link> net, int cores) {
        int totalFreeFS = 0;

        for (Link link : net.edgeSet()) {
            for (int core = 0; core < cores; core++) {
                int freeFSOnLink = 0;
                for (FrecuencySlot fs : link.getCores().get(core).getFs()) {
                    if (fs.isFree()) {
                        freeFSOnLink++;
                    }
                }
                totalFreeFS += freeFSOnLink;
                System.out.println("Enlace DE: " + link.getFrom() + ", A: " + link.getTo() + ", Núcleo " + core + ": FS Libres = " + freeFSOnLink);
            }
        }

        System.out.println("Total de FS Libres en la Red: " + totalFreeFS);
    }


    public void printFSEntryStatus(Graph<Integer, Link> net, int cores, int capacity) {
        for (Link link : net.edgeSet()) {
            System.out.println("Enlace DE: " + link.getFrom() + " A: " + link.getTo());
            for (int coreIndex = 0; coreIndex < cores; coreIndex++) {
                FrecuencySlot[] coreSlots = link.getCores().get(coreIndex).getFs().toArray(new FrecuencySlot[0]);
                System.out.print("Núcleo " + coreIndex + ": ");
                for (int fsIndex = 0; fsIndex < capacity; fsIndex++) {
                    System.out.print(coreSlots[fsIndex].isFree() ? "░" : "█");
                }
                System.out.println();
            }
        }
    }



}
