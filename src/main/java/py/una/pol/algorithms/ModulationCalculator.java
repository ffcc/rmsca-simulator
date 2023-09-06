package py.una.pol.algorithms;

import org.json.JSONArray;
import org.json.JSONObject;
import py.una.pol.rest.model.DemandDistancePair;
import py.una.pol.rest.model.ModulationInfo;

import java.io.IOException;
import java.util.*;

public class ModulationCalculator {

    private List<ModulationInfo> modulationInfoList;

    public String calculateModulation(DemandDistancePair demand) throws IOException {

        String surveyFileName = "modulation/survey.json";
        String jsonStr = new String(ModulationCalculator.class.getClassLoader().getResourceAsStream(surveyFileName).readAllBytes());

        modulationInfoList = parseJsonToModulationInfoList(jsonStr);

        // Ordenar la lista por distancia para realizar búsqueda binaria
        Collections.sort(modulationInfoList, Comparator.comparingDouble(info -> info.distance));

        // Encontrar la modulación adecuada utilizando búsqueda binaria
        String selectedModulation = null;

        // recorremos la lista de modulaciones, y elegimos uno acorde a la distancia que necesitamos
        for (ModulationInfo info: modulationInfoList) {
            if (demand.getDistance() <= info.distance) {
                selectedModulation = info.modulation;
                break;
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

        // Definir rangos de bitrate según la modulación
        int[] bitrates;

        if ("BPSK".equals(modulation)) {
            // Rangos de bitrate para BPSK
            bitrates = new int[]{10, 40, 100};
        } else if ("QPSK".equals(modulation) || "8-QAM".equals(modulation)) {
            bitrates = new int[]{40, 100};
        } else {
            bitrates = new int[]{40, 100, 400};
        }

        // Elegir aleatoriamente un bitrate dentro del rango
        Random random = new Random();
        int selectedBitrate = bitrates[random.nextInt(bitrates.length)];

        // Cargar el archivo JSON
        String bitrateFileName = "modulation/bitrate.json";
        String bitrateJsonStr = new String(ModulationCalculator.class.getClassLoader().getResourceAsStream(bitrateFileName).readAllBytes());
        JSONObject bitrateJson = new JSONObject(bitrateJsonStr);

        // Buscar la cantidad de FS correspondiente al bitrate seleccionado y la modulación
        JSONObject modulationInfo = bitrateJson.getJSONObject(String.valueOf(selectedBitrate));
        int fs = modulationInfo.getJSONObject(modulation).getInt("cantidad_de_fs");

        // Establecer la modulación y la cantidad de FS en la demanda
        demand.setModulation(modulation);
        demand.getDemand().setFs(fs);
        demand.getDemand().setBitRate(selectedBitrate);

        System.out.println("bitrate: " + selectedBitrate + ", Modulacion: " + modulation + " y FS: " + fs);

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
