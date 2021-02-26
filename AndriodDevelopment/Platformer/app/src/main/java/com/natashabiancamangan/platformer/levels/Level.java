package com.natashabiancamangan.platformer.levels;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.natashabiancamangan.platformer.R;

//references: https://stackoverflow.com/questions/11377004/how-to-retrieve-2d-array-from-xml-string-resource-for-android
public class Level extends LevelData {
    private final SparseArray<String> _tileIDToSpriteName = new SparseArray<>();
    private Context _context = null;

    public Level(final int levelID, Context context){
        _context = context;
        _tileIDToSpriteName.put(0, "background");
        _tileIDToSpriteName.put(1, PLAYER);
        _tileIDToSpriteName.put(2, "ground");
        _tileIDToSpriteName.put(3, "ground_left");
        _tileIDToSpriteName.put(4, "ground_right");
        _tileIDToSpriteName.put(5, "ground_round");
        _tileIDToSpriteName.put(6, "mud_left");
        _tileIDToSpriteName.put(7, "mud_right");
        _tileIDToSpriteName.put(8, "mud_square");
        _tileIDToSpriteName.put(9, COIN);
        _tileIDToSpriteName.put(10, ENEMY);
        _tileIDToSpriteName.put(11, POWERUP_LIFE);
        _tileIDToSpriteName.put(12, POWERUP_STAR);
        loadLevel(levelID);
        updateLevelDimensions();
    }

    private void loadLevel(final int levelID){
        if(levelID == 0){
            String[] array = _context.getResources().getStringArray(R.array.level_1_array);
            int rowLength = array[0].split(", ").length;
            _tiles = new int[array.length][rowLength]; //making an initialisation
            for(int i = 0; i < array.length; i++){
                String[] arrayLine = array[i].split(", ");
                for(int j = 0; j < arrayLine.length; j++){
                    try {
                        _tiles[i][j] = Integer.parseInt(arrayLine[j]);
                    }catch(Exception e){
                        e.printStackTrace(); //just in case i entered in the format incorrectly so i know what happened
                    }
                }
            }
        }else if(levelID == 1){
            String[] array = _context.getResources().getStringArray(R.array.level_2_array);
            int rowLength = array[0].split(", ").length;
            _tiles = new int[array.length][rowLength]; //making an initialisation
            for(int i = 0; i < array.length; i++){
                String[] arrayLine = array[i].split(", ");
                for(int j = 0; j < arrayLine.length; j++){
                    try {
                        _tiles[i][j] = Integer.parseInt(arrayLine[j]);
                    }catch(Exception e){
                        e.printStackTrace(); //just in case i entered in the format incorrectly so i know what happened
                    }
                }
            }
        }
    }

    @Override
    public String getSpriteName(int tileType) {
        final String fileName = _tileIDToSpriteName.get(tileType);
        if(fileName != null){
            return fileName;
        }
        return NULLSPRITE;
    }
}
