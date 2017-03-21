package ar.com.fiestapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.fiestapp.FiestApp;
import ar.com.fiestapp.R;
import ar.com.fiestapp.entities.Imagen;
import ar.com.fiestapp.entities.Video;
import ar.com.fiestapp.utils.Constants;

public class MainActivity extends AppCompatActivity {

    private static final int FOTO = 0;
    private static final int VIDEO = 1;
    private static final int FIRMA = 2;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        mAuth = FirebaseAuth.getInstance();

        View cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.v(Constants.TAG, "Can't create file to take picture!");
                        Toast.makeText(activity, "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT);
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

            }
        });
        View videoButton = findViewById(R.id.videoButton);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
                startActivityForResult(cameraIntent,VIDEO);
            }
        });
        View mensajeButton = findViewById(R.id.mensajeButton);
        mensajeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MensajeActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
            try {

                String fiestaIdAux = ((FiestApp)getApplication()).getFiestaId();

                if(fiestaIdAux==null) {
                    SharedPreferences sharedPref = getSharedPreferences("FiestApp", Context.MODE_PRIVATE);
                    fiestaIdAux = sharedPref.getString("fiestaId", null);
                }

                final String fiestaIdFinal = fiestaIdAux.toLowerCase();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                final StorageReference storageRef = storage.getReferenceFromUrl(Constants.DATA_STORE_URL);
                StorageReference fileRef;
                final FirebaseDatabase database = FirebaseDatabase.getInstance();


                final DatabaseReference myRef = database.getReference("fiestApp").child("fiestas/"+fiestaIdFinal);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final UploadTask uploadTask;

                switch (requestCode) {
                    case FOTO:
                        Toast.makeText(activity, R.string.foto_subiendo, Toast.LENGTH_LONG).show();

                        final String keyImagenes = myRef.child(fiestaIdFinal).child("imagenes").push().getKey();
                        fileRef = storageRef.child(fiestaIdFinal + "/imagenes/" + keyImagenes + ".jpg");

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),photoURI);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] bites = baos.toByteArray();

                        uploadTask = fileRef.putBytes(bites);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Imagen imagen = new Imagen();
                                imagen.setTime(new Date().getTime());
                                imagen.setId(keyImagenes);
                                imagen.setUrl(downloadUrl.toString());
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/imagenes/" + keyImagenes, imagen);

                                myRef.updateChildren(childUpdates);
                                Toast.makeText(activity, R.string.foto_enviada, Toast.LENGTH_LONG).show();

                            }
                        });

                        break;
                    case VIDEO:
                        Toast.makeText(activity, R.string.video_subiendo, Toast.LENGTH_LONG).show();

                        final String keyVideos = myRef.child(fiestaIdFinal).child("videos").push().getKey();
                        final Uri videoUri = intent.getData();

                        StorageReference videoRef = storageRef.child(fiestaIdFinal + "/videos/"+videoUri.getLastPathSegment() + ".mp4");
                        uploadTask = videoRef.putFile(videoUri);
                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
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
                                    activity.downloadUrl=downloadUrl;
                                    activity.videoUri=videoUri;
                                    activity.baos=baos;
                                    activity.storageRef=storageRef;
                                    activity.fiestaIdFinal=fiestaIdFinal;
                                    activity.keyVideos=keyVideos;
                                    activity.myRef=myRef;
                                }else {
                                    uploadVideo(downloadUrl, videoUri, baos, storageRef, fiestaIdFinal, keyVideos, myRef);
                                }
                            }
                        });



                        break;

                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                Log.e(Constants.TAG, getResources().getString(R.string.upload_failed), e);

            }

    }


    private void uploadVideo(final Uri downloadUrl, Uri videoUri, ByteArrayOutputStream baos, StorageReference storageRef, String fiestaIdFinal, final String keyVideos, final DatabaseReference myRef) {
        String path = getRealPathFromURI(activity, videoUri);

        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND );

        thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bites2 = baos.toByteArray();

        StorageReference thumbRef = storageRef.child(fiestaIdFinal + "/videos/"+videoUri.getLastPathSegment() + ".jpg");
        UploadTask uploadTask2 = thumbRef.putBytes(bites2);

        uploadTask2.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();

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
                Toast.makeText(activity, R.string.video_enviado, Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    public void logOut(MenuItem item){
        ((FiestApp)getApplication()).setFiestaId(null);
        mAuth.signOut();
        Intent intent = new Intent(activity, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
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
                requestPermission(downloadUrl,videoUri,baos,storageRef,fiestaIdFinal,keyVideos,myRef);

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

}
