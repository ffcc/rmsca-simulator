package py.una.pol.domain;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KspPath {

    private Integer distance;

    private List<Integer> nodes;

    private List<Integer> cores;

    private Double bfr;

    private Integer msi;

    private String modulation;

    private KspPathStatus status;

    public enum KspPathStatus {
        PENDING, CANDIDATE, ESTABLISHED, REJECTED
    }
}
