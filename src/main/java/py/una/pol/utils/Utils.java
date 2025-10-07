package py.una.pol.utils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jgrapht.Graph;
import py.una.pol.domain.Simulation;
import py.una.pol.model.*;

public class Utils {


    /**
     * Calcula el valor de Crosstalk en un núcleo
     *
     * @param n Número de cores vecinos
     * @param h Crosstalk por Unidad de Longitud
     * @param L Longitud del enlace
     * @return Crosstalk
     */
    public static double XT(int n, double h, int L) {
        double XT = 0;
        for (int i = 0; i < n; i++) {
            XT = XT + (h * (L * 1000));
        }
        return XT;
    }

    /**
     * Calcula la cantidad de nucleos adyacentes para un núcleo en una red de 7
     * núcleos
     *
     * @param core Núcleo a utilizar para encontrar la cantidad de vecinos
     * @return Cantidad de vecinos del núcleo
     */
    public static int getCantidadVecinos(int core) {
        if (core == 6) {
            return 6;
        }
        return 3;
    }

    /**
     * Conversión a decibelios
     *
     * @param value Valor de crosstalk
     * @return Valor de crosstalk en decibelios
     */
    public static BigDecimal toDB(double value) {
        try {
            //return new BigDecimal(10D*Math.log10(value));
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    /**
     * Función de asignación de conexiones a la red
     *
     * @param graph                  Red
     * @param establishedRoute       Ruta a establecer
     * @param crosstalkPerUnitLength Crosstalk por unidad de distancia de la
     *                               fibra
     */
    public static void assignFs(Simulation simulation, Graph<Integer, Link> graph,
                                EstablishedRoute establishedRoute, Double crosstalkPerUnitLength) {
        var links = simulation.getNetwork().getLinks();
        List<BigDecimal> accumulatedCrosstalk = new ArrayList<>(IntStream
                .range(0, establishedRoute.getFs())
                .mapToObj(i -> BigDecimal.ZERO)
                .toList());
        var fsIndexBegin = establishedRoute.getFsIndexBegin();
        /* Actualización del crosstalk almacenado en los core vecinos y acumulación del crosstalk */
        for (int j = 0; j < establishedRoute.getPath().size(); j++) {
            var path = establishedRoute.getPath().get(j);
            var edge = graph.getEdge(path.getTo(), path.getFrom());
            var link = links.stream()
                    .filter(l -> l.getFrom() == path.getFrom() && l.getTo() == path.getTo())
                    .findFirst().orElseThrow();

            for (int i = fsIndexBegin, xtIndex = 0; i < fsIndexBegin + establishedRoute.getFs(); i++, xtIndex++) {
                Integer core = establishedRoute.getPathCores().get(j);
                /* Marca como ocupado el FS del core escogido */
                path.getCores().get(core).getFs().get(i).setFree(false); //marca como ocupados los FS del path
                link.getCores().get(core).getFsList().get(i).setFree(false);
                /* Actualiza el crosstalk acumulado en los FS de los core vecinos ocupados */
                var neighborCoresActives = 0;
                List<Integer> coreVecinos = getCoreVecinos(core);
                for (int neighborCore : coreVecinos) {
                    var fs = path.getCores().get(neighborCore).getFs().get(i);
                    if (!fs.isFree()) {
                        neighborCoresActives++;
                        BigDecimal crosstalkDB = toDB(XT(1, crosstalkPerUnitLength, path.getDistance()));
                        fs.setCrosstalk(path.getCores().get(neighborCore).getFs().get(i).getCrosstalk().add(crosstalkDB));

                        var edgeFs = edge.getCores().get(neighborCore).getFs().get(i);
                        edgeFs.setCrosstalk(edgeFs.getCrosstalk().add(crosstalkDB));
                        link.getCores().get(neighborCore).getFsList().get(i).setCrosstalk(
                                edgeFs.getCrosstalk().round(MathContext.DECIMAL128));
                    }
                }
                /* Actualiza el crosstalk acumulado */
                var fsCrosstalk = toDB(XT(neighborCoresActives, crosstalkPerUnitLength, path.getDistance()));
                accumulatedCrosstalk.set(xtIndex, accumulatedCrosstalk.get(xtIndex).add(fsCrosstalk));
            }
        }
        /* Almacena el crosstalk acumulado en los FS */
        for (int j = 0; j < establishedRoute.getPath().size(); j++) {
            var path = establishedRoute.getPath().get(j);
            var edge = graph.getEdge(path.getTo(), path.getFrom());
            var link = links.stream()
                    .filter(l -> l.getFrom() == path.getFrom() && l.getTo() == path.getTo())
                    .findFirst().orElseThrow();
            var core = establishedRoute.getPathCores().get(j);
            for (int i = fsIndexBegin, xtIndex = 0; i < fsIndexBegin + establishedRoute.getFs(); i++, xtIndex++) {
                var xtAcumulado = accumulatedCrosstalk.get(xtIndex);
                path.getCores().get(core).getFs().get(i).setCrosstalk(xtAcumulado);
                edge.getCores().get(core).getFs().get(i).setCrosstalk(xtAcumulado);
                link.getCores().get(core).getFsList().get(i).setCrosstalk(xtAcumulado.round(MathContext.DECIMAL128));
            }
        }
    }

    /**
     * Obtiene los índices de los núcleos vecinos para un núcleo de la fibra
     *
     * @param coreActual Núcleo de la fibra
     * @return Núcleos adyacentes al núcleo actual
     */
    public static List<Integer> getCoreVecinos(Integer coreActual) {
        List<Integer> vecinos = new ArrayList<>();
        switch (coreActual) {
            case 0 -> {
                vecinos.add(1);
                vecinos.add(5);
                vecinos.add(6);
            }
            case 1 -> {
                vecinos.add(0);
                vecinos.add(2);
                vecinos.add(6);
            }
            case 2 -> {
                vecinos.add(1);
                vecinos.add(3);
                vecinos.add(6);
            }
            case 3 -> {
                vecinos.add(2);
                vecinos.add(4);
                vecinos.add(6);
            }
            case 4 -> {
                vecinos.add(3);
                vecinos.add(5);
                vecinos.add(6);
            }
            case 5 -> {
                vecinos.add(0);
                vecinos.add(4);
                vecinos.add(6);
            }
            case 6 -> {
                vecinos.add(0);
                vecinos.add(1);
                vecinos.add(2);
                vecinos.add(3);
                vecinos.add(4);
                vecinos.add(5);
            }
        }
        return vecinos;
    }

    public static AssignFsResponse assignFsv1(Simulation simulation, Graph<Integer, Link> graph,
                                            EstablishedRoute establishedRoute, Double crosstalkPerUnitLength) {
        /* Se recorre cada enlace del camino escogido */
        for (int j = 0; j < establishedRoute.getPath().size(); j++) {

            /* Se recorrer cada FS del bloque escogido */
            for (int i = establishedRoute.getFsIndexBegin(); i < establishedRoute.getFsIndexBegin() + establishedRoute.getFs(); i++) {
                /* Se indica como activo el FS en el core escogido */
                establishedRoute.getPath().get(j).getCores().get(establishedRoute.getPathCores().get(j)).getFs().get(i).setFree(false); //marca como ocupados los FS del path
                Integer core = establishedRoute.getPathCores().get(j);
                /* TODO actualizar el crosstalk acumulado en el propio FS del core escogido */

                /* Se recorre todos los cores del enlace escogido */
                List<Integer> coreVecinos = getCoreVecinos(core);
                // TODO: Asignar crosstalk
                for (Integer coreIndex = 0; coreIndex < establishedRoute.getPath().get(j).getCores().size(); coreIndex++) {
                    /* Se procesa el core solo si el mismo es un vecino del core escogido */
                    if (!core.equals(coreIndex) && coreVecinos.contains(coreIndex)) {
                        /* Se calcula el crosstalk basado en la cantidad de vecinos del core a procesar */
                        /* TODO considerar sumar sólo la interferencia actual del core escogido (VecinoActivo=1 en la formula) */
                        double crosstalk = XT(getCantidadVecinos(coreIndex), crosstalkPerUnitLength, establishedRoute.getPath().get(j).getDistance());
                        BigDecimal crosstalkDB = toDB(crosstalk);
                        /* Actualizo el crosstalk acumulado en el FS del core vecino */
                        establishedRoute
                                .getPath().get(j)
                                .getCores().get(coreIndex)
                                .getFs().get(i)
                                .setCrosstalk(establishedRoute
                                        .getPath().get(j)
                                        .getCores().get(coreIndex)
                                        .getFs().get(i)
                                        .getCrosstalk()
                                        .add(crosstalkDB));

                        BigDecimal existingCrosstalk = graph
                                .getEdge(establishedRoute.getPath().get(j).getTo(), establishedRoute.getPath().get(j).getFrom())/* Enlace actual en el grafo */
                                .getCores().get(coreIndex) /* Core vecino en el enlace del grafo */
                                .getFs().get(i) /* Fs en el core del grafo */
                                .getCrosstalk();
                        graph
                                .getEdge(establishedRoute.getPath().get(j).getTo(), establishedRoute.getPath().get(j).getFrom())
                                .getCores().get(coreIndex)
                                .getFs().get(i)
                                .setCrosstalk(existingCrosstalk.add(crosstalkDB));
                        //System.out.println("CT despues de suma" + graph.getEdge(establishedRoute.getPath().get(j).getTo(), establishedRoute.getPath().get(j).getFrom()).getCores().get(coreIndex).getFrequencySlots().get(i).getCrosstalk());
                    }
                }
            }
        }
        AssignFsResponse response = new AssignFsResponse(graph, establishedRoute);
        return response;
    }
}
