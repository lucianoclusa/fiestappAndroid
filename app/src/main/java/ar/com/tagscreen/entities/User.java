package ar.com.tagscreen.entities;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luciano.clusa on 6/10/2017.
 */

public class User {
    private String userName;
    private String email;
    private String[] fiestas;
    private String Uid;

    public User(){}

    public User(String mEmail, String mUserName, String uid) {
        this.email = mEmail;
        this.userName = mUserName;
        this.Uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String[] getFiestas() {
        return fiestas;
    }

    public void setFiestas(String[] fiestas) {
        this.fiestas = fiestas;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public void saveNewUser() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("fiestApp/users");
        Map<String, User> users = new HashMap<String, User>();
        users.put(this.getUid(), this);
        ref.setValue(users);
    }

}
