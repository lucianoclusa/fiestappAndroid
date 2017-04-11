package ar.com.fiestapp.entities;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luciano.clusa on 2/4/2017.
 */
@IgnoreExtraProperties
public class InfoFiesta {
    public boolean tieneFoto;
    public Map<String,String> data;
    public String fotoURL;

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
}
