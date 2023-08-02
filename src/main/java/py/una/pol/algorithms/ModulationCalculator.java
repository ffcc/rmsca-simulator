package py.una.pol.algorithms;

import org.json.JSONArray;
import org.json.JSONObject;
import py.una.pol.rest.model.DemandDistancePair;
import py.una.pol.rest.model.ModulationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModulationCalculator {

    private List<ModulationInfo> modulationInfoList;

    public String calculateModulation(DemandDistancePair demand) throws IOException {

        String surveyFileName = "modulation/survey.json";
        String jsonStr = new String(ModulationCalculator.class.getClassLoader().getResourceAsStream(surveyFileName).readAllBytes());

        modulationInfoList = parseJsonToModulationInfoList(jsonStr);

        // Ordenar la lista por distancia para realizar búsqueda binaria
        Collections.sort(modulationInfoList, Comparator.comparingDouble(info -> info.distance));

        // Encontrar la modulación adecuada utilizando búsqueda binaria
        int left = 0;
        int right = modulationInfoList.size() - 1;
        String selectedModulation = null;

        // Si la distancia es menor que la distancia más pequeña, seleccionar la modulación más baja
        if (demand.getDistance() < modulationInfoList.get(0).distance) {
            selectedModulation = modulationInfoList.get(0).modulation;
        } else {
            while (left <= right) {
                int mid = left + (right - left) / 2;
                ModulationInfo info = modulationInfoList.get(mid);
                if (info.distance <= demand.getDistance()) {
                    selectedModulation = info.modulation;
                    left = mid + 1; // Buscar en la mitad derecha
                } else {
                    right = mid - 1; // Buscar en la mitad izquierda
                }
            }
        }

        // Si no se encuentra ninguna modulación, elegir la modulación con la menor tasa de bits
        if (selectedModulation == null && !modulationInfoList.isEmpty()) {
            selectedModulation = modulationInfoList.get(modulationInfoList.size() - 1).modulation;
        }

        return selectedModulation;
    }

    public void calculateFS(DemandDistancePair demand) throws IOException {
        // Obtener la modulación adecuada para la demanda
        String modulation = calculateModulation(demand);

        String bitrateFileName = "modulation/bitrate.json";
        String bitrateJsonStr = new String(ModulationCalculator.class.getClassLoader().getResourceAsStream(bitrateFileName).readAllBytes());
        JSONObject bitrateJson = new JSONObject(bitrateJsonStr);
        int bitrate = demand.getDemand().getBitRate();

        // Buscar la cantidad de FS para la modulación y el bitrate dados
        JSONObject modulationJson = bitrateJson.optJSONObject(String.valueOf(bitrate));
        if (modulationJson != null) {
            JSONObject modulationInfoJson = modulationJson.optJSONObject(modulation);
            if (modulationInfoJson != null) {
                demand.setModulation(modulation);
                demand.getDemand().setFs(modulationInfoJson.getInt("cantidad_de_fs"));
                return;
            }
        }

        // Si no se encuentra información para la modulación y el bitrate especificados,
        // devolver un valor predeterminado o lanzar una excepción según el caso
        demand.setFs(-1); // Valor predeterminado, puedes ajustar este valor según tus necesidades
        // O bien, puedes lanzar una excepción si no se encuentra información para la modulación y el bitrate:
        // throw new IllegalArgumentException("No se encontró información para la modulación y el bitrate especificados.");
    }

    private List<ModulationInfo> parseJsonToModulationInfoList(String jsonStr) {
        List<ModulationInfo> modulationInfoList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String modulation = jsonObject.getString("modulation");
            int distance = jsonObject.getInt("distance");
            modulationInfoList.add(new ModulationInfo(modulation, distance));
        }
        return modulationInfoList;
    }
}
