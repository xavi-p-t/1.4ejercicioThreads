package com.xavi.ejerciciothreads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        TextView texto = findViewById(R.id.textView);
        ImageView imagen = findViewById(R.id.imageView);

        button.setOnClickListener(view -> executor.execute(() -> {

            String data = getDataFromUrl("https://api.myip.com");


            mainThreadHandler.post(() -> {
                if (data != null) {
                    texto.setText(data);
                } else {
                    texto.setText("Error loading data");
                }
            });


            String imageUrl = "https://randomfox.ca/images/122.jpg";
            Bitmap bitmap = downloadImage(imageUrl);

            
            mainThreadHandler.post(() -> {
                if (bitmap != null) {
                    imagen.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            });
        }));
    }

    private String getDataFromUrl(String demoIdUrl) {
        String result = null;
        try {
            URL url = new URL(demoIdUrl);
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setAllowUserInteraction(false);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.setRequestMethod("GET");
            httpsConn.connect();

            int resCode = httpsConn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpsConn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = sb.toString();
            } else {
                Log.e("Network", "HTTP Error Code: " + resCode);
            }
        } catch (IOException e) {
            Log.e("Network", "Error during network call", e);
        }
        return result;
    }

    private Bitmap downloadImage(String urlString) {
        Bitmap bitmap = null;
        try {
            InputStream in = new URL(urlString).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("ImageDownload", "Error downloading image", e);
        }
        return bitmap;
    }
}