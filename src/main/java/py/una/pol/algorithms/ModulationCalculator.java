package py.una.pol.algorithms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import py.una.pol.domain.KspPath;
import py.una.pol.domain.Modulation;
import py.una.pol.domain.RejectedDemand;
import py.una.pol.domain.Simulation;
import py.una.pol.exception.ModulationNotFoundException;
import py.una.pol.exception.ModulationNotSupportedException;
import py.una.pol.model.BitrateInfoDTO;
import py.una.pol.model.Demand;
import py.una.pol.model.ModulationInfo;
import py.una.pol.model.ModulationInfoDTO;

import java.io.IOException;
import java.util.*;

public class ModulationCalculator {

    private static final List<ModulationInfo> modulationInfoList;

    private static final BitrateInfoDTO bitrateInfo;

    static {
        ObjectMapper objectMapper = new ObjectMapper();

        /* Loading modulation info */
        var modulationResource = new ClassPathResource("modulation/survey.json");
        try {
            modulationInfoList = objectMapper.readValue(modulationResource.getFile(), new TypeReference<>() {});
            modulationInfoList.sort(Comparator.comparingDouble(ModulationInfo::getDistance));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Loading bitrate info */
        var bitRateResource = new ClassPathResource("modulation/bitrate.json");
        try {
            bitrateInfo = objectMapper.readValue(bitRateResource.getFile(), BitrateInfoDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String calculateModulation(int distance) {
        // Encontrar la modulación adecuada utilizando búsqueda binaria
        String selectedModulation = null;

        // recorremos la lista de modulaciones, y elegimos uno acorde a la distancia que necesitamos
        for (ModulationInfo info: modulationInfoList) {
            if (distance <= info.getDistance()) {
                selectedModulation = info.getModulation();
                break;
            }
        }

        // Si no se encuentra ninguna modulación, elegir la modulación con la menor tasa de bits
        if (selectedModulation == null && !modulationInfoList.isEmpty()) {
            selectedModulation = modulationInfoList.get(modulationInfoList.size() - 1).getModulation();
        }

        return selectedModulation;
    }

    public boolean calculateFS(Simulation simulation, Demand demand, KspPath simulationKsp) {
        // Obtener la modulación adecuada para la demanda
        String modulationName = calculateModulation(demand.getDistance());

        // Elegir aleatoriamente un bitrate dentro del rango
        int selectedBitrate = demand.getBitRate();

        // Buscar la cantidad de FS correspondiente al bitrate seleccionado y la modulación
        ModulationInfoDTO modulationInfo = null;

        // Verificar si el bitrate seleccionado está presente en el DTO
        if (bitrateInfo.getBitrateMap().containsKey(String.valueOf(selectedBitrate))) {
            // Verificar si la modulación seleccionada está presente en el DTO para el bitrate dado
            Map<String, ModulationInfoDTO> modulationMap = bitrateInfo.getBitrateMap().get(String.valueOf(selectedBitrate));
            if (modulationMap.containsKey(modulationName)) {
                modulationInfo = modulationMap.get(modulationName);
            }
        }

        if (modulationInfo != null) {
            int fs = modulationInfo.getCantidadDeFs();

            if (fs <= 0 || fs > 8) {
                simulation.addRejectedDemand(RejectedDemand.builder()
                        .demand(Simulation.Demand.builder()
                                .source(demand.getSource())
                                .target(demand.getDestination())
                                .bitRate(demand.getBitRate())
                                .build())
                        .reason(RejectedDemand.Reason.NOT_FS_AVAILABLE)
                        .build());
                // No hay FS disponibles, reject demand
                return false;
            }

            // Establecer la modulación y la cantidad de FS en la demanda
            demand.setModulation(modulationName);
            demand.setFs(fs);

            if (simulationKsp != null) {
                simulationKsp.setModulation(modulationName);
            }

            return true;
        } else {
            simulation.addRejectedDemand(RejectedDemand.builder()
                    .demand(Simulation.Demand.builder()
                            .source(demand.getSource())
                            .target(demand.getDestination())
                            .bitRate(demand.getBitRate())
                            .build())
                    .reason(RejectedDemand.Reason.PAIR_BITRATE_MODULATION_NOT_FOUND)
                    .build());
            // La combinación específica no está presente en el DTO
            return false;
        }
    }

    public Modulation calculateModulation(int bitRate, int distance) throws ModulationNotFoundException, ModulationNotSupportedException {
        Modulation modulation = null;
        // Obtener la modulación adecuada para la demanda
        var modulationName = calculateModulation(distance);

        // Buscar la cantidad de FS correspondiente al bitrate seleccionado y la modulación
        ModulationInfoDTO modulationInfo = null;

        // Verificar si el bitrate seleccionado está presente en el DTO
        if (bitrateInfo.getBitrateMap().containsKey(String.valueOf(bitRate))) {
            // Verificar si la modulación seleccionada está presente en el DTO para el bitrate dado
            Map<String, ModulationInfoDTO> modulationMap = bitrateInfo.getBitrateMap().get(String.valueOf(bitRate));
            if (modulationMap.containsKey(modulationName)) {
                modulationInfo = modulationMap.get(modulationName);
            }
        }

        if (modulationInfo == null) {
            throw new ModulationNotFoundException(bitRate, distance);
        }

        if (modulationInfo.getCantidadDeFs() < 1 || modulationInfo.getCantidadDeFs() > 8) {
            throw new ModulationNotSupportedException(modulationInfo.getCantidadDeFs());
        }

        return Modulation.builder()
                .name(modulationName)
                .fsRequired(modulationInfo.getCantidadDeFs())
                .build();
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
