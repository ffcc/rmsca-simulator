package py.una.pol.rest.model;

import java.util.List;

public class Response {
    private int nroDemanda;
    private int  cantRutasActivas;
    private int origen;
    private int destino;
    private int core;
    private int fsIndexBegin;
    private int fs;
    private List<Integer> path;
    private boolean block;
    private int slotBlock;
    private int MSI;

    public int getNroDemanda() {
        return nroDemanda;
    }

    public void setNroDemanda(int nroDemanda) {
        this.nroDemanda = nroDemanda;
    }

    public int getCantRutasActivas() {
        return cantRutasActivas;
    }

    public void setCantRutasActivas(int cantRutasActivas) {
        this.cantRutasActivas = cantRutasActivas;
    }

    public int getOrigen() {
        return origen;
    }

    public void setOrigen(int origen) {
        this.origen = origen;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public int getFsIndexBegin() {
        return fsIndexBegin;
    }

    public void setFsIndexBegin(int fsIndexBegin) {
        this.fsIndexBegin = fsIndexBegin;
    }

    public int getFs() {
        return fs;
    }

    public void setFs(int fs) {
        this.fs = fs;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public int getMSI() {
        return MSI;
    }

    public void setMSI(int MSI) {
        this.MSI = MSI;
    }

    public int getSlotBlock() {
        return slotBlock;
    }

    public void setSlotBlock(int slotBlock) {
        this.slotBlock = slotBlock;
    }
}
