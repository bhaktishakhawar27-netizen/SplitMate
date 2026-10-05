package com.example.splitmate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** Saves / loads the profile photo in app-private storage (no permissions needed). */
public final class ProfileImageHelper {

    private ProfileImageHelper() {}

    /** Copies + downsizes the picked image. Returns the saved file path or null. */
    public static String saveFromUri(Context ctx, Uri uri, String email) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }

            int sample = 1;
            while (bounds.outWidth / sample > 800 || bounds.outHeight / sample > 800) {
                sample *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            Bitmap bmp;
            try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                bmp = BitmapFactory.decodeStream(in, null, opts);
            }
            if (bmp == null) return null;

            File file = new File(ctx.getFilesDir(),
                    "profile_" + Math.abs(email.toLowerCase().hashCode())
                            + "_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 88, out);
            }
            bmp.recycle();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    public static void delete(String path) {
        if (path == null) return;
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(path).delete();
        } catch (Exception ignored) {
        }
    }

    /** Shows photo in imageView if it exists, otherwise shows the fallback view. */
    public static void show(String path, ImageView imageView, View fallback) {
        Bitmap bmp = null;
        if (path != null && new File(path).exists()) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 2;
            bmp = BitmapFactory.decodeFile(path, o);
        }
        if (bmp != null) {
            imageView.setImageBitmap(bmp);
            imageView.setVisibility(View.VISIBLE);
            if (fallback != null) fallback.setVisibility(View.INVISIBLE);
        } else {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
            if (fallback != null) fallback.setVisibility(View.VISIBLE);
        }
    }
}
