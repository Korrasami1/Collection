package com.natashabiancamangan.platformer.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.natashabiancamangan.platformer.utilities.Config;

public class StaticEntity extends Entity {
    protected Bitmap _bitmap = null;

    public StaticEntity(final String spriteName, final int xpos, final int ypos){
        _x = xpos;
        _y = ypos;
        _width = Config.DEFAULT_DIMENSION;
        _height = Config.DEFAULT_DIMENSION;
        loadBitmap(spriteName, xpos, ypos);
    }

    protected void loadBitmap(final String spriteName, final int xpos, final int ypos){
         _bitmap = _game._pool.createBitmap(spriteName, _width, _height);
    }

    @Override
    public void render(final Canvas canvas, final Matrix transform, final Paint paint) {
        canvas.drawBitmap(_bitmap, transform, paint);
    }

    @Override
    public void destroy() {}
}
