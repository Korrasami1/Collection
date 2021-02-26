package com.natashabiancamangan.platformer.Inputs;

import android.view.MotionEvent;
import android.view.View;

import com.natashabiancamangan.platformer.R;

public class TouchController extends InputManager implements View.OnTouchListener {

    public TouchController(View view){
        view.findViewById(R.id.keypad_up)
                .setOnTouchListener(this);
        view.findViewById(R.id.keypad_down)
                .setOnTouchListener(this);
        view.findViewById(R.id.keypad_left)
                .setOnTouchListener(this);
        view.findViewById(R.id.keypad_right)
                .setOnTouchListener(this);
        view.findViewById(R.id.keypad_jump)
                .setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        int action = event.getActionMasked();
        int id = v.getId();
        if(action == MotionEvent.ACTION_DOWN){
            // User started pressing a key
            if(id == R.id.keypad_up){
                _verticalFactor -= 1;
            }else if (id == R.id.keypad_down) {
                _verticalFactor += 1;
            }
            if (id == R.id.keypad_left) {
                _horizontalFactor -= 1;
            } else if(id == R.id.keypad_right) {
                _horizontalFactor += 1;
            }
            if (id == R.id.keypad_jump) {
                _isJumping = true;
            }
        } else if(action == MotionEvent.ACTION_UP) {
            // User released a key
            if (id == R.id.keypad_up) {
                _verticalFactor += 1;
            } else if (id == R.id.keypad_down) {
                _verticalFactor -= 1;
            }
            if (id == R.id.keypad_left) {
                _horizontalFactor += 1;
            } else if (id == R.id.keypad_right) {
                _horizontalFactor -= 1;
            }
            if (id == R.id.keypad_jump) {
                _isJumping = false;
            }
        }
        return false;
    }
}
