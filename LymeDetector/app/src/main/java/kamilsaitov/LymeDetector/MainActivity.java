package kamilsaitov.LymeDetector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import kamilsaitov.LymeDetector.models.Classification;
import kamilsaitov.LymeDetector.models.Classifier;
import kamilsaitov.LymeDetector.models.TensorFlowClassifier;

public class MainActivity extends AppCompatActivity implements IPickResult, View.OnClickListener {

    ImageView img;
    Button resetButton;
    Button classifyButton;
    TextView resText;
    boolean imageOn;

    Bitmap bitmap;
    private Classifier classifier;

    private static final int PIXEL_WIDTH = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageOn = false;
        resText = (TextView) findViewById(R.id.tfRes);

        loadModel();

        img = (ImageView) findViewById(R.id.img);
        img.setImageResource(R.drawable.camera);
        img.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PickImageDialog.build(new PickSetup()).show(MainActivity.this);
            }
        });

        resetButton = (Button) findViewById(R.id.btn_clear);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                img.setImageResource(R.drawable.camera);
                imageOn = false;
                resText.setText("");
            }
        });

        classifyButton = (Button) findViewById(R.id.btn_class);
        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!imageOn) {
                    Toast.makeText(MainActivity.this, "Image is not selected", Toast.LENGTH_LONG).show();
                    return;
                }
                classify();
            }
        });
    }

    public void onClick(View view) {
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            //img.setImageBitmap(r.getBitmap());

            //or
            bitmap = r.getBitmap();
            img.setImageURI(r.getUri());
            imageOn = true;
        } else {
            Toast.makeText(MainActivity.this, "A problem occurred during loading the image", Toast.LENGTH_LONG).show();
        }
    }

    private void loadModel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    classifier = TensorFlowClassifier.create(getAssets(), "Keras",
                            "model_multi.pb", "labels.txt", PIXEL_WIDTH,
                            "input_1", "output_node0", false);
                } catch (final Exception e) {
                    //if classifier isn't found, throw an error!
                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();
    }

    private void classify() {
        float pixels[] = getPixelData();

        //init an empty string to fill with the classification output
        String text = "";
        //perform classification on the image
        final Classification res = classifier.recognize(pixels);
        //if it can't classify
        if (res.getLabel() == null) {
            text += classifier.name() + ": Cannot classify\n";
        } else {
            switch(res.getLabel()){
                case "Not-EM":
                    text+="Not Lyme";
                    break;
                case "50-70":
                    text+="Confidence of lyme: "+(60+res.getConf())+"%";
                    break;
                case "70-80":
                    text+="Confidence of lyme: "+(75+res.getConf())+"%";
                    break;
                case "80-90":
                    text+="Confidence of lyme: "+(85+res.getConf())+"%";
                    break;
                case "90-100":
                    text+="Confidence of lyme: "+(90+res.getConf())+"%";
                    break;
                default:
                    text+="Cannot classify";
                        break;
            }
            //else output its name
//            text += String.format("%s: %s, %f\n", classifier.name(), res.getLabel(),
//                    res.getConf());
        }

        resText.setText(text);
    }

    private float[] getPixelData() {

        //Toast.makeText(MainActivity.this, "" + bitmap.getWidth() + "x" + bitmap.getHeight(), Toast.LENGTH_LONG).show();

        //resize to needed dimensions
        bitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);

        img.setImageBitmap(bitmap);

        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Get 250x250 pixel data from bitmap
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);


        float[] floatValues = new float[width * height * 3];
        int[] intValues = new int[width * height];

        bitmap.getPixels(intValues, 0, width, 0, 0, width, height);

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) / 255);
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) / 255);
            floatValues[i * 3 + 2] = ((val & 0xFF) / 255);

            // instead of '/255', '- imageMean) / imageStd' (different for each line) might work better
        }

        return floatValues;
    }

}
