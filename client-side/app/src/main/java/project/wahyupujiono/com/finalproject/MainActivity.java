package project.wahyupujiono.com.finalproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView batteryView;
    private TextView connectionView;
    private static Button openDownloadedFolder, downloadResult;

    EditText urlEditTxt, batteryEditTxt, connEditTxt;
    Button download;
    Button checkConnection;
    String URL_POST = "http://10.151.253.136/server-side/server.php";

    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlEditTxt = (EditText) findViewById(R.id.urlEditTxt);
        batteryEditTxt = (EditText) findViewById(R.id.batteryEditTxt);
        connEditTxt = (EditText) findViewById(R.id.connEditTxt);
        download = (Button) findViewById(R.id.download);

        downloadResult = (Button) findViewById(R.id.downloadResult);

        checkConnection = (Button) findViewById(R.id.checkConnection);
        openDownloadedFolder = (Button) findViewById(R.id.openDownloadedFolder);
        batteryView = (TextView) this.findViewById(R.id.batteryView);
        this.registerReceiver(this.mBatInfoReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                InsertSV();
            }
        });

        downloadResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (isConnectingToInternet())
                    new DownloadTask(MainActivity.this, downloadResult, Utils.downloadResultUrl);
                else
                    Toast.makeText(MainActivity.this, "Oops!! There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        });

        openDownloadedFolder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openDownloadedFolder();
            }
        });

        checkConnection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                checkNetworkConnection();
            }
        });
    }


    private void InsertSV() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_POST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplication(),response,Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error+"", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String url = urlEditTxt.getText().toString();
                String battery = batteryEditTxt.getText().toString();
                String connection = connEditTxt.getText().toString();
                params.put("url",url);
                params.put("battery",battery);
                params.put("connection",connection);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //Check Baterry Level
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            batteryView.setText("Battery Level is : "+String.valueOf(level)+"%");
        }
    };

    //Open downloaded folder
    private void openDownloadedFolder() {
        //First check if SD Card is present or not
        if (new CheckForSDCard().isSDCardPresent()) {

            //Get Download Directory File
            File apkStorage = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + Utils.downloadDirectory);

            //If file is not present then display Toast
            if (!apkStorage.exists())
                Toast.makeText(MainActivity.this, "Right now there is no directory. Please download some file first.", Toast.LENGTH_SHORT).show();

            else {

                //If directory is present Open Folder

                /** Note: Directory will open only if there is a app to open directory like File Manager, etc.  **/

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                        + "/" + Utils.downloadDirectory);
                intent.setDataAndType(uri, "file/*");
                startActivity(Intent.createChooser(intent, "Open Download Folder"));
            }

        } else
            Toast.makeText(MainActivity.this, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

    }

    //Check if internet is present or not
    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private void checkNetworkConnection() {
        // BEGIN_INCLUDE(connect)
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if(wifiConnected) {
                checkConnection.setText(getString(R.string.wifi_connection));
            } else if (mobileConnected){
                checkConnection.setText(getString(R.string.mobile_connection));
            }
        } else {
            checkConnection.setText(getString(R.string.no_wifi_or_mobile));
        }
        // END_INCLUDE(connect)
    }
}
