package com.natashabiancamangan.platformer.entities;

import com.natashabiancamangan.platformer.utilities.Config;
import com.natashabiancamangan.platformer.utilities.Utils;

public class DynamicEntity extends StaticEntity {
    private static final float MAX_DELTA = 0.48f;
    public float _velX = 0;
    public float _velY = 0;
    public float _gravity = Config.GRAVITY;
    boolean _isOnGround = false;

    public DynamicEntity(String spriteName, int xpos, int ypos) {
        super(spriteName, xpos, ypos);
        _width = Config.DEFAULT_DIMENSION;
        _height = Config.DEFAULT_DIMENSION;
    }

    @Override
    public void update(double dt) {
        _x += Utils.clamp((float)(_velX * dt), -MAX_DELTA, +MAX_DELTA);

        if(!_isOnGround){
            final float gravityThisTick = (float) (_gravity* dt);
            _velY += gravityThisTick;
        }
        _y += Utils.clamp((float)(_velY * dt), -MAX_DELTA, +MAX_DELTA);
        if(_y > _game.getWorldHeight()){
            _y = 0f;
        }
        _isOnGround = false;
    }

    @Override
    public void onCollision(Entity that) {
        Entity.getOverlap(this, that, Entity.overlap);
        _x += Entity.overlap.x;
        _y += Entity.overlap.y;
        if(Entity.overlap.y != 0){
            _velY = 0;
            if(Entity.overlap.y < 0f){ //hit our feet
                _isOnGround = true;
            }//if overlap is more than 0 we have hit our head
        }
    }
}
