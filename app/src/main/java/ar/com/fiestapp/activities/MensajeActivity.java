package ar.com.fiestapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.fiestapp.FiestApp;
import ar.com.fiestapp.R;
import ar.com.fiestapp.entities.Firma;

public class MensajeActivity extends AppCompatActivity {

    Activity activity;
    TextView firmaView;
    TextView contenidoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity=this;
        setContentView(R.layout.activity_mensaje);
        firmaView = (TextView) findViewById(R.id.firma);
        contenidoView = (TextView) findViewById(R.id.contenido);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
                    String fiestaIdAux = ((FiestApp)getApplication()).getFiestaId();

                    if(fiestaIdAux==null) {
                        SharedPreferences sharedPref = getSharedPreferences("FiestApp", Context.MODE_PRIVATE);
                        fiestaIdAux = sharedPref.getString("fiestaId", null);
                    }

                    final String fiestaIdFinal = fiestaIdAux.toLowerCase();

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
                    Toast.makeText(activity, R.string.mensaje_enviada, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });


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

}
