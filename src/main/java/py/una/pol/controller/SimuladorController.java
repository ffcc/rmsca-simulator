package py.una.pol.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.web.bind.annotation.*;
import py.una.pol.algorithms.Algorithms;
import py.una.pol.algorithms.ShortestPathFinder;
import py.una.pol.model.*;
import py.una.pol.utils.DemandSorter;
import py.una.pol.utils.DemandsGenerator;
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
@Api(value = "SimuladorController", description = "Operaciones relacionadas con la simulación de demandas")
public class SimuladorController {

    @PostMapping(path = "/simular")
    @ApiOperation(value = "Simula las demandas con las opciones proporcionadas")
    public List<Response> simular(@ApiParam(value = "Opciones para la simulación de demandas", required = true) @RequestBody Options options) {
        List<EstablishedRoute> establishedRoutes = new ArrayList<>();
        List<GraphPath<Integer, Link>> kspaths = new ArrayList<>();
        List<Demand> demands;
        int fsMax = 0;
        int previousFSMax = 0;
        int demandsQ = 0, blocksQ = 0;

        //se crea la topologia con los parámetros seleccionados
        Graph<Integer, Link> net = createTopology(options.getTopology(), options.getCores(), options.getFsWidth(), options.getCapacity());

        //buscador de caminos mas cortos DIJKSTRA - KSP
        ShortestPathFinder shortestPathFinder = new ShortestPathFinder(net);

        //se generan aleatoriamente las demandas, de acuerdo a la cantidad proporcionadas por parámetro
        demands = DemandsGenerator.generateAndValidateDemands(options.getDemandsQuantity(), net, shortestPathFinder);

        // Ordenar en función del parámetro ascendente, descendente y aleatorio seria como viene
        if (options.getSortingDemands().equalsIgnoreCase("ASC")) {
            DemandSorter.sortByDistanceAscending(demands); // Orden ascendente (por defecto)
        } else if (options.getSortingDemands().equalsIgnoreCase("DESC")) {
            DemandSorter.sortByDistanceDescending(demands); // Orden descendente
        }

        //Procesamos las demandas
        List<Response> responses = new ArrayList<>();
        for (Demand demand : demands) {

            Response response = new Response();
            response.setBitrate(demand.getBitRate());
            response.setModulation(demand.getModulation());
            response.setFs(demand.getFs());
            previousFSMax = fsMax;

            System.out.println("-------PROCESANDO NUEVA DEMANDA----------");
            response.setNroDemanda(demandsQ);
            response.setCantRutasActivas(establishedRoutes.size());
            response.setOrigen(demand.getSource());
            response.setDestino(demand.getDestination());
            System.out.println("Demanda: " + response.getNroDemanda() + ", Origen: " + demand.getSource() + ", Destino: " + demand.getDestination() + ", Cantidad de rutas en uso: " + establishedRoutes.size());
            demandsQ++;
            kspaths.clear();

            //se ejecuta dijkstra - ksp como sortest-Algorithm
            if (options.getSortestAlg().equals("Dijkstra")) {
                // Retorna el camino más corto de fuente a destino
                GraphPath<Integer, Link> shortestPath = shortestPathFinder.getShortestPath(demand.getSource(), demand.getDestination());
                // Agrega el camino a la lista kspaths
                kspaths.add(shortestPath);
            } else {
                // Retorna los 5 caminos más cortos de fuente a destino
                List<GraphPath<Integer, Link>> kShortestPaths = shortestPathFinder.getKShortestPaths(demand.getSource(), demand.getDestination(), 5);
                for (GraphPath<Integer, Link> path : kShortestPaths) {
                    // Agrega cada camino a la lista kspaths
                    kspaths.add(path);
                }
            }

            //busqueda de caminos disponibles, para establecer los enlaces
            EstablishedRoute establishedRoute = Algorithms.findBestRoute(demand, net, kspaths, options.getCores(), options.getCapacity(), fsMax, options.getMaxCrosstalk(), options.getCrosstalkPerUnitLenght());

            if (establishedRoute == null) {
                response.setBlock(true);
                System.out.println("Demanda " + demandsQ + " BLOQUEADA ");
                //response.setSlotBlock(demand.getFs());
                //blocked = true;
                demand.setBlocked(true);
                //slotsBlocked += demand.getFs();
                blocksQ++;

                break;

            } else {
                establishedRoutes.add(establishedRoute);
                //Utils.assignFs(establishedRoute);
                //response.setCore((establishedRoute).getCore());
                response.setFsIndexBegin((establishedRoute).getFsIndexBegin());
                response.setFsMax((establishedRoute).getFsMax());
                //imprimimos el path de origen a destino
                //((EstablisedRoute) establisedRoute).printDemandNodes();
                response.setPath((establishedRoute).printDemandNodes());
            }

            responses.add(response);
        }

        Map<String, Boolean> map = new LinkedHashMap<>();
        map.put("end", true);
        System.out.println("Resumen general del simulador");
        System.out.println("Cantidad de demandas: " + demandsQ);
        System.out.println("Cantidad de bloqueos: " + blocksQ);
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
                for (FrequencySlot fs : link.getCores().get(core).getFs()) {
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
                FrequencySlot[] coreSlots = link.getCores().get(coreIndex).getFs().toArray(new FrequencySlot[0]);
                System.out.print("Núcleo " + coreIndex + ": ");
                for (int fsIndex = 0; fsIndex < capacity; fsIndex++) {
                    System.out.print(coreSlots[fsIndex].isFree() ? "░" : "█");
                }
                System.out.println();
            }
        }
    }


}
