package py.una.pol.domain;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Modulation {

    private String name;

    private int fsRequired;
}
