package reimschussel.diyslider;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DIYSlider slider = new DIYSlider(this);

        FrameLayout layout = (FrameLayout) findViewById(R.id.slider_placeholder);
        layout.addView(slider);
    }
}
