package kamilsaitov.LymeDetector;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

/**
 * The main purpose of this activity is to display the result of classification.
 */
public class ResultActivity extends AppCompatActivity {

    RingProgressBar ringProgressBar; // fancy half-ring progress bar
    TextView textView; // result textView itself

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // get the image fgrom previous activity
        Intent intent = getIntent();
        float prediction = intent.getFloatExtra("classResult", -1)*100; //*100 to get percentage

        ringProgressBar = (RingProgressBar) findViewById(R.id.result_bar);

        ringProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                Toast.makeText(ResultActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });

        // start animation of progress bar
        textView = (TextView) findViewById(R.id.resultView);
        startCountAnimation(prediction);

    }


    /**
     * Method to start the animated visualization of the result
     * @param prediction the final number, where the progress bar should stop.
     */
    private void startCountAnimation(float prediction) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, prediction);
        animator.setDuration(1200); // duration of the animation
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText((animation.getAnimatedValue().toString()+"000000").substring(0, 5)+"%");
                ringProgressBar.setProgress(Math.round(Float.parseFloat(animation.getAnimatedValue().toString())/2)); // /2 because it's a half of the ring.
            }
        });
        animator.start();
    }


    /**
     * If "back" is pressed then go to launchActivity.
     */
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ResultActivity.this, LaunchActivity.class));
        finish();

    }
}
