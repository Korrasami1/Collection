package com.natashabiancamangan.platformer;

import android.graphics.Rect;
import android.util.Log;

import com.natashabiancamangan.platformer.utilities.Config;

//http://gamecodeschool.com/android/coding-android-sprite-sheet-animations/
public class AnimationController {
    private static final String TAG = "AnimationController";
    public String _spriteName = null;
    public Rect _sourceRect;
    private int _frameCount;
    private int _currentFrame;
    private float _frameTime = 0;
    public int _frameWidth;
    public int _frameHeight;
    private String [] _sprites = null;


    public AnimationController(final String[] sprites, float frameHeight, float frameWidth, int frameCount) {
        _sprites = sprites;
        _currentFrame = 0;
        _frameCount = frameCount;
        _frameWidth = (int)frameWidth * (int) Config.DEFAULT_DIMENSION;
        _frameHeight = (int)frameHeight * (int) Config.DEFAULT_DIMENSION;
        _sourceRect = new Rect(0, 0, _frameWidth, _frameHeight);
        _frameTime = Config.WALK_CYCLE_FPS;
        _spriteName = _sprites[_currentFrame];
    }

    public void getCurrentFrame(final double dt, float playerWalkSpeed){
        if(playerWalkSpeed != 0) {
            if(playerWalkSpeed < 0){ playerWalkSpeed = -playerWalkSpeed;} //keep the velocity always positive
            _frameTime -= dt * playerWalkSpeed;
            if (_frameTime < 0) {
                _currentFrame++;
                if (_currentFrame >= _frameCount) {
                    _currentFrame = 0;
                }
                _frameTime = Config.WALK_CYCLE_FPS;
            }
        }
        _spriteName = _sprites[_currentFrame];
    }

    public void setSprites(final String[] sprites){
        _sprites = sprites;
    }
}
