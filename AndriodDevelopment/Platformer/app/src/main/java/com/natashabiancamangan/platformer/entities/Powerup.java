package com.natashabiancamangan.platformer.entities;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.natashabiancamangan.platformer.utilities.Config;
import com.natashabiancamangan.platformer.levels.LevelData;
import com.natashabiancamangan.platformer.levels.LevelManager;

public class Powerup extends DynamicEntity {
    private static final String GIVE_LIFE = "giveLife";
    private static final String INVINCIBLE = "invincible";
    private String _powerType = "";
    private float _invincibleTime = 0.0f;
    public boolean _isPowerupActivated = false;
    LevelManager _levelManager = null;
    Player _player = null;

    public Powerup(String spriteName, int xpos, int ypos, final LevelManager levelManager) {
        super(spriteName, xpos, ypos);
        getPowerupType(spriteName);
        _levelManager = levelManager;
        _invincibleTime = Config.INVINCIBLE_MODE_TIME;
        loadBitmap(spriteName, xpos, ypos);
    }

    private String getPowerupType(String spriteName){
        if(spriteName == LevelData.POWERUP_LIFE){
            _powerType = GIVE_LIFE;
        }else if (spriteName == LevelData.POWERUP_STAR){
            _powerType = INVINCIBLE;
        }
        return _powerType;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        checkAndActivateInvincibleMode(dt);
    }

    @Override
    public void render(Canvas canvas, Matrix transform, Paint paint) {
        if(!_isPowerupActivated){
            super.render(canvas, transform, paint);
        }
    }

    @Override
    public void onCollision(Entity that) {
        super.onCollision(that);
        if(that.getClass() == Player.class && !_isPowerupActivated){
            powerUp(that);
        }
    }

    private void powerUp(Entity that){
        _player = (Player) that;
        if(_powerType == GIVE_LIFE){
            _player._health = Config.PLAYER_START_HEALTH;
            _levelManager.removeEntity(this);  //completely remove the powerup
        }else if (_powerType == INVINCIBLE){
            _levelManager._powerups.remove(this); //stop the collision between the player and this powerup
            _levelManager.powerUpCount = _levelManager._powerups.size();
            _isPowerupActivated = true;
            _player.setIsInvincible(_isPowerupActivated);
            _player.changeSprites(LevelData.PLAYER_SHIELD_WALK_SPRITES);
        }
    }

    void checkAndActivateInvincibleMode(final double dt){
        if(_isPowerupActivated){
            _invincibleTime -= dt;
            if(_invincibleTime < 0){
                _isPowerupActivated = false;
                _invincibleTime = Config.INVINCIBLE_MODE_TIME;
                _player.setIsInvincible(_isPowerupActivated);
                _player.changeSprites(LevelData.PLAYER_WALK_SPRITES);
                _levelManager.removeEntity(this); //completely remove the powerup
            }
        }
    }
}
