package py.una.pol.domain;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Network {

    private List<Integer> nodes;

    private List<Link> links;

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @Builder
    public static class Link {

        private Integer from;

        private Integer to;

        private Integer distance;

        private List<Core> cores;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @Builder
    public static class Core {

        private Double fsWith;

        private Integer capacity;

        private List<FrequencySlot> fsList;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class FrequencySlot {

        private Integer index;

        private Boolean free;

        @Field(targetType = FieldType.DECIMAL128)
        @Builder.Default
        private BigDecimal crosstalk = BigDecimal.ZERO;
    }
}
