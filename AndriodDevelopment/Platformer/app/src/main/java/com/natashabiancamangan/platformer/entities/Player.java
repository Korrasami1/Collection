package com.natashabiancamangan.platformer.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.natashabiancamangan.platformer.AnimationController;
import com.natashabiancamangan.platformer.Audio.Jukebox;
import com.natashabiancamangan.platformer.utilities.Config;
import com.natashabiancamangan.platformer.Inputs.InputManager;
import com.natashabiancamangan.platformer.levels.LevelData;

public class Player extends DynamicEntity {
    static final String TAG = "Player";
    private final int LEFT = 1;
    private final int RIGHT = -1;
    private int _facing = RIGHT;
    static final float MIN_INPUT_TO_TURN = 0.05f; //5% joystick input
    private boolean _isRecovering = false;
    private boolean _isPlayingRecoveryAnimation = false;
    private float _recoveryTime = 0.0f;
    private float _recoveryAnimationTime = 0.0f;
    private boolean _isInvincible = false;
    public int _health;
    Jukebox _jukebox = null;
    public AnimationController _animController = null;

    public Player(final String spriteName, final int xpos, final int ypos, final Jukebox jukebox){
        super(spriteName, xpos, ypos);
        _health = Config.PLAYER_START_HEALTH;
        _recoveryTime = Config.RECOVERY_TIME;
        _recoveryAnimationTime = Config.RECOVERY_ANIMATION_FRAME_RATE;
        _jukebox = jukebox;
        _animController = new AnimationController(LevelData.PLAYER_WALK_SPRITES, Config.PLAYER_ANIM_FRAME_HEIGHT,
                Config.PLAYER_ANIM_FRAME_WIDTH, LevelData.PLAYER_WALK_SPRITES.length);
        loadBitmap(spriteName, xpos, ypos);
    }

    @Override
    public void render(Canvas canvas, Matrix transform, Paint paint) {
        transform.preScale(_facing, 1.0f);
        if(_facing == RIGHT){
            final float offset = _game.worldToScreenX(_width);
            transform.postTranslate(offset, 0);
        }
        if(!_isPlayingRecoveryAnimation){
            loadBitmap(_animController._spriteName, (int) _x, (int) _y);
            super.render(canvas, transform, paint);
        }

    }

    @Override
    public void update(final double dt) {
        final InputManager controls = _game.getControls();
        final float direction = controls._horizontalFactor;
        _velX = direction * Config.PLAYER_RUN_SPEED;
        updateFacingDirection(direction);
        _animController.getCurrentFrame(dt, _velX);
        if(controls._isJumping && _isOnGround){
            _velY = Config.PLAYER_JUMP_FORCE;
            _isOnGround = false;
        }
        recoverFromHit(dt);
        super.update(dt);
    }

    public void setBounds(final RectF worldEdges){
        if(_x < worldEdges.left){
            _x = worldEdges.left;
        }else if(_x > worldEdges.right-Config.WORLD_EDGE_OFFSET){
            _x = worldEdges.right-Config.WORLD_EDGE_OFFSET;
        }
    }

   @Override
    public void onCollision(Entity that) {
        super.onCollision(that);
        if(that.getClass() == Enemy.class && !_isInvincible){
            checkRecoveryCountDown();
        }
    }

    private void updateFacingDirection(final float controlDirection){
        if(Math.abs(controlDirection) < MIN_INPUT_TO_TURN){return;}
        if(controlDirection < 0){_facing = RIGHT;}
        else if(controlDirection > 0){_facing = LEFT;}
    }

    void checkRecoveryCountDown(){
        if(!_isRecovering){
            _health--;
            _jukebox.play(Jukebox.HIT, Config.SOUND_LOOP_NORMAL);
            _isRecovering = true;
        }
    }
    void recoverFromHit(final double dt){
        if(_isRecovering){
            _recoveryTime -= dt;
            executeRecoveryAnimation(dt);
            if(_recoveryTime < 0){
                _isRecovering = false;
                _isPlayingRecoveryAnimation = false;
                _recoveryTime = Config.RECOVERY_TIME;
            }
        }
    }

    void executeRecoveryAnimation(final double dt){
        _recoveryAnimationTime -= dt;
        if(_recoveryAnimationTime < 0){
            _isPlayingRecoveryAnimation = false;
            _recoveryAnimationTime = Config.RECOVERY_ANIMATION_FRAME_RATE;
        }else {
            _isPlayingRecoveryAnimation = true;
        }
    }

    public void setIsInvincible(final boolean isInvincible){
        _isInvincible = isInvincible;
    }
    public void changeSprites(final String[] spriteNames){
        _animController.setSprites(spriteNames);
    }
}
