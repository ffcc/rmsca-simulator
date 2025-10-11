package py.una.pol.model;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Response {
    private int cantDemandas;
    private int cantBloqueos;
    private int fsMax;
}
