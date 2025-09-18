package py.una.pol.domain;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document("simulations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Simulation {

    private Version version;

    private Configuration configuration;

    private Parameter parameter;

    private Network network;

    private List<Demand> demands;

    private List<RejectedDemand> demandsRejected;

    private Result result;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public Demand getDemand(int i) {
        return this.demands.stream().filter(d -> d.index == i).findFirst().orElseThrow();
    }

    public void addDemand(Demand demand) {
        this.demands.add(demand);
    }

    public void addRejectedDemand(RejectedDemand rejectedDemand) {
        this.demandsRejected.add(rejectedDemand);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Demand {

        private Integer index;

        private Integer source;

        private Integer target;

        private Integer bitRate;

        private String modulation;

        private Integer distance;

        private DemandStatus status;

        private List<KspPath> kspPaths;

        public enum DemandStatus {
            PENDING, ACTIVE, BLOCKED
        }
    }
}
