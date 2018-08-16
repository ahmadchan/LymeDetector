package kamilsaitov.LymeDetector;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class ResultActivity extends AppCompatActivity {

    RingProgressBar ringProgressBar;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        float prediction = intent.getFloatExtra("classResult", -1)*100; //*100 to get percentage
        //System.out.println(predictionString==null);
        //float prediction = Float.parseFloat(predictionString);

        ringProgressBar = (RingProgressBar) findViewById(R.id.result_bar);

        ringProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                Toast.makeText(ResultActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });

        textView = (TextView) findViewById(R.id.resultView);
        startCountAnimation(prediction);

    }


    private void startCountAnimation(float prediction) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, prediction);
        animator.setDuration(1200);
        String lastCount;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText((animation.getAnimatedValue().toString()+"000000").substring(0, 5)+"%");
                ringProgressBar.setProgress(Math.round(Float.parseFloat(animation.getAnimatedValue().toString())/2));
            }
        });
        animator.start();
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ResultActivity.this, LaunchActivity.class));
        finish();

    }
}
