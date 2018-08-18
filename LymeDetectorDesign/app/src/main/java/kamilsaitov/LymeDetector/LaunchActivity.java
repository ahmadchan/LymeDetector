package kamilsaitov.LymeDetector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.sax.StartElementListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

/**
 * MainActivity of the app. Opens on launch. Basically it's an introduction screen with only one button (which launches image picker).
 */
public class LaunchActivity extends AppCompatActivity implements IPickResult {

    Button chooseImage;
    Bitmap bitmap;
    Uri uri;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        chooseImage = (Button) findViewById(R.id.choose_button);
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickImageDialog.build(new PickSetup()).show(LaunchActivity.this); // rises the image picker, which allows to choose whether to use the image
            }                                                                        // from gallery or take a photo.
        });
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            uri = r.getUri();
            bitmap = r.getBitmap();
            imagePath = r.getPath();
            goToConfirmActivity();
        } else {
            Toast.makeText(LaunchActivity.this, "A problem occurred during loading the image: " + r.getError(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Goes to the next activity
     */
    void goToConfirmActivity(){
        Intent i = new Intent(this, ConfirmActivity.class);
        i.putExtra("imagePath", uri.toString());
        startActivity(i);
    }


}
