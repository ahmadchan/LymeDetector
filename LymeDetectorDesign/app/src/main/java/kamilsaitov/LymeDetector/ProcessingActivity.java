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

public class ProcessingActivity extends AppCompatActivity {

    final int PIXEL_WIDTH = 600;

    Uri uri;
    int progress = 0;

    String result;

    final String URL_ADDRESS = "http://138.68.81.220:5000/predict";

    TextView testTextView;

    RingProgressBar ringProgressBar;
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

        Intent intent = getIntent();
        String finalImage = intent.getStringExtra("finalImage");
        uri = Uri.parse(finalImage);


        testTextView = (TextView) findViewById(R.id.textView4);


        ringProgressBar = (RingProgressBar) findViewById(R.id.progress_bar);

        ringProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                Toast.makeText(ProcessingActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });


        try {
            recognizeNew();
            //testTextView.setText(result);
            //if(result != null) {
            //goToResultActivity();
            handler.sendEmptyMessage(4);
//                    }
//                    else{
//                        throw new NullPointerException();
//                    }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    void goToResultActivity(float res) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("classResult", res);
        System.out.println(i.getExtras());
        startActivity(i);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProcessingActivity.this, LaunchActivity.class));
        finish();

    }


    void recognizeNew() throws IOException, InterruptedException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        bitmap = Bitmap.createScaledBitmap(bitmap, PIXEL_WIDTH, PIXEL_WIDTH, true);
        String encoded = encodeImage(bitmap);

        sendPost(encoded);


        //return result; //change
    }

    public void sendPost(String jpeg) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("In thread", "In thread");
                    handler.sendEmptyMessage(0);
                    URL url = new URL(URL_ADDRESS);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    handler.sendEmptyMessage(1);
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("image", jpeg);

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();
                    handler.sendEmptyMessage(2);

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    result = br.readLine();

                    conn.disconnect();

                    handler.sendEmptyMessage(3);
                    runOnUiThread(new Runnable() {
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
                } catch (Exception e) {
                    goToFailedConnectionActivity();
                }
            }
        });

        thread.start();
    }


    public static String encodeImage(Bitmap thumbnail) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    void goToFailedConnectionActivity() {
        startActivity(new Intent(ProcessingActivity.this, FailedConnectionActivity.class));
    }

}
