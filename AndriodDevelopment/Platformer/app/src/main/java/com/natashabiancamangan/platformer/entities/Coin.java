package com.natashabiancamangan.platformer.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.natashabiancamangan.platformer.utilities.Utils;

public class Coin extends DynamicEntity {

    public Coin(String spriteName, int xpos, int ypos) {
        super(spriteName, xpos, ypos);
        loadBitmap(spriteName, xpos, ypos);
    }
    @Override
    public void render(Canvas canvas, Matrix transform, Paint paint) {
        super.render(canvas, transform, paint);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
    }

    @Override
    public void onCollision(Entity that) {
        super.onCollision(that);
    }
}
