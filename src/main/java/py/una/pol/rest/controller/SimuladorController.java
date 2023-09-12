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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class SimuladorController {

    @PostMapping(path= "/simular")
    public List<Response> simular(@RequestBody Options options) throws Exception {
        List<Demand> demands;
        List<EstablisedRoute> establishedRoutes = new ArrayList<>();
        //se crea la topoligia con los parámetros seleccionados
        Graph<Integer, Link> net = createTopology(options.getTopology(), options.getCores(), options.getFsWidth(), options.getCapacity());
        List<List<GraphPath>> kspList = new ArrayList<>();
        List<BFR> listaBfr;

        int demandsQ = 0, blocksQ = 0;

        //se generan aleatoriamente las demandas, de acuerdo a la cantidad proporcionadas por parámetro
        demands = Utils.generateDemands(options.getDemandsQuantity(), options.getFsRangeMin(),
                options.getFsRangeMax(), net.vertexSet().size());

        //se carga la red en ksp - dijkstra, con todos los nodos, pares de nodos
        KShortestSimplePaths ksp = new KShortestSimplePaths(net);
        DijkstraShortestPath<Integer, Link> djkt = new DijkstraShortestPath<>(net);
        //colector que va a almacenar los k caminos mas cortos
        List<GraphPath> kspaths = new ArrayList<>();
        int core = 0;


        //ORDENAMOS LAS DEMANDAS ASCENDENTE - DESCENDENTE - ALEATORIO NO HACER NADA
        List<DemandDistancePair> demandDistances = new ArrayList<>();

        for (Demand demand : demands) {
            int source = demand.getSource();
            int destination = demand.getDestination();

            GraphPath<Integer, Link> shortestPath = djkt.getPath(source, destination);
            double distance = shortestPath.getWeight();

            demandDistances.add(new DemandDistancePair(demand, distance, ""));
        }

        // Ordenar en función del parámetro ascendente, descendente y aleatorio seria como viene
        if (options.getSortingDemands().equalsIgnoreCase("ASC")) {
            Collections.sort(demandDistances); // Orden ascendente (por defecto)
        } else if (options.getSortingDemands().equalsIgnoreCase("DESC")) {
            Collections.sort(demandDistances, Collections.reverseOrder()); // Orden descendente
        }

        //Procesamos las demandas
        List<Response> responses = new ArrayList<>();
        for(DemandDistancePair demand : demandDistances) {
            Response response = new Response();
            //boolean blocked = false;
            System.out.println("-------PROCESANDO NUEVA DEMANDA----------");
            response.setNroDemanda(demandsQ);
            response.setCantRutasActivas(establishedRoutes.size());
            System.out.println("Demanda: " + response.getNroDemanda() + ", Cantidad de rutas en uso: " + establishedRoutes.size());
            demandsQ++;
            kspaths.clear();

            //se ejecuta dijkstra - ksp como sortest-Algorithm
            if(options.getSortestAlg().equals("Dijkstra")) {
                //retorna el camino mas corto de fuente a destino
                kspaths.add(djkt.getPath(demand.getDemand().getSource(), demand.getDemand().getDestination()));
            } else {
                //retorna los 5 caminos mas cortos de fuente a destino
                kspaths = ksp.getPaths(demand.getDemand().getSource(), demand.getDemand().getDestination(), 5);
            }

            //Calcular la modulación para una demanda con una distancia específica
            ModulationCalculator modulationCalculator = new ModulationCalculator();
            modulationCalculator.calculateFS(demand);

            //busqueda de caminos disponibles, para establecer los enlaces
            try {
                //bandera para ir pintando los nucleos visitados
                boolean[] tested = new boolean[options.getCores()];
                Arrays.fill(tested, false);
                while (true) {
                    core = getCore(options.getCores(), tested);
                    //response.setCore(core);
                    Class<?>[] paramTypes = {Graph.class, List.class, Demand.class, int.class, int.class};
                    Method method = Algorithms.class.getMethod(options.getRoutingAlg(), paramTypes);
                    Object establisedRoute = method.invoke(this, net, kspaths, demand.getDemand(), options.getCapacity(), core);

                    if (establisedRoute == null) {
                        tested[core] = true;//Se marca el core probado
                        if (!Arrays.asList(tested).contains(false)) {//Se ve si ya se probaron todos los cores
                            response.setBlock(true);
                            System.out.println("Demanda " + demandsQ + " BLOQUEADA ");
                            //response.setSlotBlock(demand.getFs());
                            //blocked = true;
                            demand.getDemand().setBlocked(true);
                            //slotsBlocked += demand.getFs();
                            blocksQ++;
                        }
                    } else {
                        //Ruta establecida
                        establishedRoutes.add((EstablisedRoute) establisedRoute);
                        kspList.add(kspaths);
                        Utils.assignFs((EstablisedRoute) establisedRoute, core);
                        //response.setOrigen(demand.getSource());
                        //response.setDestino(demand.getDestination());
                        //response.setFs(demand.getFs());
                        response.setFsIndexBegin(((EstablisedRoute) establisedRoute).getFsIndexBegin());
                        response.setPath(obtenerCaminos(kspaths, core));
                        System.out.println("PATH: " + imprimirCaminos(kspaths, core));
                        //System.out.println("Ruta establecida: { origen: " + demand.getSource() + " destino: " + demand.getDestination() + " en el Core: " + core + " utilizando " + demand.getFs() + " FS [ " + ((EstablisedRoute) establisedRoute).getFsIndexBegin() + " - "+ ((EstablisedRoute) establisedRoute).getFsIndexEnd() + "] } ");
                        System.out.println("Imprimiendo BFR: " + Algorithms.BFR(net, options.getCapacity()));
                    }
                    if (establisedRoute != null || demand.getDemand().getBlocked())
                        break;
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
        //System.out.println("Cantidad de defragmentaciones: " + defragsQ);
        //System.out.println("Cantidad de desfragmentaciones fallidas: " + defragsF);
        System.out.println("Fin Simulación");

        //int maxDistance = findMaxDistance(net);

        //System.out.println("La distancia máxima entre dos nodos es: " + maxDistance);


        return responses;
    }

    //para imprimir las rutas que se eligio de origen a destino
    public List<Integer> obtenerCaminos(List<GraphPath> kspaths, int core){
        List<Integer> listaCaminos = new ArrayList<>();
        for (GraphPath caminos : kspaths) {
            for (Object camino: caminos.getEdgeList()) {
                Link link = (Link) camino;
                //System.out.println("Caminos: De " + link.getFrom() + " a " + link.getTo());
                listaCaminos.add(link.getTo());
                listaCaminos.add(link.getFrom());
            }
        }
        return listaCaminos;
    }

    public String imprimirCaminos(List<GraphPath> kspaths, int core) {
        List<Integer> listaCaminos = obtenerCaminos(kspaths, core);
        StringBuilder path = new StringBuilder();



        path.append("PATH:");
        for (int i = 0; i < listaCaminos.size(); i++) {
            path.append(" ").append(listaCaminos.get(i));
            if (i < listaCaminos.size() - 1) {
                path.append(" -->");
            }
        }

        return path.toString();
    }




    @GetMapping(path= "/getTopology")
    public String getTopologia() {
        /* Graph g = createTopology2("nsfnet.json",4,12.5,350);
        KShortestSimplePaths ksp = new KShortestSimplePaths(g);

            //k caminos más cortos entre source y destination de la demanda actual
            List<GraphPath> kspaths = ksp.getPaths(2, 6, 5);
            comprobarKspVocConfia(kspaths);
        DOTExporter<Integer, Link> exporter =
                new DOTExporter<>(v -> v.toString().replace('.', '_'));
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.toString()));
            return map;
        });
        Writer writer = new StringWriter();
        exporter.exportGraph(g, writer);
        return writer.toString();*/
        return "x";
    }

    private int getCore(int limit, boolean [] tested){
        Random r = new Random();
        int core = r.nextInt(limit);
        while(tested[core]){
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
            for (JsonNode node: object.get("network")) {
                for (int i = 0; i < node.get("connections").size(); i++) {
                    int connection = node.get("connections").get(i).intValue();
                    int distance = node.get("distance").get(i).intValue();
                    List<Core> cores = new ArrayList<>();

                    for (int j = 0; j < numberOfCores; j++){
                        Core core = new Core(fsWidh,numberOffs);
                        cores.add(core);
                    }

                    Link link = new Link(distance,cores, vertex, connection);
                    g.addEdge(vertex,connection,link);
                    g.setEdgeWeight(link,distance);
                }
                vertex++;
            }
            return g;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static int findMaxDistance(Graph<Integer, Link> network) {
        int maxDistance = Integer.MIN_VALUE;

        for (Integer sourceNode : network.vertexSet()) {
            for (Integer targetNode : network.vertexSet()) {
                if (!sourceNode.equals(targetNode)) {
                    DijkstraShortestPath<Integer, Link> shortestPath = new DijkstraShortestPath<>(network);
                    double distance = shortestPath.getPathWeight(sourceNode, targetNode);

                    if (distance > maxDistance) {
                        maxDistance = (int) distance;
                    }
                }
            }
        }

        return maxDistance;
    }
}
