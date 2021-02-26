package com.natashabiancamangan.platformer.levels;

import android.graphics.RectF;

import com.natashabiancamangan.platformer.Audio.Jukebox;
import com.natashabiancamangan.platformer.utilities.Config;
import com.natashabiancamangan.platformer.HUD;
import com.natashabiancamangan.platformer.entities.Coin;
import com.natashabiancamangan.platformer.entities.DynamicEntity;
import com.natashabiancamangan.platformer.entities.Enemy;
import com.natashabiancamangan.platformer.entities.Entity;
import com.natashabiancamangan.platformer.entities.Player;
import com.natashabiancamangan.platformer.entities.Powerup;
import com.natashabiancamangan.platformer.entities.StaticEntity;
import com.natashabiancamangan.platformer.utilities.BitmapPool;

import java.util.ArrayList;

public class LevelManager {
    public int _levelHeight = 0;
    public int _levelWidth = 0;
    public final ArrayList<Entity> _entities = new ArrayList<>();
    private final ArrayList<Entity> _entitiesToAdd = new ArrayList<>();
    private final ArrayList<Entity> _entitiesToRemove = new ArrayList<>();
    public Player _player = null;
    private final ArrayList<Coin> _coins = new ArrayList<>();
    public final ArrayList<Powerup> _powerups = new ArrayList<>();
    private final ArrayList<Enemy> _enemies = new ArrayList<>();
    private final ArrayList<StaticEntity> _tiles = new ArrayList<>();
    private BitmapPool _pool = null;
    private Jukebox _jukebox = null;
    private HUD _hud = null;
    private boolean _gameOver = false;
    private boolean _gameWin = false;
    private int currentCollectibles = 0;
    private int numOfCollectibles = 0;
    private static final int MIN_VALUE = 0;
    public int powerUpCount = 0;

    public LevelManager(final LevelData map,final BitmapPool pool, final Jukebox jukebox, final HUD hud){
        _pool = pool;
        _jukebox = jukebox;
        _hud = hud;
        loadMapAssets(map);
        numOfCollectibles = _coins.size();
        currentCollectibles = 0;
    }

    public void setHUDStartValues(){
         _hud.updateHealth(_player._health);
         _hud.updateCollectibles(numOfCollectibles, currentCollectibles);
    }

    public void update(final double dt){
        if(_gameOver || _gameWin){
            return;
        }
        for(Entity e: _entities){
            e.update(dt);
        }
        _player.setBounds(getLevelDimensions());
        checkCollisionCoins();
        checkCollisionPowerups();
        checkCollisionEnemies();
        checkCollisionDynamicEntities();
        addAndRemoveEntities();
        checkLevelGameOver();
        checkLevelGameWin();
    }

    public RectF getLevelDimensions(){
        RectF worldEdges = new RectF(MIN_VALUE,MIN_VALUE,_levelWidth,_levelHeight);
        return worldEdges;
    }

    private void checkLevelGameOver() {
        if(_player._health < 1){
            _gameOver = true;
            _jukebox.play(Jukebox.GAMEOVER, Config.SOUND_LOOP_NORMAL);
        }
    }

    private void checkLevelGameWin(){
        if(currentCollectibles >= numOfCollectibles){
            _gameWin = true;
            _jukebox.play(Jukebox.GAMEWIN, Config.SOUND_LOOP_NORMAL);
        }
    }

    public boolean getGameOver() {return _gameOver;}
    public boolean getGameWin(){ return _gameWin;}

    private void checkCollisionCoins(){
        final int count = _coins.size();
        Entity a, b;
        a = _player;
        for(int i = 0; i < count ; i++){
            b = _coins.get(i);
            if(a.isColliding(b)){
                a.onCollision(b);
                b.onCollision(a);
                removeEntity(b);
                currentCollectibles++;
                _hud.updateCollectibles(numOfCollectibles, currentCollectibles);
                _jukebox.play(Jukebox.COIN, Config.SOUND_LOOP_NORMAL);
            }
        }
    }
    private void checkCollisionPowerups(){
        powerUpCount = _powerups.size();
        Entity a, b;
        a = _player;
        for(int i = 0; i < powerUpCount; i++){
            b = _powerups.get(i);
            if(a.isColliding(b)){
                a.onCollision(b);
                b.onCollision(a);
                _hud.updateHealth(_player._health);
                _jukebox.play(Jukebox.POWERUP, Config.SOUND_LOOP_NORMAL);
            }
        }
    }

    private void checkCollisionEnemies(){
        final int count = _enemies.size();
        Entity a, b;
        a = _player;
        for(int i = 0; i < count ; i++){
            b = _enemies.get(i);
            if(a.isColliding(b)){
                a.onCollision(b);
                b.onCollision(a);
                _hud.updateHealth(_player._health);
            }
        }
    }

    private void checkCollisionDynamicEntities(){
        Entity a, b;
        ArrayList<DynamicEntity> _dynamicEntities = new ArrayList<>();
        _dynamicEntities.add(_player);
        for(int c = 0; c < _coins.size(); c++){
            _dynamicEntities.add(_coins.get(c));
        }
        for(int p = 0; p < _powerups.size(); p++){
            _dynamicEntities.add(_powerups.get(p));
        }
        for(int e = 0; e < _enemies.size(); e++){
            _dynamicEntities.add(_enemies.get(e));
        }
        final int count = _dynamicEntities.size();
        for(int i = 0; i < count ; i++){
            a = _dynamicEntities.get(i);
            for(int j = i; j < _tiles.size(); j++){
                b = _tiles.get(j);
                if(a.isColliding(b)){
                    a.onCollision(b);
                    b.onCollision(a);
                }
            }
        }
    }

    private void loadMapAssets(final LevelData map){
        cleanUp();
        _levelHeight = map._height;
        _levelWidth = map._width;
        for(int y = 0; y < _levelHeight; y++){
            final int[] row = map.getRow(y);
            for(int x = 0; x < row.length; x++){
                final int tileID = row[x];
                if(tileID == LevelData.NO_TILE){ continue; }
                final String spriteName = map.getSpriteName(tileID);
                createEntity(spriteName, x, y);
            }
        }
    }
    private void createEntity(final String spriteName, final int xpos, final int ypos){
        Entity e = null;
        if(spriteName.equalsIgnoreCase(LevelData.PLAYER)){
            e = new Player(spriteName, xpos, ypos, _jukebox);
            if(_player == null){
                _player = (Player) e;
            }
        }else if(spriteName.equalsIgnoreCase(LevelData.ENEMY)){
            e = new Enemy(spriteName, xpos, ypos);
            _enemies.add((Enemy) e);
        }else if(spriteName.equalsIgnoreCase(LevelData.COIN)){
            e = new Coin(spriteName, xpos, ypos);
            _coins.add((Coin) e);
        }else if(spriteName.equalsIgnoreCase(LevelData.POWERUP_LIFE) || spriteName.equalsIgnoreCase(LevelData.POWERUP_STAR)){
            e = new Powerup(spriteName, xpos, ypos, this);
            _powerups.add((Powerup) e);
        }
        else{
            e = new StaticEntity(spriteName, xpos, ypos);
            _tiles.add((StaticEntity) e);
        }
        addEntity(e);
    }

    private void addAndRemoveEntities(){
        for(Entity e: _entitiesToRemove){
            _entities.remove(e);
            _coins.remove(e);
            _powerups.remove(e);
        }
        for(Entity e: _entitiesToAdd){
            _entities.add(e);
        }
        _entitiesToRemove.clear();
        _entitiesToAdd.clear();
    }

    public void addEntity(final Entity e){
        if(e != null){ _entitiesToAdd.add(e);}
    }
    public void removeEntity(final Entity e){
        if(e != null){ _entitiesToRemove.add(e);}
    }

    private void cleanUp(){
        addAndRemoveEntities();
        for(Entity e: _entities){
            e.destroy();
        }
        _entities.clear();
        _tiles.clear();
        _coins.clear();
        _powerups.clear();
        _enemies.clear();
        _player = null;
        _pool.empty();
    }

    public void destroy(){
        cleanUp();
    }
}
