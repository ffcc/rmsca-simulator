package py.una.pol.domain;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Parameter {

    private String topology;

    private Double fsWidth;

    private Integer coreCapacity;

    private Integer linkCapacity;

    private String ksp;

    private Integer demandsLimit;

    private String sortingStrategy;

    private Integer maxCrosstalkDb;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal maxCrosstalk;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal unitCrosstalk;
}
