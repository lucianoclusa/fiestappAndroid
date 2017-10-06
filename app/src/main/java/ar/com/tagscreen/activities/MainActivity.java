package ar.com.tagscreen.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import ar.com.tagscreen.TagScreen;
import ar.com.tagscreen.R;
import ar.com.tagscreen.entities.Imagen;
import ar.com.tagscreen.entities.InfoFiesta;
import ar.com.tagscreen.entities.Video;
import ar.com.tagscreen.utils.Constants;
import ar.com.tagscreen.utils.ImageUtils;

public class MainActivity extends AppCompatActivity {

    private static final int FOTO = 1;
    private static final int VIDEO = 2;
    MainActivity activity;
    private FirebaseAuth mAuth;
    private String mCurrentPhotoPath;
    public Uri downloadUrl;
    public Uri videoUri;
    public ByteArrayOutputStream baos;
    public StorageReference storageRef;
    public String fiestaIdFinal;
    public String keyVideos;
    public DatabaseReference myRef;
    private Uri photoURI;
    private ImageView backgroundImage;
    private ImageButton cameraButton;
    private ImageButton videoButton;
    private ImageButton mensajeButton;
    private FirebaseAnalytics mFirebaseAnalytics;
    InfoFiesta infoFiesta;
    String error;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        activity = this;
        mAuth = FirebaseAuth.getInstance();
        backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        videoButton = (ImageButton) findViewById(R.id.videoButton);
        mensajeButton = (ImageButton) findViewById(R.id.mensajeButton);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView)findViewById(R.id.versionNumber)).setText(pInfo.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String fiestaIdAux = ((TagScreen) getApplication()).getFiestaId();

        if (fiestaIdAux == null) {
            SharedPreferences sharedPref = getSharedPreferences("TagScreen", Context.MODE_PRIVATE);
            fiestaIdAux = sharedPref.getString("fiestaId", null);
        }

        fiestaIdFinal = fiestaIdAux.toLowerCase();

        setTitle("#" + fiestaIdFinal);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("fiestApp").child("fiestas/" + fiestaIdFinal);
        this.getEventInfo();

        View cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validateAction()) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.v(Constants.TAG, "Can't create file to take picture!");
                            Toast.makeText(activity, "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT).show();
                            FirebaseCrash.report(ex);
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            photoURI = FileProvider.getUriForFile(activity,
                                    "ar.com.android.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, FOTO);
                        }
                    }
                }else{
                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                }

            }
        });
        View videoButton = findViewById(R.id.videoButton);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateAction()) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
                    cameraIntent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    startActivityForResult(cameraIntent, VIDEO);
                }else{
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        View mensajeButton = findViewById(R.id.mensajeButton);
        mensajeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateAction()) {
                    Intent intent = new Intent(activity, MensajeActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void getEventInfo() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                infoFiesta = dataSnapshot.getValue(InfoFiesta.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(Constants.TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(activity, "No se pueden obtener los datos del evento.", Toast.LENGTH_SHORT).show();
            }
        };
        myRef.child("info").addListenerForSingleValueEvent(postListener);
    }

    private boolean validateAction() {
        if(infoFiesta!=null && infoFiesta.getEventTime()!=null && infoFiesta.getEventDuration()>0){
            try {
                SimpleDateFormat eventTimedf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                eventTimedf.setTimeZone(TimeZone.getTimeZone("GMT-03:00"));
                Date eventTime = eventTimedf.parse(infoFiesta.getEventTime());
                if(new Date().before(eventTime)){
                    SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ssa");
                    dt.setTimeZone(TimeZone.getTimeZone("GMT-03:00"));
                    error = "El evento comienza el " + dt.format(eventTime);
                    return false;
                }else{
                    Calendar cal = Calendar.getInstance(); // creates calendar
                    cal.setTimeInMillis(eventTime.getTime() + (long)(infoFiesta.getEventDuration()*60*60*1000));
                    cal.setTimeZone(TimeZone.getTimeZone("GMT-03:00"));

                    if(new Date().after(cal.getTime())){
                        error="El evento ya ha finalizado.";
                        return false;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return true;
            }
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReferenceFromUrl(Constants.DATA_STORE_URL);
            final StorageReference fileRef;


            final UploadTask uploadTask;
            final UploadTask uploadTaskThumbImage;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            if(resultCode!=RESULT_CANCELED){
                switch (requestCode) {
                    case FOTO:
                        Toast.makeText(activity, R.string.foto_subiendo, Toast.LENGTH_LONG).show();

                        final String keyImagenes = myRef.child(fiestaIdFinal).child("imagenes").push().getKey();
                        fileRef = storageRef.child(fiestaIdFinal + "/imagenes/" + keyImagenes + ".jpg");

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);

                        final Bitmap rotatedBitmap = rotateBitmap(bitmap);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                        byte[] bites = baos.toByteArray();

                        //Upload FullSizeImage
                        uploadTask = fileRef.putBytes(bites);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
                                FirebaseCrash.report(exception);
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Imagen imagen = new Imagen();
                                imagen.setTime(new Date().getTime());
                                imagen.setId(keyImagenes);
                                imagen.setUrl(downloadUrl.toString());
                                imagen.setThumbnailUrl(downloadUrl.toString());
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/imagenes/" + keyImagenes, imagen);
                                myRef.updateChildren(childUpdates);

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, keyImagenes);
                                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Constants.EVENT_UPLOAD);
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                Toast.makeText(activity, R.string.foto_enviada, Toast.LENGTH_LONG).show();

                                //uploadThumbImage(rotatedBitmap, fileRef, myRef, keyImagenes, downloadUrl.toString());
                            }
                        });


                        break;
                    case VIDEO:
                        Toast.makeText(activity, R.string.video_subiendo, Toast.LENGTH_LONG).show();

                        final String keyVideos = myRef.child(fiestaIdFinal).child("videos").push().getKey();
                        final Uri videoUri = intent.getData();

                        StorageReference videoRef = storageRef.child(fiestaIdFinal + "/videos/" + videoUri.getLastPathSegment() + ".mp4");
                        uploadTask = videoRef.putFile(videoUri);
                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
                                FirebaseCrash.report(exception);

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                final Uri downloadUrl = taskSnapshot.getDownloadUrl();


                                if (ContextCompat.checkSelfPermission(activity,
                                        Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    requestPermission(downloadUrl, videoUri, baos, storageRef, fiestaIdFinal, keyVideos, myRef);
                                    activity.downloadUrl = downloadUrl;
                                    activity.videoUri = videoUri;
                                    activity.baos = baos;
                                    activity.storageRef = storageRef;
                                    activity.fiestaIdFinal = fiestaIdFinal;
                                    activity.keyVideos = keyVideos;
                                    activity.myRef = myRef;
                                } else {
                                    uploadVideo(downloadUrl, videoUri, baos, storageRef, fiestaIdFinal, keyVideos, myRef);
                                }
                            }
                        });


                        break;
                }
            }else{
                this.photoURI = null;
                this.mCurrentPhotoPath = null;
            }


        } catch (Exception e) {
            Toast.makeText(MainActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, getResources().getString(R.string.upload_failed), e);
            FirebaseCrash.report(e);
        }

    }

    private Bitmap rotateBitmap(Bitmap bitmap) throws IOException {

        int rotation = ImageUtils.getRotation(this, photoURI, mCurrentPhotoPath);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        }
        return bitmap;
    }

    private void uploadVideo(final Uri downloadUrl, Uri videoUri, ByteArrayOutputStream baos, StorageReference storageRef, String fiestaIdFinal, final String keyVideos, final DatabaseReference myRef) {
        String path = getRealPathFromURI(activity, videoUri);

        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);

        thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bites2 = baos.toByteArray();

        StorageReference thumbRef = storageRef.child(fiestaIdFinal + "/videos/" + videoUri.getLastPathSegment() + ".jpg");
        UploadTask uploadTask2 = thumbRef.putBytes(bites2);

        uploadTask2.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
                FirebaseCrash.report(exception);


            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Video video = new Video();
                video.setTime(new Date().getTime());
                video.setId(keyVideos);
                video.setUrl(downloadUrl.toString());
                video.setThumbnailUrl(taskSnapshot.getDownloadUrl().toString());
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/videos/" + keyVideos, video);

                myRef.updateChildren(childUpdates);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, keyVideos);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Constants.EVENT_UPLOAD);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "video");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


                Toast.makeText(activity, R.string.video_enviado, Toast.LENGTH_LONG).show();

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_log_out) {
            Intent intent = new Intent(activity, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut(MenuItem item) {
        ((TagScreen) getApplication()).setFiestaId(null);
        mAuth.signOut();
        Intent intent = new Intent(activity, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void requestPermission(final Uri downloadUrl, Uri videoUri, ByteArrayOutputStream baos, StorageReference storageRef, String fiestaIdFinal, final String keyVideos, final DatabaseReference myRef) {

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            Toast.makeText(activity, "Se necesitan permisos para continuar", Toast.LENGTH_LONG).show();
            requestPermission(downloadUrl, videoUri, baos, storageRef, fiestaIdFinal, keyVideos, myRef);

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    uploadVideo(downloadUrl, videoUri, baos, storageRef, fiestaIdFinal, keyVideos, myRef);


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(activity, "Se necesitan permisos para continuar", Toast.LENGTH_LONG).show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", photoURI);
        outState.putString("file_path", mCurrentPhotoPath);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        photoURI = savedInstanceState.getParcelable("file_uri");
        mCurrentPhotoPath = savedInstanceState.getString("file_path");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getInfoFiesta(menu);
        return true;
    }

    private void addFiestaInfoMenu(Menu menu) {
        MenuItem item = menu.add("Info");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(activity, InfoActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private void getInfoFiesta(final Menu menu) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("fiestApp").child("fiestas/" + fiestaIdFinal + "/info");
        ValueEventListener postListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InfoFiesta info = dataSnapshot.getValue(InfoFiesta.class);
                if (info!=null && (info.isTieneFoto() || info.getData()!=null)) {
                    addFiestaInfoMenu(menu);
                }
                if(info!=null && info.isChangeTheme()){
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
                if (info.getBackgroundURL() != null)
                    Picasso.with(activity).load(info.getBackgroundURL()).into(backgroundImage);
                if (info.getStatusBarColour() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setNavigationBarColor(Color.parseColor(info.getStatusBarColour()));
                        getWindow().setStatusBarColor(Color.parseColor(info.getStatusBarColour()));
                    }
                }
                if (info.getActionBarColour() != null) {
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(info.getActionBarColour())));
                }
                if (info.getButtonsColour() != null) {
                    cameraButton.setBackgroundColor(Color.parseColor(info.getButtonsColour()));
                    videoButton.setBackgroundColor(Color.parseColor(info.getButtonsColour()));
                    mensajeButton.setBackgroundColor(Color.parseColor(info.getButtonsColour()));
                }
            }
        }catch(Exception e){
            FirebaseCrash.report(e);
        }
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
