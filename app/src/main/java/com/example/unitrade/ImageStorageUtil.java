package com.example.unitrade;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageStorageUtil {

    public static Uri copyFromUri(Context context, Uri sourceUri) {
        try {
            InputStream in = context.getContentResolver().openInputStream(sourceUri);

            File dir = new File(context.getFilesDir(), "profile_images");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "profile_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.close();

            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Uri saveBitmap(Context context, Bitmap bitmap) {
        try {
            File dir = new File(context.getFilesDir(), "profile_images");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "profile_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
