package com.example.lostandfound;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for all image operations:
 *  - Picking from gallery
 *  - Compressing & resizing
 *  - Fixing rotation (EXIF)
 *  - Converting Bitmap ↔ byte[] for SQLite BLOB storage
 */
public class ImageHelper {

    // Max dimensions for stored images (keeps DB size reasonable)
    private static final int MAX_WIDTH   = 800;
    private static final int MAX_HEIGHT  = 800;
    private static final int JPEG_QUALITY = 80;  // 0–100

    // ══════════════════════════════════════════════════════════════
    //  Uri → Bitmap  (handles rotation via EXIF)
    // ══════════════════════════════════════════════════════════════
    public static Bitmap uriToBitmap(Context context, Uri uri) throws IOException {
        Bitmap bitmap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28+ — use ImageDecoder (handles orientation automatically)
            android.graphics.ImageDecoder.Source source =
                    android.graphics.ImageDecoder.createSource(context.getContentResolver(), uri);
            bitmap = android.graphics.ImageDecoder.decodeBitmap(source);
        } else {
            // API < 28 — use MediaStore, then fix rotation manually
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            bitmap = fixRotation(context, uri, bitmap);
        }

        return scaleBitmap(bitmap);
    }

    // ══════════════════════════════════════════════════════════════
    //  Fix EXIF rotation (phones often save sideways)
    // ══════════════════════════════════════════════════════════════
    private static Bitmap fixRotation(Context context, Uri uri, Bitmap bitmap)
            throws IOException {

        InputStream input = context.getContentResolver().openInputStream(uri);
        if (input == null) return bitmap;

        ExifInterface exif = new ExifInterface(input);
        int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        );
        input.close();

        int degrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:  degrees = 90;  break;
            case ExifInterface.ORIENTATION_ROTATE_180: degrees = 180; break;
            case ExifInterface.ORIENTATION_ROTATE_270: degrees = 270; break;
        }

        if (degrees == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotated = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotated;
    }

    // ══════════════════════════════════════════════════════════════
    //  Scale down to MAX_WIDTH × MAX_HEIGHT while keeping aspect ratio
    // ══════════════════════════════════════════════════════════════
    private static Bitmap scaleBitmap(Bitmap original) {
        int width  = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) return original;

        float scaleX = (float) MAX_WIDTH  / width;
        float scaleY = (float) MAX_HEIGHT / height;
        float scale  = Math.min(scaleX, scaleY);   // maintain aspect ratio

        int newWidth  = Math.round(width  * scale);
        int newHeight = Math.round(height * scale);

        Bitmap scaled = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
        if (scaled != original) original.recycle();
        return scaled;
    }

    // ══════════════════════════════════════════════════════════════
    //  Bitmap → byte[]  for SQLite BLOB storage
    // ══════════════════════════════════════════════════════════════
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream);
        return stream.toByteArray();
    }

    // ══════════════════════════════════════════════════════════════
    //  byte[] → Bitmap  for display (loaded from SQLite BLOB)
    // ══════════════════════════════════════════════════════════════
    public static Bitmap bytesToBitmap(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // ══════════════════════════════════════════════════════════════
    //  Decode a large image safely without OutOfMemoryError
    //  (uses inSampleSize to sub-sample before loading into RAM)
    // ══════════════════════════════════════════════════════════════
    public static Bitmap decodeSampledBitmap(byte[] bytes, int reqWidth, int reqHeight) {
        // First pass: get dimensions only (no memory allocation)
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Second pass: decode with inSampleSize
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width  = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth  = width  / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth  / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}