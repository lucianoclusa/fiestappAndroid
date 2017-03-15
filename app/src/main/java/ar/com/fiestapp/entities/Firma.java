package ar.com.fiestapp.entities;

/**
 * Created by luciano.clusa on 22/12/2016.
 */
public class Firma {

    String id;
    String remitente;
    String dedicaoria;
    long time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getDedicaoria() {
        return dedicaoria;
    }

    public void setDedicaoria(String dedicaoria) {
        this.dedicaoria = dedicaoria;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
