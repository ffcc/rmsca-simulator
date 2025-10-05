package py.una.pol.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Options {

    private String topology;

    private float fsWidth;

    private int capacity;

    private int cores;

    private String shortestAlg;

    private int demandsQuantity;

    private String sortingDemands;

    private Integer maxCrosstalkDb;

    private BigDecimal maxCrosstalk;

    private Double crosstalkPerUnitLenght;

    private String crosstalkPerUnitLengthName;
}
