package ar.com.tagscreen.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ar.com.tagscreen.R;

public class RegisterEventActivity extends CommonActivity{
    private EditText mEventView;
    private String mEventId;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_register_event);
        mEventView = (EditText) findViewById(R.id.event);
        findViewById(R.id.register_event_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventView.setError(null);
                mEventId = mEventView.getText().toString().toLowerCase().replaceAll("#","");
                // Check for a valid user name.
                if (TextUtils.isEmpty(mEventId)) {
                    mEventView.setError(getString(R.string.error_field_required));
                }else{
                    checkIfEventExists();
                }
            }
        });
    }

    private void checkIfEventExists() {
        showProgress(true);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("fiestApp/fiestas");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showProgress(false);
                if (dataSnapshot.hasChild(mEventId)) {
                    Intent intent = new Intent(activity, InfoActivity.class);
                    intent.putExtra("fiestaId", mEventId);
                    startActivity(intent);
                }else{
                    mEventView.setError(getString(R.string.error_invalid_event_id));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected View getProgressView() {
        return findViewById(R.id.login_progress);
    }

    @Override
    protected View getFormView() {
        return findViewById(R.id.login_form);
    }
}
