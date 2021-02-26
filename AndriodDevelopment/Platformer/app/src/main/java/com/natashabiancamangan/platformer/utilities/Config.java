package com.natashabiancamangan.platformer.utilities;

public abstract class Config {
    public static int STAGE_WIDTH = 1280;
    public static int STAGE_HEIGHT = 720;
    public static final float GRAVITY = 40f;
    public static final float PLAYER_RUN_SPEED = 6.0f; //meter per second
    public static final float PLAYER_JUMP_FORCE = -(GRAVITY/2); //meter per second
    public static final float DEFAULT_DIMENSION = 1.0f; //meters
    public static final float WORLD_EDGE_OFFSET = 1.0f;

    public static final int PLAYER_START_HEALTH = 2;
    public static float RECOVERY_ANIMATION_FRAME_RATE = 0.05f;
    public static float RECOVERY_TIME = 3f;
    public static float INVINCIBLE_MODE_TIME = 7f;
    public static final float WALK_CYCLE_FPS = 0.5f;
    public static final int SOUND_LOOP_NORMAL = 0;
    public static final int DEFAULT_MUSIC_VOLUME = 4;

    public static final float PLAYER_ANIM_FRAME_HEIGHT = 185;
    public static final float PLAYER_ANIM_FRAME_WIDTH = 218;

    public static final float LEVEL_LOAD_ANIM_TIME = 3f;
}
