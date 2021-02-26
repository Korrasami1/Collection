package com.natashabiancamangan.platformer.levels;

public abstract class LevelData {
    public static final String NULLSPRITE = "nullsprite";
    public static final String PLAYER = "red_right1";
    public static final String PLAYER_INVINCIBLE = "red_right_shield1";
    public static final String ENEMY = "spear_up";
    public static final String COIN = "coin_yellow_shade";
    public static final String POWERUP_LIFE = "powerup_life";
    public static final String POWERUP_STAR = "powerup_star";
    public static final int NO_TILE = 0;
    public static String[] PLAYER_WALK_SPRITES = {PLAYER, "red_right2", "red_right3"};
    public static String[] PLAYER_SHIELD_WALK_SPRITES = {PLAYER_INVINCIBLE, "red_right_shield2", "red_right_shield3"};
    int[][] _tiles;
    int _height;
    int _width;

    public int getTile(final int x, final int y){
        return _tiles[y][x];
    }

    int[] getRow(final int y){
        return _tiles[y];
    }

    void updateLevelDimensions(){
        _height = _tiles.length;
        _width = _tiles[0].length;
    }

    abstract public String getSpriteName(final int tileType);

}
