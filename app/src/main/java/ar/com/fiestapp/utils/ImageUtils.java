package ar.com.fiestapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

/**
 * Created by luciano.clusa on 2/4/2017.
 */

public class ImageUtils {


    public static int getRotation(Context context, Uri photoURI, String photoFilePath) throws IOException {
        int orientation =-1;
        try {
            String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
            Cursor cur = context.getContentResolver().query(photoURI, orientationColumn, null, null, null);
            if (cur != null && cur.moveToFirst()) {
                orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
            }
        }catch(Exception e){
            Log.e(Constants.TAG, "Error obteniendo orientation de cursor", e);
        }

        if(orientation == -1)
            orientation = ImageUtils.getOrientationFromExif(photoFilePath);


        int rotation=0;

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation =180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation =270;
                break;

            case ExifInterface.ORIENTATION_NORMAL:

            default:
                break;
        }

        return rotation;
    }


    public static int getOrientationFromExif(String photoFilePath) throws IOException {
        ExifInterface ei = new ExifInterface(photoFilePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        return orientation;
    }
}
