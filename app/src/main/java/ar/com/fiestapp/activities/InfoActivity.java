package ar.com.fiestapp.activities;

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

import java.util.Map;

import ar.com.fiestapp.FiestApp;
import ar.com.fiestapp.R;
import ar.com.fiestapp.entities.InfoFiesta;
import ar.com.fiestapp.utils.Constants;

import static android.R.id.message;

public class InfoActivity extends AppCompatActivity {
    ImageView photoView;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        photoView = (ImageView) findViewById(R.id.info_foto);
        activity =this;
        setTitle("Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String fiestaIdAux = ((FiestApp)getApplication()).getFiestaId();

        if(fiestaIdAux==null) {
            SharedPreferences sharedPref = getSharedPreferences("FiestApp", Context.MODE_PRIVATE);
            fiestaIdAux = sharedPref.getString("fiestaId", null);
        }

        final String fiestaIdFinal = fiestaIdAux.toLowerCase();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("fiestApp").child("fiestas/"+fiestaIdFinal + "/info");

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
