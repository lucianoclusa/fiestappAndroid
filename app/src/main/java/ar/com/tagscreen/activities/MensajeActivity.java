package ar.com.tagscreen.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.tagscreen.TagScreen;
import ar.com.tagscreen.R;
import ar.com.tagscreen.entities.Firma;
import ar.com.tagscreen.entities.InfoFiesta;
import ar.com.tagscreen.utils.Constants;

public class MensajeActivity extends AppCompatActivity {

    Activity activity;
    TextView firmaView;
    TextView contenidoView;
    public String fiestaIdFinal;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity=this;
        setContentView(R.layout.activity_mensaje);
        firmaView = (TextView) findViewById(R.id.firma);
        contenidoView = (TextView) findViewById(R.id.contenido);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setTitle("Enviar comentario");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String fiestaIdAux = ((TagScreen) getApplication()).getFiestaId();

        if (fiestaIdAux == null) {
            SharedPreferences sharedPref = getSharedPreferences("TagScreen", Context.MODE_PRIVATE);
            fiestaIdAux = sharedPref.getString("fiestaId", null);
        }

        fiestaIdFinal = fiestaIdAux.toLowerCase();

        Button sendFirma = (Button) findViewById(R.id.send_message_button);
        sendFirma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset errors.
                firmaView.setError(null);
                contenidoView.setError(null);

                // Store values at the time of the login attempt.
                String remitente = firmaView.getText().toString();
                String contenido = contenidoView.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid firm.
                if (TextUtils.isEmpty(remitente)) {
                    firmaView.setError(getString(R.string.error_field_required));
                    focusView = firmaView;
                    cancel = true;
                }

                // Check for a valid firm.
                if (TextUtils.isEmpty(contenido)) {
                    contenidoView.setError(getString(R.string.error_field_required));
                    focusView = contenidoView;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                }else{

                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("fiestApp").child("fiestas/"+fiestaIdFinal);

                    final String keyFirmas = myRef.child(fiestaIdFinal).child("firmas").push().getKey();

                    Firma firma = new Firma();
                    firma.setRemitente(remitente);
                    firma.setDedicaoria(contenido);
                    firma.setTime(new Date().getTime());
                    firma.setId(keyFirmas);
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/firmas/" + keyFirmas, firma);

                    myRef.updateChildren(childUpdates);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, keyFirmas);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Constants.EVENT_UPLOAD);
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "firma");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    Toast.makeText(activity, R.string.mensaje_enviada, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        getInfoFiesta();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }


    private void getInfoFiesta() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("fiestApp").child("fiestas/" + fiestaIdFinal + "/info");
        ValueEventListener postListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InfoFiesta info = dataSnapshot.getValue(InfoFiesta.class);
                if (info!=null && info.isChangeTheme()) {
                    changeTheme(info);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(Constants.TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        myRef.addListenerForSingleValueEvent(postListener);
    }

    private void changeTheme(InfoFiesta info) {
        try {
            if (info.isChangeTheme()) {
                if (info.getStatusBarColour() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setNavigationBarColor(Color.parseColor(info.getStatusBarColour()));
                        getWindow().setStatusBarColor(Color.parseColor(info.getStatusBarColour()));
                    }
                }
                if (info.getActionBarColour() != null) {
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(info.getActionBarColour())));
                }
            }
        }catch(Exception e){
            FirebaseCrash.report(e);
        }
    }

}
