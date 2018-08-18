package kamilsaitov.LymeDetector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

/**
 * The main class for the app. It does 90% of the job:
 * fetches the image from the previous activity;
 * converts to Base64 and then to JSON format;
 * sends the JSON to the server;
 * receives a JSON, decodes it into the prediction string;
 * sends the result to the next activity;
 * shows the progress;
 */
public class ProcessingActivity extends AppCompatActivity {

    final int PIXEL_WIDTH = 600; // image size of the neural network (used to convert the image to that resolution)
    final String URL_ADDRESS = "http://138.68.81.220:5000/predict"; // change this when you deploy the app to your server. Do not change the "/predict".

    Uri uri;
    int progress = 0; // variable to show the progress.

    String result; // here will be the result which will be passed to the resultActivity. Only use this in the sendPost thread or will be unstable (because of parallelism)


    TextView testTextView; // to show the progress stage in the center of the progress ring.

    RingProgressBar ringProgressBar;
    // the handler receives signals from the main thread and shows the stages of the progress as it happens.
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0 || msg.what == 1 || msg.what == 2 || msg.what == 3 || msg.what == 4) {
                if (progress < 100) {
                    for (int i = 0; i < 25; i++) {
                        progress++;
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ringProgressBar.setProgress(progress);
                        switch (msg.what) {
                            case 0:
                                testTextView.setText("Connecting to server...");
                                break;
                            case 1:
                                testTextView.setText("Sending image...");
                                break;
                            case 2:
                                testTextView.setText("Recognizing...");
                                break;
                            case 3:
                                testTextView.setText("Getting result...");
                                break;
                            case 4:
                                //testTextView.setText(""+Double.parseDouble(result.substring(1, result.length()-2)));
                                break;
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        // get the image from the previous activity
        Intent intent = getIntent();
        String finalImage = intent.getStringExtra("finalImage");
        uri = Uri.parse(finalImage);


        testTextView = (TextView) findViewById(R.id.textView4);


        ringProgressBar = (RingProgressBar) findViewById(R.id.progress_bar);

        // show a toast when the process is complete
        ringProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                Toast.makeText(ProcessingActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });


        // launches the main method
        try {
            recognizeNew();
            handler.sendEmptyMessage(4);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Goes to the resultActivity if everything is okay.
     * @param res - prediction float
     */
    void goToResultActivity(float res) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("classResult", res);
        System.out.println(i.getExtras());
        startActivity(i);
    }


    /**
     * if "back" is pressed, return to launchActivity.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProcessingActivity.this, LaunchActivity.class));
        finish();

    }


    /**
     * Converts to a scaled and optimized for the network resolution, encodes it into Base64,
     * then calls next method sendPost().
     * @throws IOException
     * @throws InterruptedException
     */
    void recognizeNew() throws IOException, InterruptedException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        bitmap = Bitmap.createScaledBitmap(bitmap, PIXEL_WIDTH, PIXEL_WIDTH, true);
        String encoded = encodeImage(bitmap);

        sendPost(encoded);
    }

    /**
     * Opens a Http connection, sends the image to the server;
     * gets the response - JSON with prediction;
     * parses the JSON for the prediction float;
     * sends the response to resultActivity;
     * if the connection cannot be established, goes to failedConnectionActivity.
     *
     * @param jpeg - encoded Base64 image
     * @throws InterruptedException
     */
    public void sendPost(String jpeg) throws InterruptedException {
        // Http connection must be opened in a separate thread in Android.
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // open Http connection
                    Log.i("Thread", "In thread");
                    handler.sendEmptyMessage(0);
                    URL url = new URL(URL_ADDRESS);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    handler.sendEmptyMessage(1);

                    // put Base64 image into JSON
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("image", jpeg);

                    // send the JSON to server
                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    // close Http connection
                    os.flush();
                    os.close();
                    handler.sendEmptyMessage(2);

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    // get the response
                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    result = br.readLine();

                    conn.disconnect();

                    handler.sendEmptyMessage(3);
                    runOnUiThread(new Runnable() {
                        // parse the response JSON
                        @Override
                        public void run() {
                            result = result.substring(1, result.length() - 1);
                            float res = Float.parseFloat(result);
                            if (result != null) {
                                goToResultActivity(res);
                            } else {
                                throw new NullPointerException();
                            }
                        }
                    });
                // if unable to establish Http connection, handle
                } catch (Exception e) {
                    goToFailedConnectionActivity();
                }
            }
        });

        thread.start();
    }

    /**
     * Converts the bitmap image into Base64 format.
     * @param thumbnail bitmap image
     * @return encoded Base64 image
     */
    public static String encodeImage(Bitmap thumbnail) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    /**
     * Goes to the connectionFailed activity if Http connection was not established successfully.
     */
    void goToFailedConnectionActivity() {
        startActivity(new Intent(ProcessingActivity.this, FailedConnectionActivity.class));
    }

}
