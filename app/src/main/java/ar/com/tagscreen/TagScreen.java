package ar.com.tagscreen;

import android.app.Application;

/**
 * Created by luciano.clusa on 20/12/2016.
 */
public class TagScreen extends Application {
    public String getFiestaId() {
        return fiestaId;
    }

    public void setFiestaId(String fiestaId) {
        this.fiestaId = fiestaId;
    }

    public String fiestaId;

}
