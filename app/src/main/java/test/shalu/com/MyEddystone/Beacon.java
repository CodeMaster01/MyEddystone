package test.shalu.com.MyEddystone;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.jakewharton.rxbinding.view.RxView;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;
import com.zxy.recovery.callback.RecoveryCallback;
import com.zxy.recovery.core.Recovery;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import butterknife.ButterKnife;
import rx.functions.Action1;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Beacon extends Activity {
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothAdapter.LeScanCallback leScanCallback;


    private BluetoothLeScanner mBluetoothLeScanner;

   private Handler mHandler ;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 5000;

    ArrayList<String> devname,rssilist,distance,devicelist;
    ArrayList<String> url;
    int i=0;
    private static ProgressDialog pDialog;
    String code="";
    double round,withcode;
    public String clickUrl,devName,Distance,txpower,sDis;
    Handler handler;
    boolean status=false;
    Runnable runnable;
    @BindView(R.id.dvicelist)ListView mList;
    @BindView(R.id.back)BootstrapButton mBack;
    @BindView(R.id.searchme)ImageView mSearch;


    static WebView mWebview;
    static  LinearLayout mWebLayer;
    static LinearLayout mListLayout;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
        ButterKnife.bind(this);

       //error handler
        Recovery.getInstance()
                .debug(true)
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPage(MainActivity.class)
                .callback(new MyCrashCallback())
                .init(this);
        //error handler

        devname = new ArrayList<String>();
        distance = new ArrayList<String>();
        rssilist=new ArrayList<String>();
        devicelist=new ArrayList<String>();
        url=new ArrayList<String>();

        RxView.clicks(mBack).subscribe(backAction);
        RxView.clicks(mSearch).subscribe(SearchAction);
        mHandler = new Handler();
        handler=new Handler();
        pDialog = new ProgressDialog(Beacon.this);

        mWebview=(WebView)findViewById(R.id.mywebview);
        mWebLayer=(LinearLayout)findViewById(R.id.web);
        mListLayout=(LinearLayout)findViewById(R.id.blist);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bletooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



      mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }catch (Exception e){
                    Toast.makeText(Beacon.this,"run : "+e.toString(),Toast.LENGTH_LONG).show();
                }

            }
        }, 1000);
        startProgress();
        mHandler.postDelayed(scan, 5000);

    }

    //error handler
    static final class MyCrashCallback implements RecoveryCallback {
        @Override
        public void stackTrace(String exceptionMessage) {
            Log.e("zxy", "exceptionMessage:" + exceptionMessage);
        }

        @Override
        public void cause(String cause) {
            Log.e("zxy", "cause:" + cause);
        }

        @Override
        public void exception(String exceptionType, String throwClassName, String throwMethodName, int throwLineNumber) {
            Log.e("zxy", "exceptionClassName:" + exceptionType);
            Log.e("zxy", "throwClassName:" + throwClassName);
            Log.e("zxy", "throwMethodName:" + throwMethodName);
            Log.e("zxy", "throwLineNumber:" + throwLineNumber);
        }

        @Override
        public void throwable(Throwable throwable) {

        }
    }
    //error handler

    Action1 backAction=new Action1() {
        @Override
        public void call(Object o) {
            startActivity(new Intent(Beacon.this, MainActivity.class));
            System.exit(0);
        }
    };
    Action1 SearchAction=new Action1() {
        @Override
        public void call(Object o) {

            mHandler.postDelayed(scan, 100);

               startProgress();
        }
    };

    Runnable scan=new Runnable() {
        @Override
        public void run() {
            try {

                mHandler.removeCallbacks(scan);
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                mHandler.postDelayed(scan, 5000);
            }catch (Exception e){
                Toast.makeText(Beacon.this,"runnable "+e.toString(),Toast.LENGTH_LONG).show();
            }


        }
    };

    public static void adapterRespond(String s){

        mWebLayer.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);
        mWebview.loadUrl(s);// webview process
        mWebview.setWebViewClient(new MyWebViewClient());



        WebSettings webSettings = mWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        int scale = (int) (100 * mWebview.getScale());
        mWebview.setInitialScale(scale);
        mWebview.getSettings().setLoadWithOverviewMode(true);
        mWebview.getSettings().setUseWideViewPort(true);
        mWebview.getSettings().setBuiltInZoomControls(true);

    }
    private static class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            stopProgress();
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            startProgress();
            super.onPageStarted(view, url, favicon);
        }
    }
    public static void startProgress()
    {

        pDialog.setCancelable(false);
        pDialog.setMessage("Please Wait...");
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.show();
    }

    public static void stopProgress()
    {
        pDialog.cancel();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }


    }
    @Override
    protected void onPause() {
        super.onPause();


        Log.d("pau", "pause");

    }


    //Scan low energy devices
    private BluetoothAdapter.LeScanCallback mLeScanCallback =new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device,final int rssi,final byte[] scanRecord)
        {
            // Parse the payload of the advertisement packet
            // as a list of AD structures.


            Log.d("deviceName", "Devices : " + device.getName()+" "+rssi+" ");
            Log.d("rss", " : " + rssi);

            //  mDis.setText(distance);

                new Thread()
                {
                    public void run()
                    {
                        Beacon.this.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                connect(rssi,scanRecord,device);


                            }
                        });
                    }
                }.start();
            }

    };


    public void connect(final int  rssi, byte[] scanRecord,final BluetoothDevice device) {

        Log.d("recei", rssi + " " + scanRecord);
       final List<ADStructure> structures =
                ADPayloadParser.getInstance().parse(scanRecord);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // For each AD structure contained in the advertisement packet.
                for (ADStructure structure : structures) {


                    //get only Eddystone from structure
                    if (structure instanceof EddystoneURL) {

                        // Eddystone URL
                        EddystoneURL es = (EddystoneURL) structure;

                        Log.d("Eddy", "Tx Power = " + es.getTxPower());
                        Log.d("Eddy", "URL = " + es.getURL() + " length : ");


                        getDistance(rssi, es.getTxPower(), device);

                        clickUrl = es.getURL().toString();
                        txpower = String.valueOf(es.getTxPower());
                        devName = device.getName();
                        // notification(clickUrl);


                        try {

                            boolean status = false;
                            for (String st : devicelist) {
                                if (st.contentEquals(devName)) {
                                    status = true;
                                }
                            }

                            if (status) {

                            } else {
                                if (devName != null) {
                                    devicelist.add(devName);
                                } else {
                                    devicelist.add("Eddystone");
                                }

                                url.add("" + clickUrl);
                                rssilist.add("" + rssi);
                                distance.add(" " + sDis + code);
                            }

                            mList.setAdapter(new CustomAdapter(Beacon.this, devicelist, url, rssilist, distance));
                            Log.d("deviceName", "" + devName);

                        } catch (Exception e) {

                        }

                        mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    } else {

                        Log.d("sttaus", "nourl: " + status);


                    }


                }

            }
        }, 4000);
        stopProgress();

    }


    double getDistance(int rssi, int txPower,BluetoothDevice device) {


        double num=Math.pow(10d, ((double) txPower - rssi) / (10 * 2));

        Log.d("diss", " " + num);



        DecimalFormat df = new DecimalFormat("#.##");
        round = Double.valueOf(df.format(num));

        if( round >0.00 && round<100 ){
            withcode=round;
            code=" cm";
        }else if(round >=100 && round<100000){

            withcode=round/100;

            code=" m";
        }else {

            withcode=round/1000;
        }

        Log.d("diss", " round " + round);

        sDis=String.valueOf(withcode);


        Log.d("diss", " disss " + sDis + code);


        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));


    }



}
