package py.una.pol.algorithms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import py.una.pol.rest.model.*;

import java.io.IOException;
import java.util.*;

public class ModulationCalculator {

    private List<ModulationInfo> modulationInfoList;

    public String calculateModulation(Demand demand) throws IOException {

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

    public boolean calculateFS(Demand demand) {
        try {
            // Obtener la modulación adecuada para la demanda
            String modulation = calculateModulation(demand);

            // Elegir aleatoriamente un bitrate dentro del rango
            Random random = new Random();
            int selectedBitrate = demand.getBitRate();

            // Cargar el archivo JSON en un DTO
            String bitrateFileName = "modulation/bitrate.json";
            String bitrateJsonStr = new String(ModulationCalculator.class.getClassLoader().getResourceAsStream(bitrateFileName).readAllBytes());
            ObjectMapper objectMapper = new ObjectMapper();
            BitrateInfoDTO bitrateInfo = objectMapper.readValue(bitrateJsonStr, BitrateInfoDTO.class);

            // Buscar la cantidad de FS correspondiente al bitrate seleccionado y la modulación
            ModulationInfoDTO modulationInfo = null;

            // Verificar si el bitrate seleccionado está presente en el DTO
            if (bitrateInfo.getBitrateMap().containsKey(String.valueOf(selectedBitrate))) {
                // Verificar si la modulación seleccionada está presente en el DTO para el bitrate dado
                Map<String, ModulationInfoDTO> modulationMap = bitrateInfo.getBitrateMap().get(String.valueOf(selectedBitrate));
                if (modulationMap.containsKey(modulation)) {
                    modulationInfo = modulationMap.get(modulation);
                }
            }

            if (modulationInfo != null) {
                int fs = modulationInfo.getCantidadDeFs();

                if (fs <= 0 || fs > 8) {
                    // No hay FS disponibles, reject demand
                    return false;
                }

                // Establecer la modulación y la cantidad de FS en la demanda
                demand.setModulation(modulation);
                demand.setFs(fs);

                System.out.println("bitrate: " + selectedBitrate + ", Modulacion: " + modulation + " y FS: " + fs);

                return true;
            } else {
                // La combinación específica no está presente en el DTO
                System.out.println("Combinación de bitrate y modulación no encontrada en el DTO");
                return false;
            }
        } catch (IOException e) {
            // Manejar la excepción de lectura del archivo JSON
            System.out.println("Error al leer el archivo JSON: " + e.getMessage());
            return false;
        }
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
