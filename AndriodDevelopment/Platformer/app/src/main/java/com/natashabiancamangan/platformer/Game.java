package com.natashabiancamangan.platformer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.natashabiancamangan.platformer.Audio.Jukebox;
import com.natashabiancamangan.platformer.Inputs.InputManager;
import com.natashabiancamangan.platformer.entities.Entity;
import com.natashabiancamangan.platformer.levels.LevelManager;
import com.natashabiancamangan.platformer.levels.Level;
import com.natashabiancamangan.platformer.utilities.BitmapPool;
import com.natashabiancamangan.platformer.utilities.Config;

import java.util.ArrayList;

public class Game extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    public static final String TAG = "Game";
    public static final String PREFS = "com.natashabiancamangan.platformer.Preferences";
    public static final String LEVEL_ID = "level_id";
    public static int currentLevelID = 0;
    public static final int FIRST_LEVEL_ID = 0;
    private static final float METERS_TO_SHOW_X = 10f; //set the value you want fixed
    private static final float METERS_TO_SHOW_Y = 0f;  //the other is calculated at runtime!
    private static final int BG_COLOUR = Color.rgb(135, 206, 235);
    private final Matrix _transform = new Matrix();
    private static final Point _position = new Point();
    private float _levelLoadTime = 0;

    private Thread _gameThread;
    private volatile  boolean _isRunning = false;

    private LevelManager _level = null;
    private InputManager _controls = new InputManager();
    private SurfaceHolder _holder;
    private Paint _paint;
    private Canvas _canvas;
    private Viewport _camera = null;
    public final ArrayList<Entity> _visibleEntities = new ArrayList<>();
    public BitmapPool _pool = null;
    private Jukebox _jukebox = null;
    private SharedPreferences _prefs = null;
    private SharedPreferences.Editor _editor = null;
    private HUD _hud = null;
    private MainActivity _activity = null;
    private static final String GAMEOVER = "GameOver";
    private static final String LEVELCLEARED = "LevelCleared";

    public Game(Context context) {
        super(context);
        init();
    }
    public Game(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public Game(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public Game(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        _activity = (MainActivity) getContext();
        currentLevelID = 0; //double sure that currentLevelID has not been set yet
        _levelLoadTime = Config.LEVEL_LOAD_ANIM_TIME;
        final int TARGET_HEIGHT = Config.STAGE_HEIGHT;
        final int actualHeight = getScreenHeight();
        final float ratio = (TARGET_HEIGHT >= actualHeight) ? 1 : (float) TARGET_HEIGHT / actualHeight;
        Config.STAGE_WIDTH = (int) (ratio * getScreenWidth());
        Config.STAGE_HEIGHT = TARGET_HEIGHT;
        _camera = new Viewport(Config.STAGE_WIDTH, Config.STAGE_HEIGHT, METERS_TO_SHOW_X, METERS_TO_SHOW_Y);
        Log.d(TAG, _camera.toString());
        Entity._game = this;
        _pool = new BitmapPool(this);
        _activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        _jukebox = new Jukebox(_activity);
        _prefs = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        _editor = _prefs.edit();
        if(!_prefs.contains(LEVEL_ID)){
            _editor.putInt(LEVEL_ID, FIRST_LEVEL_ID);
            _editor.apply();
        }else {
            currentLevelID = _prefs.getInt(LEVEL_ID, 0);
        }
        _hud = new HUD(getContext());
        _level = new LevelManager(new Level(currentLevelID, getContext()), _pool, _jukebox, _hud);
        _holder = getHolder();
        _holder.addCallback(this);
        _holder.setFixedSize(Config.STAGE_WIDTH, Config.STAGE_HEIGHT);
    }

    public InputManager getControls(){
        return _controls;
    }
    public void setControls(final InputManager controls){
        _controls.onPause();
        _controls.onStop();
        _controls = controls;
    }

    public void setupHUD(){
        _hud.setHealthAndCollectibleIndicators();
        _level.setHUDStartValues();
    }

    public float getWorldHeight(){return _level._levelHeight;}
    public float getWorldWidth(){return _level._levelWidth;}

    public int worldToScreenX(float worldDistance){
        return (int) worldDistance * _camera.getPixelsPerMeterX();
}
    public int worldToScreenY(float worldDistance){
        return (int) worldDistance * _camera.getPixelsPerMeterY();
    }

    public float screenToWorldX(float pixelDistance){
        return pixelDistance / _camera.getPixelsPerMeterX();
    }
    public float screenToWorldY (float pixelDistance){
        return pixelDistance / _camera.getPixelsPerMeterY();
    }

    public static int getScreenHeight(){
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth(){
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
    private static final double NANOS_TO_SECONDS = 1.0 /1000000000;

    @Override
    public void run() {
        long lastFrame = System.nanoTime();
        while (_isRunning) {
            final double deltaTime = (System.nanoTime() - lastFrame) * NANOS_TO_SECONDS;
            lastFrame = System.nanoTime();
            update(deltaTime);
            buildVisibleSet();
            render(_camera, _visibleEntities);
        }
    }

    private  void update(final double dt){
        _camera.lookAt(_level._player);
        _camera.setBounds(_level.getLevelDimensions());
        _level.update(dt);
        if(_level.getGameOver() == true){
            _jukebox.stopMusic();
            _hud.playLevelAnimation(GAMEOVER);
            _levelLoadTime -= dt;
            if(_levelLoadTime < 0){
                restart();
                _levelLoadTime = Config.LEVEL_LOAD_ANIM_TIME;
            }
        }
        if (_level.getGameWin() == true){
            _jukebox.stopMusic();
            _hud.playLevelAnimation(LEVELCLEARED);
            _levelLoadTime -= dt;
            if(_levelLoadTime < 0){
                loadNextLevel();
                _levelLoadTime = Config.LEVEL_LOAD_ANIM_TIME;
            }
        }
    }

    private void buildVisibleSet(){
        _visibleEntities.clear();
        for(final Entity e: _level._entities){
            if(_camera.inView(e)){
                _visibleEntities.add(e);
            }
        }
    }

    private  void render(final Viewport camera, final ArrayList<Entity> visibleEntities){
        if (!lockCanvas()) {return;}
        try{
            _canvas.drawColor(BG_COLOUR); //clear screen
            for(final Entity e: visibleEntities){
                _transform.reset();
                camera.worldToScreen(e, _position);
                _transform.postTranslate(_position.x, _position.y);
                e.render(_canvas, _transform, _paint);
            }
        }finally {
            _holder.unlockCanvasAndPost(_canvas);
        }
    }

    private boolean lockCanvas() {
        if(!_holder.getSurface().isValid()){
            return false;
        }
        _canvas = _holder.lockCanvas();
        return (_canvas != null);
    }

    protected void restart(){
        _hud._levelAnimation.setVisible(false, true);
        init();
        setupHUD();
        _jukebox.restartMusic();
    }

    protected void loadNextLevel(){
        _hud._levelAnimation.setVisible(false, true);
        if(currentLevelID == 0){
            _editor.putInt(LEVEL_ID, currentLevelID+1);
            _editor.apply();
            _jukebox.setMusic("platformer2.wav");
        }else {
            _editor.putInt(LEVEL_ID, FIRST_LEVEL_ID);
            _editor.apply();
            _jukebox.setMusic("platformer1.mp3");
        }
        init();
        setupHUD();
        _jukebox.resumeBgMusic();
    }

    protected void onPause(){
        Log.d(TAG, String.format("OnPause"));
        _isRunning = false;
        _controls.onPause();
        _jukebox.pauseBgMusic();
        while (true){
            try {
                _gameThread.join();
                return;
            } catch (InterruptedException e) {
                Log.d(TAG, Log.getStackTraceString(e.getCause()));
            }
        }

    }

    protected void onResume(){
        Log.d(TAG, String.format("OnResume"));
        _isRunning = true;
        _controls.onResume();
        _jukebox.resumeBgMusic();
        _gameThread = new Thread(this);
    }

    protected void onDestroy(){
        Log.d(TAG, String.format("OnDestroy"));
        _gameThread = null;
        if(_level != null){
            _level.destroy();
            _level = null;
        }
        _controls = null;
        Entity._game = null; //always one of the last elements to be destroyed
        if(_pool != null){
            _pool.empty();
        }
        if(_jukebox != null){
            _jukebox.destroy();
        }
        _holder.removeCallback(this);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        Log.d(TAG, String.format("Surface Created"));
    }

    @Override
    public void surfaceChanged(final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        Log.d(TAG, String.format("Surface Changed!"));
        Log.d(TAG, String.format("\t Width: "+ width+ " Height: "+ height));
        if(_gameThread != null && _isRunning){
            Log.d(TAG, String.format("GameThread Started!"));
            _gameThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
        Log.d(TAG, String.format("Surface Destroyed!"));
    }
}