package com.natashabiancamangan.platformer.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils" ;
    private static final boolean FILTER = true ;
    private static BitmapFactory.Options _options = new BitmapFactory.Options(); //Q&D pool, assumes single threading
    private static Point _dimensions = new Point( 0 , 0 ); //Q&D  pool, assumes single threading

    private BitmapUtils(){ super ();}

    //Set either of the dimensions for aspect-correct scaling, or both to force the aspect.
    public static Bitmap loadScaledBitmap( final Context context, final String bitmapName,
                                           final int targetWidth, final int targetHeight) throws OutOfMemoryError {
        final Resources res = context.getResources();
        final int resID = res.getIdentifier(bitmapName, "drawable" , context.getPackageName());
        return loadScaledBitmap(res, resID, targetWidth, targetHeight);
    }

    //Set either of the dimensions for aspect-correct scaling, or both to force the aspect.
    private static Bitmap loadScaledBitmap( final Resources res, final int resID,
                                           final int targetWidth, final int targetHeight) throws OutOfMemoryError{
        _options = readBitmapMetaData(res, resID); //parse raw file info into _options
        _dimensions = scaleToTargetDimensions(targetWidth, targetHeight, _options.outWidth, _options.outHeight);
        Bitmap bitmap = loadSubSampledBitmap(res, resID, _dimensions.x, _dimensions.y, _options);
        if (bitmap != null ){
            if (bitmap.getHeight() != _dimensions.y || bitmap.getWidth() != _dimensions.x) {
                //scale to pixel-perfect dimensions in case we have non-uniform density on x / y
                bitmap = Bitmap.createScaledBitmap(bitmap, _dimensions.x, _dimensions.y, FILTER);
            }
        }
        return bitmap;
    }

    private static Bitmap loadSubSampledBitmap( final Resources res, final int resId, final int targetWidth, final int targetHeight, final BitmapFactory.Options opts) {
        opts.inSampleSize = calculateInSampleSize(opts, targetWidth, targetHeight); //calculates clostest POT sample factor
        opts.inJustDecodeBounds = false ;
        opts.inScaled = true ; //scale after subsampling, to reach exact target density.
        if (targetHeight > 0 ) {
            opts.inDensity = opts.outHeight;
            opts.inTargetDensity = targetHeight * opts.inSampleSize;
        } else {
            opts.inDensity = opts.outWidth;
            opts.inTargetDensity = targetWidth * opts.inSampleSize;
        }
        return BitmapFactory.decodeResource(res, resId, opts);
    }

    private static int calculateInSampleSize( final BitmapFactory.Options opts, final int reqWidth, final int reqHeight) {
        final int height = opts.outHeight; // original height and width of image
        final int width = opts.outWidth;
        int inSampleSize = 1 ;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2 ;
            final int halfWidth = width / 2 ;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2 ;
            }
        }
        return inSampleSize;
    }

    //loads metadata about a Bitmap into the Options object. Does *not* load the Bitmap.
    private static BitmapFactory.Options readBitmapMetaData( final Resources res, final int resID){
        _options.inJustDecodeBounds = true ;
        BitmapFactory.decodeResource(res, resID, _options);
        _options.inJustDecodeBounds = false ;
        return _options;
    }

    //provide both or either of targetWidth / targetHeight. If one is left at 0, the other is calculated
    //based on source-dimensions. Ergo; should scale and keep aspect ratio.
    private static Point scaleToTargetDimensions( float targetWidth, float targetHeight,
                                                  final float srcWidth, final float srcHeight){
        if (targetWidth <= 0 && targetHeight <= 0 ){
            targetWidth = srcWidth;
            targetHeight = srcHeight;
        }
        _dimensions.x = ( int ) targetWidth;
        _dimensions.y = ( int ) targetHeight;
        if (targetWidth == 0 || targetHeight == 0 ){
            //formula: new height = (original height / original width) x new width
            if (targetHeight > 0 ) { //if Y is configured, calculate X
                _dimensions.x = ( int ) ((srcWidth / srcHeight) * targetHeight);
            } else { //calculate Y
                _dimensions.y = ( int ) ((srcHeight / srcWidth) * targetWidth);
            }
        }
        return _dimensions;
    }
}
