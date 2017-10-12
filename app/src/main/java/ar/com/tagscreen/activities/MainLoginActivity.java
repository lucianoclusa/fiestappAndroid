package ar.com.tagscreen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ar.com.tagscreen.R;
import ar.com.tagscreen.TagScreen;
import ar.com.tagscreen.entities.User;
import ar.com.tagscreen.utils.Constants;


public class MainLoginActivity extends CommonActivity implements GoogleApiClient.OnConnectionFailedListener {

    /********FIREBASE************/
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean called = false;

    /**********GOOGLE***********/
    public static final int RC_SIGN_IN = 2020;
    private SignInButton mSignInButton;
    private GoogleApiClient mGoogleApiClient;

    private MainLoginActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activity=this;
        showProgress(true);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Flag porque el listener se vuelve a llamar cuando se logea y se hace un bucle.
                if (!called) {
                    called = true;
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        //usuario logeado se avanza para ver como se sigue
                        advanceWithUser(user, null);
                    } else {
                        showProgress(false);
                        Log.d(Constants.TAG, "onAuthStateChanged:signed_out");
                    }
                }
            }
        };

        initializeGoogleSignInButton();
        findViewById(R.id.email_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, FirebaseLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void advanceWithUser(final FirebaseUser fbUser, final GoogleSignInAccount googleSignIn) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("fiestApp").child("users/"+fbUser.getUid());

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null && dataSnapshot.getValue(User.class)!=null) {
                    final User user = dataSnapshot.getValue(User.class);
                    ((TagScreen)getApplication()).setCurrentUser(user);
                    if (getCurrentEvent(user)) {
                        // Usuario logeado, guardado en la base y con evento registrado
                        gotToMain();
                    } else {
                        // Usuario logeado, guardado en la base pero sin evento evento registrado
                        gotToRegisterEvent();
                    }
                }else{
                    // Usuario logeado, no guardado en la base
                    if(googleSignIn!=null){
                        // Si es por google se guarda el usuario con los datos de Google
                        String personName = googleSignIn.getDisplayName();
                        String personEmail = googleSignIn.getEmail();
                        User user = new User(personEmail,personName,fbUser.getUid());
                        ((TagScreen)getApplication()).setCurrentUser(user);
                        user.saveOrUpdate();
                        gotToRegisterEvent();
                    }else {
                        // Si es por mail se tiene que registrar con nombre mail y pass
                        gotToRegisterUser();
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

    private boolean getCurrentEvent(User user) {
        return user.getCurrentEvent()!=null;
    }
    /***********************METODOS DE GOOGLE****************************/

    private void initializeGoogleSignInButton() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inicioSesionGoogle();
            }
        });
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
    }
    private void inicioSesionGoogle() {
        showProgress(true);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result, Intent data) {
        Log.e(Constants.TAG, "Google Login Result:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct,data);
        } else {
            showAuthenticationError();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showAuthenticationError();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result,data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct, final Intent data) {
        Log.d(Constants.TAG, "firebaseAuthWithGoogle:" + acct.getId());
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                            GoogleSignInAccount acct = result.getSignInAccount();
                            advanceWithUser(task.getResult().getUser(), acct);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Constants.TAG, "signInWithCredential:failure", task.getException());
                            showAuthenticationError();
                        }
                    }
                });
    }

    //***********************FIN GOOGLE***************************//

    private void gotToMain(){
        Intent intent = new Intent(activity, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void gotToRegisterEvent(){
        Intent intent = new Intent(activity, RegisterEventActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotToRegisterUser() {
        mAuth.signOut();
        Intent intent = new Intent(activity, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAuthenticationError(){
        this.showProgress(false);
        Toast.makeText(activity, "Falló autenticación.",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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

