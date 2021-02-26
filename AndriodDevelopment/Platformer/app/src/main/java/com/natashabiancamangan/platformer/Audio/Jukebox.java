package com.natashabiancamangan.platformer.Audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;

import com.natashabiancamangan.platformer.utilities.Config;

import java.io.IOException;

public class Jukebox {
    SoundPool _soundPool = null;
    MediaPlayer _bgPlayer = null;
    Context _context = null;
    private static final int MAX_STREAMS = 3;
    public static int HIT = 0;
    public static int GAMEOVER = 1;
    public static int GAMESTART = 2;
    public static int GAMEWIN = 3;
    public static int COIN = 4;
    public static int POWERUP = 5;
    int _streamID = 0;
    SharedPreferences _prefs;
    SharedPreferences.Editor _editor;
    private static final String SOUNDS_PREF_KEY = "sounds_pref_key";
    private static final String MUSIC_PREF_KEY = "music_pref_key";
    private boolean _soundEnabled;
    private boolean _musicEnabled;
    public static final String MUSIC_ID = "music_id";
    private String _musicID;

    public Jukebox(final Context context){
        _context = context;
        createSoundPool();
        _prefs = PreferenceManager.getDefaultSharedPreferences(context);
        _editor = _prefs.edit();
        _soundEnabled = _prefs.getBoolean(SOUNDS_PREF_KEY, true);
        _musicEnabled = _prefs.getBoolean(MUSIC_PREF_KEY, true);
        if(_prefs.contains(MUSIC_ID)){
            _musicID = _prefs.getString(MUSIC_ID, "");
        }else {
            _editor.putString(MUSIC_ID, "platformer1.mp3");
            _editor.apply();
        }
        loadIfNeeded(_context);
    }

    private void loadIfNeeded(final Context context){
        if(_soundEnabled){
            loadSounds(context);
        }
        if(_musicEnabled){
            loadMusic(context);
        }
    }

    @SuppressWarnings("depreciation")
    private void createSoundPool(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            _soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
        else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            _soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(MAX_STREAMS)
                    .build();
        }
    }

    private void loadSounds(final Context context){
        try{
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd(String.format("hit.wav"));
            HIT = _soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd(String.format("gameover.wav"));
            GAMEOVER = _soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd(String.format("gamestart.wav"));
            GAMESTART = _soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd(String.format("gamewin.wav"));
            GAMEWIN = _soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd(String.format("coin.wav"));
            COIN = _soundPool.load(descriptor, 1);
            descriptor = assetManager.openFd(String.format("powerup.wav"));
            POWERUP = _soundPool.load(descriptor, 1);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public int play(final int soundID, final int loop){
        if(!_soundEnabled){return _streamID;}
        final float leftVolume = 1f;
        final float rightVolume = 1f;
        final int priority = 1;
        final float rate = 1.0f;
        if(soundID > 0){
            _streamID = _soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
        return _streamID;
    }

    public void toggleSoundStatus(){
        _soundEnabled = !_soundEnabled;
        if(_soundEnabled){
            loadSounds(_context);
        }else{
            unloadSounds();
        }
        PreferenceManager
                .getDefaultSharedPreferences(_context)
                .edit()
                .putBoolean(SOUNDS_PREF_KEY, _soundEnabled)
                .commit();
    }

    public void stopMusic(){_bgPlayer.stop();}
    private void unloadSounds(){
        if(_soundPool != null){
            _soundPool.release();
            _soundPool = null;
        }
    }
    public void setMusic(final String musicIDTxt){
        _editor.putString(MUSIC_ID, musicIDTxt);
        _editor.apply();
        _musicID = _prefs.getString(MUSIC_ID, "");
    }

    public void restartMusic(){
        _bgPlayer.seekTo(0);
        resumeBgMusic();
    }

    private void loadMusic(final Context context){
        try{
            _bgPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context
                    .getAssets().openFd(_musicID);
            _bgPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            _bgPlayer.setLooping(true);
            _bgPlayer.setVolume(Config.DEFAULT_MUSIC_VOLUME, Config.DEFAULT_MUSIC_VOLUME);
            _bgPlayer.prepare();
        }catch(IOException e){
            _bgPlayer = null;
            _musicEnabled = false;
            //Log.e(e);
        }
    }

    public void toggleMusicStatus(){
        _musicEnabled = !_musicEnabled;
        if(_musicEnabled){
            loadMusic(_context);
        }else{
            unloadMusic();
        }
        PreferenceManager
                .getDefaultSharedPreferences(_context)
                .edit()
                .putBoolean(MUSIC_PREF_KEY, _soundEnabled)
                .commit();
    }

    private void unloadMusic(){
        if(_bgPlayer != null) {
            _bgPlayer.stop();
            _bgPlayer.release();
        }
    }

    public void pauseBgMusic(){
        if(!_musicEnabled){return;}
            _bgPlayer.pause();
    }
    public void resumeBgMusic(){
        if(!_musicEnabled){return;}
            _bgPlayer.start();
    }

    public void destroy(){
        unloadSounds();
        unloadMusic();
    }
}