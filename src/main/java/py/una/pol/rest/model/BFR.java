package py.una.pol.rest.model;

public class BFR {
    private String alias;
    private Double bfr;

    BFR(String alias , Double bfr) {
        this.alias = alias;
        this.bfr = bfr;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Double getBfr() {
        return bfr;
    }

    public void setBfr(Double bfr) {
        this.bfr = bfr;
    }
}
