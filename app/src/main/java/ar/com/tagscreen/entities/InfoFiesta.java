package ar.com.tagscreen.entities;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luciano.clusa on 2/4/2017.
 */
@IgnoreExtraProperties
public class InfoFiesta {
    private boolean tieneFoto;
    private boolean changeTheme;
    private String backgroundURL;
    private String statusBarColour;
    private String actionBarColour;
    private String buttonsColour;
    private Map<String,String> data;
    private String fotoURL;
    private String eventTime;
    private float eventDuration;
    private String infoURL;

    public InfoFiesta(){}

    public InfoFiesta (boolean tieneFoto, Map<String,String> data, String fotoURL){
        this.data=data;
        this.fotoURL=fotoURL;
        this.tieneFoto=tieneFoto;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("tieneFoto", tieneFoto);
        result.put("data", data);
        result.put("fotoURL", fotoURL);
        return result;
    }

    public boolean isTieneFoto() {
        return tieneFoto;
    }

    public void setTieneFoto(boolean tieneFoto) {
        this.tieneFoto = tieneFoto;
    }

    public Map<String,String> getData() {
        return data;
    }

    public void setData(Map<String,String> data) {
        this.data = data;
    }

    public String getFotoURL() {
        return fotoURL;
    }

    public void setFotoURL(String fotoURL) {
        this.fotoURL = fotoURL;
    }

    public boolean isChangeTheme() {
        return changeTheme;
    }

    public void setChangeTheme(boolean changeTheme) {
        this.changeTheme = changeTheme;
    }

    public String getBackgroundURL() {
        return backgroundURL;
    }

    public void setBackgroundURL(String backgroundURL) {
        this.backgroundURL = backgroundURL;
    }

    public String getStatusBarColour() {
        return statusBarColour;
    }

    public void setStatusBarColour(String statusBarColour) {
        this.statusBarColour = statusBarColour;
    }

    public String getActionBarColour() {
        return actionBarColour;
    }

    public void setActionBarColour(String actionBarColour) {
        this.actionBarColour = actionBarColour;
    }

    public String getButtonsColour() {
        return buttonsColour;
    }

    public void setButtonsColour(String buttonsColour) {
        this.buttonsColour = buttonsColour;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public float getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(float eventDuration) {
        this.eventDuration = eventDuration;
    }

    public String getInfoURL() {
        return infoURL;
    }

    public void setInfoURL(String infoURL) {
        this.infoURL = infoURL;
    }
}
