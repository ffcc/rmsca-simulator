package py.una.pol.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.web.bind.annotation.*;
import py.una.pol.algorithms.Algorithms;
import py.una.pol.rest.model.*;
import py.una.pol.utils.ResourceReader;
import py.una.pol.utils.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class SimuladorController {

    @PostMapping(path= "/simular")
    public void simular(@RequestBody Options options) throws Exception {

        //socketClient.startConnection("127.0.0.1",9999);
        List<Demand> demands;
        List<EstablisedRoute> establishedRoutes = new ArrayList<>();
        Graph net = createTopology(options.getTopology(), options.getCores(), options.getFsWidth(), options.getCapacity());
        List<List<GraphPath>> kspList = new ArrayList<>();
        FileWriter file = new FileWriter("bloqueos.csv");
        BufferedWriter writer = new BufferedWriter(file);
        int slotsBlocked;
        int demandsQ = 0;
        int defragsQ = 0, blocksQ = 0, defragsF = 0;
        writer.write("Entropy, Pc, Msi, Bfr, Shf, % Uso, Slots Bloqueados, Prediccion");
        writer.newLine();

        for (int i = 0; i < options.getTime(); i++) {
            boolean blocked = false;
            System.out.println("Tiempo: " + (i+1) + ", Cantidad de rutas activas: " + establishedRoutes.size());
            demands = Utils.generateDemands(
                    options.getDemandsQuantity(), options.getTime(),
                    options.getFsRangeMin(), options.getFsRangeMax(),
                    net.vertexSet().size());

            System.out.println("Cantidad de demandas: " + demands.size());
            writer.write("cantidad de demandas: " + demands.size());
            writer.newLine();

            //se carga la red en ksp, con todos los nodos, pares de nodos
            KShortestSimplePaths ksp = new KShortestSimplePaths(net);
            slotsBlocked = 0;
            demandsQ += demands.size();

            for(Demand demand : demands){
                //k caminos más cortos entre source y destination de la demanda actual
                List<GraphPath> kspaths = ksp.getPaths(demand.getSource(), demand.getDestination(), 5);
                try {
                    boolean [] tested = new boolean[options.getCores()];
                    Arrays.fill(tested, false);
                    int core;
                    while (true){
                        core = getCore(options.getCores(), tested);
                        Class<?>[] paramTypes = {Graph.class, List.class, Demand.class, int.class, int.class};
                        Method method = Algorithms.class.getMethod(options.getRoutingAlg(), paramTypes);
                        Object establisedRoute = method.invoke(this, net, kspaths, demand, options.getCapacity(), core);
                        if(establisedRoute == null){
                            tested[core] = true;//Se marca el core probado
                            if(!Arrays.asList(tested).contains(false)){//Se ve si ya se probaron todos los cores
                                //Bloqueo
                                //System.out.println("BLOQUEO");
                                blocked = true;
                                //System.out.println("Va a desfragmentar con :" + establishedRoutes.size() + " rutas");
                                ///if((defragS || (i - last_defrag_time >= tmin))){
                                //defragS = Algorithms.aco_def(net,establishedRoutes,antsq,aco_def_metric,FSMinPC,aco_improv,options.getRoutingAlg(),ksp,options.getCapacity(), kspList);
                                //defragsQ++;
                                //if(!defragS){
                                //    defragsF++;
                                //    last_defrag_time = i;
                                //}
                                //}
                                demand.setBlocked(true);
                                //this.template.convertAndSend("/message",  demand);
                                //break;
                                slotsBlocked += demand.getFs();
                                blocksQ++;
                            }
                        }else{
                            //Ruta establecida
                            establishedRoutes.add((EstablisedRoute) establisedRoute);
                            kspList.add(kspaths);
                            Utils.assignFs((EstablisedRoute)establisedRoute, core);
                            //this.template.convertAndSend("/message",  establisedRoute);
                            //break;
                        }
                        if(establisedRoute != null || demand.getBlocked())
                            break;
                    }
                }catch (java.lang.Exception e) {
                    e.printStackTrace();
                }
            }

            /*
            for (int ri = 0; ri < establishedRoutes.size(); ri++){
                EstablisedRoute route = establishedRoutes.get(ri);
                if(route.getTimeLife() == 0){
                    establishedRoutes.remove(ri);
                    kspList.remove(ri);
                    ri--;
                }
            }*/


            ReleasedSlots rSlots = new ReleasedSlots();
            rSlots.setTime(i + 2);
            rSlots.setReleased(true);
            //rSlots.setReleasedSlots(this.setTimeLife(net));
            //this.template.convertAndSend("/message", rSlots);

        }
        Map<String, Boolean> map = new LinkedHashMap<>();
        map.put("end", true);
        System.out.println("Resumen general del simulador");
        System.out.println("Cantidad de demandas: " + demandsQ);
        System.out.println("Cantidad de bloqueos: " + blocksQ);
        System.out.println("Cantidad de defragmentaciones: " + defragsQ);
        System.out.println("Cantidad de desfragmentaciones fallidas: " + defragsF);
        System.out.println("Fin Simulación");
        writer.close();
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
}
