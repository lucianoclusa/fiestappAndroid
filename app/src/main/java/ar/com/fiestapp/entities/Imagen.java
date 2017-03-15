package ar.com.fiestapp.entities;

import java.util.Date;

/**
 * Created by luciano.clusa on 22/12/2016.
 */
public class Imagen {
    String id;
    String url;
    long time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
