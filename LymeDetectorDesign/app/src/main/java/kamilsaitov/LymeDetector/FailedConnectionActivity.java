package kamilsaitov.LymeDetector;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Acitivity which launches if the connection is not established sucessfully. Only plays as a handler and does not provide any buttons.
 */
public class FailedConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failed_connection);
    }

    /**
     * If "back" is pressed then go back to launchActivity.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(FailedConnectionActivity.this, LaunchActivity.class));
        finish();
    }
}
