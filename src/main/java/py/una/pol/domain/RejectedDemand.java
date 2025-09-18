package py.una.pol.domain;

import lombok.*;
import py.una.pol.domain.Simulation.Demand;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RejectedDemand {

    private Demand demand;

    private Reason reason;

    public enum Reason {
        MAX_PERMITTED_DISTANCE_EXCEEDED,
        NOT_FS_AVAILABLE,
        PAIR_BITRATE_MODULATION_NOT_FOUND,
        UNKNOWN_ERROR
    }
}
