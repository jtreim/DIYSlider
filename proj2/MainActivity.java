package reimschussel.diyslider;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DIYSlider.OnDIYSliderChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DIYSlider slider = new DIYSlider(this, 2, 10, 50);

        FrameLayout layout = (FrameLayout) findViewById(R.id.slider_placeholder);
        layout.addView(slider);
    }

    @Override
    public void onStartObservingTouch(DIYSlider slider, ArrayList<Integer> values) {

    }

    @Override
    public void onValueChanged(DIYSlider slider, ArrayList<Integer> values) {

    }

    @Override
    public void onStopObservingTouch(DIYSlider slider, ArrayList<Integer> values) {

    }
}
