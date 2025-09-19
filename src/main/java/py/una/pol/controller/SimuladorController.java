package py.una.pol.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import py.una.pol.algorithms.Algorithms;
import py.una.pol.algorithms.ShortestPathFinder;
import py.una.pol.domain.*;
import py.una.pol.model.*;
import py.una.pol.repository.SimulationRepository;
import py.una.pol.utils.DemandSorter;
import py.una.pol.utils.DemandsGenerator;
import py.una.pol.utils.ResourceReader;
import py.una.pol.utils.Utils;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.math.BigDecimal.valueOf;
import static java.util.stream.Collectors.toList;
import static py.una.pol.utils.DemandsGenerator.BITRATE_DISTRIBUTIONO;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(value = "SimuladorController", description = "Operaciones relacionadas con la simulación de demandas")
public class SimuladorController {

    @Autowired
    private SimulationRepository simulationRepository;

    private Simulation simulation;

    @PostMapping(path = "/simular")
    @ApiOperation(value = "Simula las demandas con las opciones proporcionadas")
    public Response simular(@ApiParam(value = "Opciones para la simulación de demandas", required = true) @RequestBody Options options) {
        System.out.println("##################################");
        System.out.println("Inicio simulación");
        simulation = buildSimulation(options);

        List<EstablishedRoute> establishedRoutes = new ArrayList<>();
        List<GraphPath<Integer, Link>> kspaths = new ArrayList<>();
        List<Demand> demands;
        int demandsQ = 0, blocksQ = 0, fsMax = 0;

        //se crea la topologia con los parámetros seleccionados
        Graph<Integer, Link> net = createTopology(options.getTopology(), options.getCores(), options.getFsWidth(), options.getCapacity());

        //buscador de caminos mas cortos DIJKSTRA - KSP
        ShortestPathFinder shortestPathFinder = new ShortestPathFinder(net);

        //se generan aleatoriamente las demandas, de acuerdo a la cantidad proporcionadas por parámetro
        demands = DemandsGenerator.generateAndValidateDemands(simulation, options.getDemandsQuantity(), net,
                shortestPathFinder);

        // Ordenar en función del parámetro ascendente, descendente y aleatorio seria como viene
        if (options.getSortingDemands().equalsIgnoreCase("ASC")) {
            DemandSorter.sortByDistanceAscending(demands); // Orden ascendente (por defecto)
        } else if (options.getSortingDemands().equalsIgnoreCase("DESC")) {
            DemandSorter.sortByDistanceDescending(demands); // Orden descendente
        }

        demands.forEach(this::addDemandIntoSimulation);

        for (var demand : demands) {
            demandsQ++;
            kspaths.clear();

            //se ejecuta k1 = Dijkstra, k3 = ksp (3 caminos), k5 = ksp (5 caminos)
            if (options.getShortestAlg().equals("k1")) {
                // Retorna el camino más corto de fuente a destino
                GraphPath<Integer, Link> shortestPath = shortestPathFinder.getShortestPath(demand.getSource(), demand.getDestination());
                // Agrega el camino a la lista kspaths
                kspaths.add(shortestPath);
            } else if (options.getShortestAlg().equals("k3")) {
                // Retorna los 3 caminos más cortos de fuente a destino
                List<GraphPath<Integer, Link>> kShortestPaths = shortestPathFinder.getKShortestPaths(demand.getSource(), demand.getDestination(), 3);
                // Agrega todos los caminos a la lista kspaths
                kspaths.addAll(kShortestPaths);
            } else {
                // Retorna los 5 caminos más cortos de fuente a destino
                List<GraphPath<Integer, Link>> kShortestPaths = shortestPathFinder.getKShortestPaths(demand.getSource(), demand.getDestination(), 5);
                // Agrega todos los caminos a la lista kspaths
                kspaths.addAll(kShortestPaths);
            }

            kspaths.forEach(kspPath -> addKspPathIntoSimulation(kspPath, demand.getId()));

            //busqueda de caminos disponibles, para establecer los enlaces
            EstablishedRoute establishedRoute = Algorithms.findBestRoute(simulation, demand, kspaths, options.getCores(),
                    options.getCapacity(), fsMax, options.getMaxCrosstalk(), options.getCrosstalkPerUnitLenght());

            if (establishedRoute == null) {
                demand.setBlocked(true);
                blocksQ++;
                simulation.getDemand(demand.getId()).setStatus(Simulation.Demand.DemandStatus.BLOCKED);
                break;
            } else {
                establishedRoutes.add(establishedRoute);
                Utils.assignFs(simulation, net, establishedRoute, options.getCrosstalkPerUnitLenght());

                fsMax = Math.max(fsMax, establishedRoute.getFsMax());
                simulation.getDemand(demand.getId()).setStatus(Simulation.Demand.DemandStatus.ACTIVE);
            }
        }

        Map<String, Boolean> map = new LinkedHashMap<>();
        map.put("end", true);
        System.out.println("Resumen general del simulador");
        System.out.println("Cantidad de demandas: " + demandsQ);
        System.out.println("Cantidad de bloqueos: " + blocksQ);
        System.out.println("FSMAX: " + fsMax);
        System.out.println("Fin Simulación");
        System.out.println("##################################");

        Response response = new Response();
        response.setCantDemandas(demandsQ);
        response.setFsMax(fsMax);

        var result = Result.builder()
                .maxFs(fsMax)
                .maxBlocks(blocksQ)
                .build();
        simulation.setResult(result);
        simulation.setEndAt(LocalDateTime.now());
        simulationRepository.save(simulation);

        return response;
    }

    private static Simulation buildSimulation(final Options options) {
        return Simulation.builder()
                .version(Version.ORIGINAL_FIX)
                .parameter(buildParameter(options))
                .configuration(buildConfiguration())
                .demands(new ArrayList<>(options.getDemandsQuantity()))
                .demandsRejected(new ArrayList<>())
                .startAt(LocalDateTime.now())
                .build();
    }

    private static Configuration buildConfiguration() {
        return Configuration.builder()
                .bitRateDistribution(BITRATE_DISTRIBUTIONO)
                .build();
    }

    private static Network.Core buildCore(int numberOfCores, double fsWidth, Integer coreFsCapacity) {
        return Network.Core.builder()
                .fsWith(fsWidth)
                .capacity(numberOfCores)
                .fsList(IntStream.range(0, coreFsCapacity)
                        .mapToObj(i -> Network.FrequencySlot.builder()
                                .index(i)
                                .free(true)
                                .crosstalk(BigDecimal.ZERO)
                                .build()).collect(toList()))
                .build();
    }

    private static Network.Link buildNetworkLink(int vertex, int connection, int distance) {
        return Network.Link.builder()
                .from(vertex)
                .to(connection)
                .distance(distance)
                .cores(new ArrayList<>())
                .build();
    }

    private static Parameter buildParameter(Options options) {
        return Parameter.builder()
                .topology(options.getTopology().split("\\.")[0])
                .fsWidth(Float.valueOf(options.getFsWidth()).doubleValue())
                .coreCapacity(options.getCapacity())
                .linkCapacity(options.getCores())
                .ksp(options.getShortestAlg())
                .demandsLimit(options.getDemandsQuantity())
                .sortingStrategy(options.getSortingDemands())
                .maxCrosstalkDb(options.getMaxCrosstalkDb())
                .maxCrosstalk(options.getMaxCrosstalk().round(MathContext.DECIMAL128))
                .unitCrosstalk(valueOf(options.getCrosstalkPerUnitLenght()))
                .build();
    }

    public static void writeResponsesToCSV(Options options, int fsMax) {
        String filePath = "src/main/resources/salida/salida.csv"; // Ruta del archivo CSV en la carpeta resources/salida
        //String filePath = "src\\main\\resources\\salida\\salida.csv"; //para windows


        // Verifica si la ruta es correcta y crea el archivo si no existe
        File file = new File(filePath);
        try {
            if (file.getParentFile() != null) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs(); // Crea los directorios necesarios
                }
            }
            if (file.createNewFile()) {
                System.out.println("El archivo ha sido creado: " + file.getAbsolutePath());
            } else {
                System.out.println("El archivo ya existe: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al crear el archivo: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Escribir en el archivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            // Escribir encabezados si el archivo está vacío
            if (file.length() == 0) {
                writer.write("Topologia,KSP(caminos),Ordenamiento,fsMax");
                writer.newLine();
            }

            // Escribir datos de cada respuesta
            String line = options.getTopology() + "," + options.getShortestAlg() + "," + options.getSortingDemands() + "," + fsMax;
            writer.write(line);
            writer.newLine();

            System.out.println("Los datos se han guardado en el archivo CSV: " + filePath);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addDemandIntoSimulation(Demand rawDemand) {
        simulation.addDemand(Simulation.Demand.builder()
                .index(rawDemand.getId())
                .source(rawDemand.getSource())
                .target(rawDemand.getDestination())
                .bitRate(rawDemand.getBitRate())
                .distance(rawDemand.getDistance())
                .kspPaths(new ArrayList<>())
                .status(Simulation.Demand.DemandStatus.PENDING)
                .build());
    }

    private void addKspPathIntoSimulation(GraphPath<Integer, Link> kspPath, int demandId) {
        simulation.getDemand(demandId)
                .getKspPaths().add(KspPath.builder()
                        .distance(Double.valueOf(kspPath.getWeight()).intValue())
                        .nodes(kspPath.getVertexList())
                        .cores(new ArrayList<>(simulation.getParameter().getLinkCapacity()))
                        .status(KspPath.KspPathStatus.PENDING)
                        .build());
    }

    private Graph createTopology(String fileName, int numberOfCores, double fsWidh, int numberOffs) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Graph<Integer, Link> g = new SimpleWeightedGraph<>(Link.class);
            InputStream is = ResourceReader.getFileFromResourceAsStream(fileName);
            JsonNode object = objectMapper.readTree(is);

            var network = Network.builder()
                    .nodes(new ArrayList<>())
                    .links(new ArrayList<>())
                    .build();

            //se agregan los vertices
            for (int i = 0; i < object.get("network").size(); i++) {
                g.addVertex(i);
                network.getNodes().add(i);
            }
            int vertex = 0;
            for (JsonNode node : object.get("network")) {
                for (int i = 0; i < node.get("connections").size(); i++) {
                    int connection = node.get("connections").get(i).intValue();
                    int distance = node.get("distance").get(i).intValue();
                    List<Core> cores = new ArrayList<>();
                    var networkLink = buildNetworkLink(vertex, connection, distance);

                    for (int j = 0; j < numberOfCores; j++) {
                        Core core = new Core(fsWidh, numberOffs);
                        cores.add(core);
                        networkLink.getCores().add(buildCore(numberOfCores, fsWidh,
                                simulation.getParameter().getCoreCapacity()));
                    }

                    Link link = new Link(distance, cores, vertex, connection);
                    g.addEdge(vertex, connection, link);
                    g.setEdgeWeight(link, distance);

                    network.getLinks().add(networkLink);
                }
                vertex++;
            }
            simulation.setNetwork(network);
            return g;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
