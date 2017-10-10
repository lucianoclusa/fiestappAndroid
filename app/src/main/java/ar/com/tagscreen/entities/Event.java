package ar.com.tagscreen.entities;

import java.util.Date;
import java.util.List;

/**
 * Created by luciano.clusa on 22/12/2016.
 */

public class Event {
    String fiestaId;
    List<Imagen> imagenes;
    List<Video> videos;
    List<Firma> firmas;
    Date fechaCreacion;

    public String getFiestaId() {
        return fiestaId;
    }

    public void setFiestaId(String fiestaId) {
        this.fiestaId = fiestaId;
    }

    public List<Imagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<Imagen> imagenes) {
        this.imagenes = imagenes;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public List<Firma> getFirmas() {
        return firmas;
    }

    public void setFirmas(List<Firma> firmas) {
        this.firmas = firmas;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

}
