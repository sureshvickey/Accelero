package com.viki.accelero;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccdt.easyble.BleDevice;
import com.ccdt.easyble.BleManager;
import com.ccdt.easyble.Logger;
import com.ccdt.easyble.gatt.bean.CharacteristicInfo;
import com.ccdt.easyble.gatt.bean.ServiceInfo;
import com.ccdt.easyble.gatt.callback.BleConnectCallback;
import com.ccdt.easyble.gatt.callback.BleNotifyCallback;
import com.ccdt.easyble.gatt.callback.BleReadCallback;
import com.ccdt.easyble.gatt.callback.BleWriteCallback;
import com.ccdt.easyble.scan.BleScanCallback;
import com.viki.accelero.bluetooth.utils.ByteUtils;
import com.viki.accelero.kdgaugeview.KdGaugeView;
import com.viki.accelero.serialcomm.driver.UsbSerialDriver;
import com.viki.accelero.serialcomm.driver.UsbSerialPort;
import com.viki.accelero.serialcomm.driver.UsbSerialProber;
import com.viki.accelero.serialcomm.util.SerialInputOutputManager;
import com.viki.accelero.tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SerialInputOutputManager.Listener {
    KdGaugeView gaugeView;
    BatteryView batteryView;
    Indicator mHorizontalBattery;
    TextView timeTextView;
    TextView tempAndLocTextView;
    public ImageView blIndicator,wifiIndicator,cellularIndicator,answerCall,dismissCall;
    LinearLayout linearLayout;
    //SensorManager mSensorManager;
    //Sensor mTemperature;
    private GpsTracker gpsTracker;
    int[] colorArr;
    float[] changeValArr;
    Random randomNum;
    int batteryVal = 0;
    String[] days = new String[]{"Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};
    private BleManager manager;
    public static final UUID SERVICE_LEVEL_UUID = UUID
            .fromString("19B10000-E8F1-537E-4F6C-D104768A1214");
    private CoordinatorLayout fragmentContainer;
    public CardView callIndicator;
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int READ_WAIT_MILLIS = 200;
    private SerialInputOutputManager usbIoManager;
    private List<BleDevice> deviceList = new ArrayList<>();
    public static boolean isConnected = false;
    RelativeLayout parent;
    public static final String KEY_DEVICE_INFO = "keyDeviceInfo";

    private BleDevice device;
    private LinearLayout llWrite, llRead;
    private EditText etWrite;
    private ProgressBar pb;
    private TextView tvConnectionState, tvReadResult, tvWriteResult,
            tvNotify, tvInfoCurrentUuid, tvInfoNotification,tvQuery;
    private ExpandableListView elv;
    private List<ServiceInfo> groupList = new ArrayList<>();
    private List<List<CharacteristicInfo>> childList = new ArrayList<>();
    private List<String> notifySuccessUuids = new ArrayList<>();

    private ServiceInfo curService;
    private CharacteristicInfo curCharacteristic;
    private boolean isAccessKeyCheck = false;
    private boolean isCarSpeedCheck = false;

    public static final UUID ACCESS_LEVEL_UUID = UUID
            .fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    public static final UUID ACCESS_KEY_UUID = UUID
            .fromString("19B10000-E8F3-537E-4F6C-D104768A1214");
    public static final UUID SPEED_UUID = UUID
            .fromString("19B10000-E8F5-537E-4F6C-D104768A1215");
    public static final UUID OP_MODE_UUID = UUID
            .fromString("19B10000-E8F4-537E-4F6C-D104768A1215");
    public static final UUID SOC_UUID = UUID
            .fromString("19B10000-E8F6-537E-4F6C-D104768A1215");
    public static final UUID FAULT_STATUS_UUID = UUID
            .fromString("19B10000-E8F7-537E-4F6C-D104768A1215");
    public static final UUID CAR_SPEED_UUID = UUID
            .fromString("19B10000-E8F8-537E-4F6C-D104768A1215");

    public static final UUID FNR_SWITCH_UUID = UUID
            .fromString("19B10000-E8FD-537E-4F6C-D104768A1215");
    public static final UUID ODOMETER_UUID = UUID
            .fromString("19B10000-E8FE-537E-4F6C-D104768A1215");

    private int maxCarSpeed;

    private boolean isAutoQueryClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        randomNum = new Random();
        setContentView(R.layout.activity_main);
        gaugeView = findViewById(R.id.speedMeter);
        batteryView = findViewById(R.id.battery);
        mHorizontalBattery = findViewById((R.id.horizontalBattery));
        tempAndLocTextView = findViewById((R.id.tempAndLocTextView));
        timeTextView = findViewById((R.id.timeTextView));
        blIndicator = findViewById((R.id.blIndicator));
        wifiIndicator = findViewById((R.id.wifiIndicator));
        cellularIndicator = findViewById((R.id.cellularIndicator));
        answerCall = findViewById((R.id.answerCall));
        dismissCall = findViewById((R.id.dismissCall));
        callIndicator = findViewById((R.id.callIndicator));
        linearLayout = findViewById(R.id.header);
        parent = findViewById(R.id.parentLayout);
        callIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callIndicator.setVisibility(View.INVISIBLE);
            }
        });
        initBleManager();
        blIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BleManager.isBluetoothOn()) {
                    BleManager.toggleBluetooth(true);
                }
                //for most devices whose version is over Android6,scanning may need GPS permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isGpsOn()) {
                    Toast.makeText(getApplicationContext(), "Please turn on GPS before scanning", Toast.LENGTH_LONG).show();
                    return;
                }
                startScan();
            }
        });
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},1);
            }
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},2);
            }
        }

//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        //mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE); // requires API level 14.
       /* try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }*/
        try {
            updateTimeAtEveryMin();
            Timer t = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setGaugeSpeedAndColors();
                        }
                    });
                }

                ;
            };
            t.scheduleAtFixedRate(tt, 500, 3000);
            Timer t1 = new Timer();
            TimerTask tt1 = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBatteryValues();
                        }
                    });
                }

                ;
            };
            t1.scheduleAtFixedRate(tt1, 2000, 6000);
//            Timer tempTask = new Timer();
//            TimerTask tempTask1 = new TimerTask() {
//                @Override
//                public void run() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            String location = getLocation();
//                            if (location != null && !location.isEmpty())
//                                tempAndLocTextView.setText(getLocation() + getResources().getString(R.string.celsius));
//                        }
//                    });
//                }
//
//                ;
//            };
//            tempTask.scheduleAtFixedRate(tempTask1, 0, 10000);
        }catch (Exception e){
            e.printStackTrace();
        }

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        Log.i("Available Devices", Arrays.toString(new List[]{availableDrivers}));
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
            manager.requestPermission(driver.getDevice(), mPermissionIntent);
        }

        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
            port.setParameters(57600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            usbIoManager = new SerialInputOutputManager(port, this);
            Executors.newSingleThreadExecutor().submit(usbIoManager);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startScan() {
        manager.startScan(new BleScanCallback() {
            @Override
            public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {

                for (BleDevice d : deviceList) {
                    if (device.address.equals(d.address)) {
                        return;
                    }
                }
                try{
                    Log.i("bluetooth",device.name+"  rssi: "+rssi);
                    if (rssi >= -70) {
                        deviceList.add(device);

                        BleManager.getInstance().connect(device.address, connectCallback);
//                        Intent intent = new Intent(MainActivity.this, OperateActivity.class);
//                        intent.putExtra(OperateActivity.KEY_DEVICE_INFO, device);
//                        startActivity(intent);
                        Toast.makeText(getApplicationContext(),"device found",Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onStart(boolean startScanSuccess, String info) {
                Log.e("bluetooth", "start scan = " + startScanSuccess + "   info: " + info);

                if (startScanSuccess) {
                    deviceList.clear();
                }
            }

            @Override
            public void onFinish() {
                Log.e("bluetooth", "scan finish");

            }
        });
    }

    private BleConnectCallback connectCallback = new BleConnectCallback() {
        @Override
        public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
            Logger.e("start connecting:" + startConnectSuccess + "    info=" + info);
            MainActivity.this.device = device;
            //updateConnectionStateUi(false);
            if (!startConnectSuccess) {
                Toast.makeText(MainActivity.this, "start connecting fail:" + info, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onConnected(BleDevice device) {
            isAccessKeyCheck = true;
            BleManager.getInstance().read(device,SERVICE_LEVEL_UUID.toString(),ACCESS_KEY_UUID.toString(),readCallback);
        }

        @Override
        public void onDisconnected(String info, int status, BleDevice device) {
            Logger.e("disconnected!");
            //reset();
            //updateConnectionStateUi(false);
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("connect fail:" + info);
            Toast.makeText(MainActivity.this,
                    getResources().getString(failCode == BleConnectCallback.FAIL_CONNECT_TIMEOUT ?
                            R.string.tips_connect_timeout : R.string.tips_connect_fail), Toast.LENGTH_LONG).show();
            //reset();
            //updateConnectionStateUi(false);
        }
    };

    private BleWriteCallback writeCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(byte[] data, BleDevice device) {
            Logger.e("write success:" + ByteUtils.bytes2HexStr(data));
           // tvWriteResult.setText(ByteUtils.bytes2HexStr(data));
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("write fail:" + info);
            //tvWriteResult.setText("write fail:" + info);
        }
    };

    private BleReadCallback readCallback = new BleReadCallback() {
        @Override
        public void onReadSuccess(byte[] data, BleDevice device) {
            Logger.e("read success:"  + ByteUtils.bytes2HexStr(data));
            try {
                if (isAccessKeyCheck) {
                    isAccessKeyCheck = false;
                    String ACCESS_KEY = new String(data);
                    if (ACCESS_KEY.equals("HelloWorld")) {
                        isCarSpeedCheck = true;
                        addDeviceInfoDataAndUpdate();
                       // updateConnectionStateUi(true);
                        BleManager.getInstance().write(device, SERVICE_LEVEL_UUID.toString(), OP_MODE_UUID.toString(), ByteUtils.hexStr2Bytes("01"), writeCallback);
                        BleManager.getInstance().read(device, SERVICE_LEVEL_UUID.toString(), CAR_SPEED_UUID.toString(), readCallback);
                        BleManager.getInstance().notify(device, SERVICE_LEVEL_UUID.toString(), CAR_SPEED_UUID.toString(), notifyCallback);
                    }
                } else if (isCarSpeedCheck) {
                    isCarSpeedCheck = false;
                    maxCarSpeed = (data[0] & 0xFF);
                    Logger.e("car Speed" + maxCarSpeed);
                    //BleManager.getInstance().write(device, SERVICE_LEVEL_UUID.toString(), CAR_SPEED_UUID.toString(), ByteUtils.hexStr2Bytes("" + maxCarSpeed), writeCallback);
                    // writeRandomValues(device);
                } else {
                    tvReadResult.setText(ByteUtils.bytes2HexStr(data));
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("read fail:" + info);
            tvReadResult.setText("read fail:" + info);
        }
    };

    private BleNotifyCallback notifyCallback = new BleNotifyCallback() {
        @Override
        public void onCharacteristicChanged(byte[] data, BleDevice device) {
            String s = ByteUtils.bytes2HexStr(data);
            Logger.e("onCharacteristicChanged:" + s);
           // updateNotificationInfo(s);
        }

        @Override
        public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
            Logger.e("notify success uuid:" + notifySuccessUuid);
//            tvInfoNotification.setVisibility(View.VISIBLE);
//            if (!notifySuccessUuids.contains(notifySuccessUuid)) {
//                notifySuccessUuids.add(notifySuccessUuid);
//            }
           // updateNotificationInfo("");
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
            Logger.e("notify fail:" + info);
            Toast.makeText(MainActivity.this, "notify fail:" + info, Toast.LENGTH_LONG).show();
        }
    };

    private void addDeviceInfoDataAndUpdate() {
        if (device == null) return;
        Map<ServiceInfo, List<CharacteristicInfo>> deviceInfo = BleManager.getInstance().getDeviceServices(device.address);
        if (deviceInfo == null) {
            return;
        }
        for (Map.Entry<ServiceInfo, List<CharacteristicInfo>> e : deviceInfo.entrySet()) {
            if (e.getKey().uuid.equals(SERVICE_LEVEL_UUID.toString())) {
                groupList.add(e.getKey());
                childList.add(e.getValue());
            }
        }
        //adapter.notifyDataSetChanged();
        //tvInfoCurrentUuid.setVisibility(View.VISIBLE);
    }

    private boolean isGpsOn() {
        LocationManager locationManager
                = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    private void initBleManager() {
        //check if this android device supports ble
        if (!BleManager.supportBle(this)) {
            return;
        }
        //open bluetooth without a request dialog
        BleManager.toggleBluetooth(true);

        BleManager.ScanOptions scanOptions = BleManager.ScanOptions
                .newInstance()
                .scanPeriod(8000)
                .scanServiceUuids(new UUID[]{SERVICE_LEVEL_UUID})
                .scanDeviceName(null);

        BleManager.ConnectOptions connectOptions = BleManager.ConnectOptions
                .newInstance()
                .connectTimeout(12000);

        manager = BleManager
                .getInstance()
                .setScanOptions(scanOptions)
                .setConnectionOptions(connectOptions)
                .setLog(true, "EasyBle")
                .init(this.getApplication());
    }



    @Override
    protected void onResume() {
        super.onResume();
        /*if (mTemperature != null) {
            mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "No Ambient Temperature Sensor !", Toast.LENGTH_LONG).show();
        }*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }

    private void updateTimeAtEveryMin() {
        Timer t1 = new Timer();
        TimerTask tt1 = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy");
                        SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm aa");
                        String date = dateformat.format(cal.getTime());
                        String time = timeformat.format(cal.getTime());
                        timeTextView.setText(time+"\n"+date+" "+days[cal.get(Calendar.DAY_OF_WEEK) - 1] );
                    }
                });
            }

            ;
        };
        t1.scheduleAtFixedRate(tt1, 0, 1000);
    }

    private void setGaugeSpeedAndColors() {
        float max = 120.0f;
        float min = 20.0f;
        float factor = max - min;
        float gaugeSpeed = randomNum.nextFloat() * factor + min;
        ;
        if (gaugeSpeed <= 50.0f) {
            colorArr = new int[]{Color.GREEN};
            changeValArr = new float[]{gaugeSpeed};
        } else if (gaugeSpeed > 50.0f && gaugeSpeed <= 80.0f) {
            colorArr = new int[]{Color.GREEN, Color.YELLOW};
            changeValArr = new float[]{50.0f, gaugeSpeed};
        } else {
            colorArr = new int[]{Color.GREEN, Color.YELLOW, Color.RED};
            changeValArr = new float[]{50.0f, 80.0f, gaugeSpeed};
        }
        gaugeView.setSpeed(gaugeSpeed);
        gaugeView.setColors(colorArr);
        gaugeView.setChangeValues(changeValArr);
    }

    private void setBatteryValues() {
        batteryVal = batteryVal + 10;
        if (batteryVal == 100) {
            batteryVal = 0;
        }
        int currentVal = 100 - batteryVal;
        batteryView.setSpeed(currentVal);
        if (currentVal >= 75) {
            batteryView.setColors(new int[]{Color.GREEN});
        } else if (currentVal < 75 && currentVal >= 50) {
            batteryView.setColors(new int[]{Color.YELLOW});
        } else if (currentVal < 50 && currentVal >= 25) {
            batteryView.setColors(new int[]{Color.rgb(255, 127, 80)});
        } else {
            batteryView.setColors(new int[]{Color.RED});
        }
        batteryView.setChangeValues(new float[]{currentVal});
       /* final boolean isCharging  = (status == BatteryManager.BATTERY_STATUS_CHARGING)
                || (status == BatteryManager.BATTERY_STATUS_FULL);*/
        final boolean isCharging = false;
        //BatteryEvent batteryEvent = new BatteryEvent(currentVal,isCharging,status == BatteryManager.BATTERY_STATUS_FULL);
        mHorizontalBattery.setProgress(currentVal);
        mHorizontalBattery.setBatteryCharge(false);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float ambient_temperature = sensorEvent.values[0];
        //temperature through String.valueOf(ambient_temperature) + getResources().getString(R.string.celsius));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public String getLocation(){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            return getWeatherData(String.valueOf(latitude),String.valueOf(longitude));
        }else{
            //gpsTracker.showSettingsAlert();
        }
        return "";
    }

    public String getWeatherData(String latitude, String longitude) {
        HttpURLConnection con = null ;
        InputStream is = null;

        try {
            String url = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=" + latitude + "&lon=" + longitude + "&cnt=10&mode=json";
            con = (HttpURLConnection) ( new URL(url)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            StringBuffer buffer = null;
            try {
                buffer = new StringBuffer();
                is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ( (line = br.readLine()) != null )
                    buffer.append(line + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            is.close();
            con.disconnect();
            JSONObject jObj = new JSONObject(buffer.toString());
            String cityName = getString("name", jObj);
            JSONObject mainObj = getObject("main", jObj);
            float temp = getFloat("temp", mainObj);
            return cityName + " "+ temp;
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }
        return null;

    }

    private static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }
    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    @Override
    protected void onStart() {
        super.onStart();


    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //exitFromConversation();
//        DialogInterface.OnClickListener confirmExitListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                exitFromConversation();
//            }
//        };
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//        if (fragment != null) {
//            if (fragment instanceof ConversationFragment) {
//                showConfirmExitDialog(confirmExitListener);
//            } else {
//                super.onBackPressed();
//            }
//        } else {
//            super.onBackPressed();
//        }
    }


    protected void showConfirmExitDialog(DialogInterface.OnClickListener confirmListener) {
        //creazione del dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage("Confirm exit");
        builder.setPositiveButton(android.R.string.ok, confirmListener);
        builder.setNegativeButton(android.R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public CoordinatorLayout getFragmentContainer() {
        return fragmentContainer;
    }



    @Override
    public void onNewData(byte[] data) {
        receive(data);
    }

    @Override
    public void onRunError(Exception e) {

    }

    private void receive(byte[] data) {
//        SpannableStringBuilder spn = new SpannableStringBuilder();
//        spn.append("receive " + data.length + " bytes" + Arrays.toString(data));
        String str = new String(data, StandardCharsets.UTF_8);
//        if(data.length > 0)
//            spn.append(HexDump.dumpHexString(data)).append("\n");
        Log.i("new Data : ",str);

    }


//    class RequestTask extends AsyncTask<String, String, String>{
//
//        @Override
//        protected String doInBackground(String... uri) {
//            Httpcl httpclient = new DefaultHttpClient();
//            HttpResponse response;
//            String responseString = null;
//            try {
//                response = httpclient.execute(new HttpGet(uri[0]));
//                StatusLine statusLine = response.getStatusLine();
//                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    response.getEntity().writeTo(out);
//                    responseString = out.toString();
//                    out.close();
//                } else{
//                    //Closes the connection.
//                    response.getEntity().getContent().close();
//                    throw new IOException(statusLine.getReasonPhrase());
//                }
//            } catch (ClientProtocolException e) {
//                //TODO Handle problems..
//            } catch (IOException e) {
//                //TODO Handle problems..
//            }
//            return responseString;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            //Do anything with response..
//        }
//    }

}