package py.una.pol.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;


public class BitrateInfoDTO {
    private Map<String, Map<String, ModulationInfoDTO>> bitrateMap = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Map<String, ModulationInfoDTO>> getBitrateMap() {
        return bitrateMap;
    }

    @JsonAnySetter
    public void setBitrate(String bitrate, Map<String, ModulationInfoDTO> modulationInfoDTO) {
        bitrateMap.put(bitrate, modulationInfoDTO);
    }
}