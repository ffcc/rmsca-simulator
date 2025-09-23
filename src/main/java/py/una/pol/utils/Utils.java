package py.una.pol.utils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

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
        for (int j = 0; j < establishedRoute.getPath().size(); j++) {
            var path = establishedRoute.getPath().get(j);
            var edge = graph.getEdge(path.getTo(), path.getFrom());
            var link = links.stream()
                    .filter(l -> l.getFrom() == path.getFrom() && l.getTo() == path.getTo())
                    .findFirst().orElseThrow();

            for (int i = establishedRoute.getFsIndexBegin(); i < establishedRoute.getFsIndexBegin() + establishedRoute.getFs(); i++) {
                Integer core = establishedRoute.getPathCores().get(j);
                path.getCores().get(core).getFs().get(i).setFree(false); //marca como ocupados los FS del path
                link.getCores().get(core).getFsList().get(i).setFree(false);
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
                path.getCores().get(core).getFs().get(i).setCrosstalk(
                        toDB(XT(neighborCoresActives, crosstalkPerUnitLength, path.getDistance())));
                link.getCores().get(core).getFsList().get(i).setCrosstalk(
                        path.getCores().get(core).getFs().get(i).getCrosstalk().round(MathContext.DECIMAL128)
                );
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


}
