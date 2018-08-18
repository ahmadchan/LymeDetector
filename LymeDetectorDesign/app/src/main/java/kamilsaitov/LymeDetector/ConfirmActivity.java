package kamilsaitov.LymeDetector;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * A simple activity which shows the picture chosen by user, confirms the choice and proceeds to the next activity.
 */
public class ConfirmActivity extends AppCompatActivity {

    Uri uri;
    ImageView imageView;
    Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        confirmButton = (Button) findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProcessingActivity();
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);

        // get the picture from previous activity
        Intent intent = getIntent();
        String imagePath= intent.getStringExtra("imagePath");
        uri = Uri.parse(imagePath);
        imageView.setImageURI(uri);
    }

    /**
     * Goes to the next activity
     */
    void goToProcessingActivity(){
        Intent i = new Intent(this, ProcessingActivity.class);
        i.putExtra("finalImage", uri.toString());
        startActivity(i);
    }
}
