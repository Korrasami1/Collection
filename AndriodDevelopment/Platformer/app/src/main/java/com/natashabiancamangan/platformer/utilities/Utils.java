package com.natashabiancamangan.platformer.utilities;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public abstract class Utils {
    private static final Matrix _matrix = new Matrix();
    private final static java.util.Random RNG = new java.util.Random();

    public static boolean coinFlip(){ return RNG.nextFloat() > 0.5 ; } //random chance
    public static float nextFloat(){ return RNG.nextFloat(); }
    public static int nextInt( final int max){ return RNG.nextInt(max); }
    public static int between( final int min, final int max){ return RNG.nextInt(max-min)+min; }
    public static float between( final float min, final float max){ return min+RNG.nextFloat()*(max-min); }

    public static Bitmap rotateBitmap(final Bitmap source, final float angle){
        _matrix.reset();
        _matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), _matrix, true);
    }

    public static Bitmap flipBitmap(Bitmap src, final boolean horizontal){
        _matrix.reset();
        final int cx = src.getWidth()/2;
        final int cy = src.getHeight()/2;
        if(horizontal){
            _matrix.postScale(1, -1, cx, cy);
        }else{
            _matrix.postScale(-1,1, cx, cy);
        }
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), _matrix, true);
    }

    public static Bitmap scaleToTargetHeight(final Bitmap src, final int targetHeight) {
        float ratio = targetHeight / (float) src.getHeight();
        int newH = (int) (src.getHeight() * ratio);
        int newW = (int) (src.getWidth() * ratio);
        return Bitmap.createScaledBitmap(src, newW, newH, true);
    }

    public static float wrap(float val, final float min, final float max){
        if(val < min){
            val = max;
        }else if(val > max){
            val = min;
        }
        return val;
    }

    public static float clamp(float val, final float min, final float max){
        if(val > max){
            val = max;
        }else if (val < min){
            val = min;
        }
        return val;
    }
}
