package ca.poum.btjoystick;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get joystick instance and set callback
        JoystickView jt = findViewById(R.id.joystick);
        jt.setOnMoveListener((angle, power) -> Log.d("BTJ", String.format("angle = %d, power = %f", angle, power)));
    }
}