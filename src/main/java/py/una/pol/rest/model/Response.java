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
    private String path;
    private int bitrate;
    private String modulation;
    private boolean block;
    private int slotBlock;
    private int MSI;

    private int fsMax;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getModulation() {
        return modulation;
    }

    public void setModulation(String modulation) {
        this.modulation = modulation;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public int getSlotBlock() {
        return slotBlock;
    }

    public void setSlotBlock(int slotBlock) {
        this.slotBlock = slotBlock;
    }

    public int getMSI() {
        return MSI;
    }

    public void setMSI(int MSI) {
        this.MSI = MSI;
    }

    public int getFsMax() {
        return fsMax;
    }

    public void setFsMax(int fsMax) {
        this.fsMax = fsMax;
    }
}
