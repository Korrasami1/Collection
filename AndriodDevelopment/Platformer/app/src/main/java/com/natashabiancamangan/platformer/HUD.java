package com.natashabiancamangan.platformer;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import com.natashabiancamangan.platformer.utilities.Config;

public class HUD extends SurfaceView{
    ImageView _health = null;
    TextView _collectiblesTxt = null;
    ImageView _gameLevelLoadView = null;
    MainActivity _activity = null;
    public AnimationDrawable _levelAnimation = null;

    public HUD(final Context context){
        super(context);
        _activity = (MainActivity) getContext();
    }

    public void setHealthAndCollectibleIndicators(){
        _health = (ImageView) _activity.findViewById(R.id.healthView);
        _collectiblesTxt = (TextView) _activity.findViewById(R.id.collectiblesView);
        _gameLevelLoadView = (ImageView) _activity.findViewById(R.id.levelAnim);
        _gameLevelLoadView.setBackgroundResource(R.drawable.walk_animation); //basically setting a temp value
        _levelAnimation = (AnimationDrawable) _gameLevelLoadView.getBackground();
    }

    public void updateHealth(final int currentHealth){
        if(_health == null){
           throw new AssertionError();
        }
        final int healthValueFull = Config.PLAYER_START_HEALTH;
        final int healthValueHalf = Config.PLAYER_START_HEALTH/2;

        if(currentHealth == healthValueFull){
            _health.setImageResource(R.drawable.life_health_full);
        }else if(currentHealth == healthValueHalf){
            _health.setImageResource(R.drawable.life_health_half);
        }else {
            _health.setImageResource(R.drawable.life_health_empty);
        }
    }

    public void updateCollectibles(int totalCollectibles, int totalCollectiblesLeft){
        if(_collectiblesTxt == null){
            throw new AssertionError();
        }
        try {
            _collectiblesTxt.setText(getContext().getString(R.string.collectibles_txt) + totalCollectiblesLeft + getContext().getString(R.string.divider) + totalCollectibles);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void playLevelAnimation(String gameStatus){
            if(!_levelAnimation.isRunning()){
                if(gameStatus == "GameOver"){
                    _gameLevelLoadView.setBackgroundResource(R.drawable.gameover_anim);
                    _levelAnimation = (AnimationDrawable) _gameLevelLoadView.getBackground();
                }else if (gameStatus == "LevelCleared"){
                    _gameLevelLoadView.setBackgroundResource(R.drawable.levelcleared_anim);
                    _levelAnimation = (AnimationDrawable) _gameLevelLoadView.getBackground();
                }
                _levelAnimation.setOneShot(true);
                _levelAnimation.setVisible(true, true);
                _levelAnimation.start();
            }

    }
}
