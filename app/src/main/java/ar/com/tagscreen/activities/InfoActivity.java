package ar.com.tagscreen.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import ar.com.tagscreen.TagScreen;
import ar.com.tagscreen.R;
import ar.com.tagscreen.entities.InfoFiesta;
import ar.com.tagscreen.utils.Constants;

public class InfoActivity extends AppCompatActivity {
    ImageView photoView;
    Activity activity;
    private final String ASISTIRE = "asistire";
    private final String TAL_VEZ = "talVez";
    private final String NO_ASISTIRE = "noAsistire";
    private String fiestaId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        photoView = (ImageView) findViewById(R.id.info_foto);
        activity =this;
        setTitle("Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.asistire).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAsistencia(ASISTIRE);
            }
        });
        findViewById(R.id.tal_vez).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAsistencia(TAL_VEZ);
            }
        });
        findViewById(R.id.no_asistire).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAsistencia(NO_ASISTIRE);
            }
        });
        fiestaId = getIntent().getExtras().getString("fiestaId");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("fiestApp").child("fiestas/"+fiestaId + "/info");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                final InfoFiesta info = dataSnapshot.getValue(InfoFiesta.class);
                if(info.isTieneFoto()) {
                    Picasso.with(activity).load(info.getFotoURL()).into(photoView);
                    if (info.getInfoURL() != null && !info.getInfoURL().equals("")){
                        photoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(info.getInfoURL()));
                                startActivity(i);
                            }
                        });
                    }
                }else{
                    LinearLayout contenidoInfo = (LinearLayout) findViewById(R.id.contenidoInfo);
                    for (Map.Entry<String, String> entry : info.getData().entrySet()){
                        TextView lineaData = new TextView(InfoActivity.this);
                        lineaData.setText(entry.getKey() + ": " + entry.getValue());
                        lineaData.setTextSize(20);
                        lineaData.setClickable(true);
                        lineaData.setLayoutParams(new LinearLayout.LayoutParams(
                                new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT)));
                        lineaData.setGravity(Gravity.CENTER);
                        contenidoInfo.addView(lineaData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(Constants.TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        myRef.addListenerForSingleValueEvent(postListener);

    }

    private void saveAsistencia(String asistire) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference eventosRef = database.getReference("fiestApp").child("fiestas/" + fiestaId);
        Map<String, Object> eventoUpdates = new HashMap<>();
        DatabaseReference usuariosRef = database.getReference("fiestApp").child("users/" + ((TagScreen)getApplication()).getCurrentUser().getUid());
        Map<String, Object> usuarioUpdates = new HashMap<>();
        switch (asistire){
            case ASISTIRE:
                eventoUpdates.put("/asistire/" + ((TagScreen)getApplication()).getCurrentUser().getUid(), true);
                eventosRef.child("/talvez/" + ((TagScreen)getApplication()).getCurrentUser().getUid()).removeValue();
                eventosRef.child("/noasistire/" + ((TagScreen)getApplication()).getCurrentUser().getUid()).removeValue();
                eventosRef.updateChildren(eventoUpdates);
                usuarioUpdates.put("/asistire/"+fiestaId, true);
                usuariosRef.child("/talvez/"+fiestaId).removeValue();
                usuariosRef.child("/noasistire/"+fiestaId).removeValue();
                usuariosRef.updateChildren(usuarioUpdates);
                break;
            case TAL_VEZ:
                eventoUpdates.put("/talvez/" + ((TagScreen)getApplication()).getCurrentUser().getUid(), true);
                eventosRef.child("/asistire/" + ((TagScreen)getApplication()).getCurrentUser().getUid()).removeValue();
                eventosRef.child("/noasistire/" + ((TagScreen)getApplication()).getCurrentUser().getUid()).removeValue();
                eventosRef.updateChildren(eventoUpdates);
                usuarioUpdates.put("/talvez/"+fiestaId, true);
                usuariosRef.child("/asistire/"+fiestaId).removeValue();
                usuariosRef.child("/noasistire/"+fiestaId).removeValue();
                usuariosRef.updateChildren(usuarioUpdates);
                break;
            case NO_ASISTIRE:
                eventoUpdates.put("/noasistire/" + ((TagScreen)getApplication()).getCurrentUser().getUid(), true);
                eventosRef.child("/talvez/" + ((TagScreen)getApplication()).getCurrentUser().getUid()).removeValue();
                eventosRef.child("/asistire/" + ((TagScreen)getApplication()).getCurrentUser().getUid()).removeValue();
                eventosRef.updateChildren(eventoUpdates);
                usuarioUpdates.put("/noasistire/"+fiestaId, true);
                usuariosRef.child("/talvez/"+fiestaId).removeValue();
                usuariosRef.child("/asistire/"+fiestaId).removeValue();
                usuariosRef.updateChildren(usuarioUpdates);
                break;
        }
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
