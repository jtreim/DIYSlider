package reimschussel.diyslider;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DIYSlider.OnDIYSliderChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DIYSlider slider = new DIYSlider(this, 3, 100, 200, 40, 60);
        slider.setListener(this);

        FrameLayout layout = (FrameLayout) findViewById(R.id.slider_placeholder);
        layout.addView(slider);

        slider.post(new Runnable() {
            @Override
            public void run() {
                slider.setup();
            }
        });
    }

    @Override
    public void onStartObservingTouch(DIYSlider slider, ArrayList<Float> values) {
        TextView sliderValues = findViewById(R.id.thumbValuesText);
        sliderValues.setText("Ready");
    }

    @Override
    public void onValueChanged(DIYSlider slider, ArrayList<Float> values) {
        TextView sliderValues = findViewById(R.id.thumbValuesText);
        StringBuilder text = new StringBuilder();
        for(int i = 0; i < values.size(); i++){
            text.append("Thumb " + Integer.toString(i+1) + ": ");
            text.append(values.get(i));
            text.append("\n");
        }

        sliderValues.setText(text);
    }

    @Override
    public void onStopObservingTouch(DIYSlider slider, ArrayList<Float> values) {

    }
}
