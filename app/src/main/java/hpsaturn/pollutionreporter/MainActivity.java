package hpsaturn.pollutionreporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.bleservice.ServiceBLE;
import hpsaturn.pollutionreporter.bleservice.ServiceInterface;
import hpsaturn.pollutionreporter.bleservice.ServiceManager;
import hpsaturn.pollutionreporter.bleservice.ServiceScheduler;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.view.ChartFragment;
import hpsaturn.pollutionreporter.view.MapFragment;
import hpsaturn.pollutionreporter.view.RecordsFragment;
import hpsaturn.pollutionreporter.view.ScanFragment;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.bt_map)
    Button btMap;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    private ScanFragment scanFragment;
    private EasyPreference.Builder prefBuilder;
    private ChartFragment chartFragment;
    private ServiceManager serviceManager;
    private Fragment mapFragment;
    private RecordsFragment recordsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBleService();

        ButterKnife.bind(this);
        prefBuilder = AppData.getPrefBuilder(this);

        setSupportActionBar(toolbar);
        checkBluetoohtBle();
        setupUI();
        serviceManager = new ServiceManager(this,serviceListener);
        deviceConnect();
    }


    private ServiceInterface serviceListener = new ServiceInterface() {
        @Override
        public void onServiceStatus(String status) {

            if(status.equals(ServiceManager.STATUS_BLE_START)){
                showChartFragment();
            }
            else if(status.equals(ServiceManager.STATUS_BLE_FAILURE)){
                showSnackMessage(R.string.msg_device_reconnecting);
            }
        }

        @Override
        public void onServiceStart() {
        }

        @Override
        public void onServiceStop() {

        }

        @Override
        public void onServiceData(byte[] bytes) {
            refreshUI();
            String strdata = new String(bytes);
            Logger.i(TAG, "data: " + strdata);
            SensorData data = new Gson().fromJson(strdata, SensorData.class);
            if(chartFragment!=null) chartFragment.addData(data.P25);
        }

        @Override
        public void onSensorRecord() {

        }

        @Override
        public void onSensorRecordStop() {

        }
    };

    private View.OnClickListener onFabClickListener = view -> {
        if(prefBuilder.getBoolean(Keys.SENSOR_RECORD,false)){
            stopRecord();
        }else {
            startRecord();
        }
    };

    private void stopRecord(){
        showSnackMessageSlow(R.string.msg_record_stop);
        prefBuilder.addBoolean(Keys.SENSOR_RECORD,false).save();
        refreshUI();
        serviceManager.sensorRecordStop();
    }

    private void startRecord(){
        showSnackMessageSlow(R.string.msg_record);
        prefBuilder.addBoolean(Keys.SENSOR_RECORD,true).save();
        refreshUI();
        serviceManager.sensorRecord();
    }

    private void setupUI() {
        btMap.setOnClickListener(view -> {
//            showMapFragment();
            showRecordsFragment();
        });
        fab.setOnClickListener(onFabClickListener);
        checkForPermissions();
        if (!prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            fab.setVisibility(View.INVISIBLE);
            showScanFragment();
        }
        else {
           showChartFragment();
        }
    }

    private void refreshUI(){
        fab.setVisibility(View.VISIBLE);
        if(prefBuilder.getBoolean(Keys.SENSOR_RECORD,false)){
            fab.setBackgroundTintList(getColorStateList(R.color.color_state_record_stop));
            fab.setImageDrawable(getDrawable(R.drawable.ic_stop_white_24dp));
        }else{
            fab.setBackgroundTintList(getColorStateList(R.color.color_state_record));
            fab.setImageDrawable(getDrawable(R.drawable.ic_record_white_24dp));
        }
    }

    private void showChartFragment() {
        if (chartFragment == null) chartFragment = ChartFragment.newInstance();
        if (!chartFragment.isVisible()) showFragment(chartFragment, ChartFragment.TAG, false);
    }

    private void showScanFragment() {
        if (scanFragment == null) scanFragment = ScanFragment.newInstance();
        if (!scanFragment.isVisible()) showFragment(scanFragment, ScanFragment.TAG, false);
    }

    private void showMapFragment() {
        if (mapFragment == null) mapFragment = MapFragment.newInstance();
        if (!mapFragment.isVisible()) showFragment(mapFragment, MapFragment.TAG, true);
    }

    private void showRecordsFragment() {
        if (recordsFragment == null) recordsFragment = RecordsFragment.newInstance();
        if (!recordsFragment.isVisible()) showFragment(recordsFragment, RecordsFragment.TAG, true);
    }

    public void removeScanFragment(){
        if (scanFragment != null)removeFragment(scanFragment);
    }

    private void showSnackMessage(int id) {
        Snackbar.make(coordinatorLayout, getString(id), Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void showSnackMessageSlow(int id) {
        Snackbar.make(coordinatorLayout, getString(id), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    private void startBleService() {
        Intent newIntent = new Intent(this, ServiceBLE.class);
        startService(newIntent);
        ServiceScheduler.startScheduleService(this, Config.DEFAULT_INTERVAL);
    }

    public void deviceConnect() {
        if (prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            showSnackMessage(R.string.msg_device_connecting);
            serviceManager.start();
        }
    }

    @Override
    void actionUnPair() {
        Logger.i(TAG,"[BLE] unpaired..");
        stopRecord();
        serviceManager.stop();
        prefBuilder.clearAll().save();
        if(chartFragment!=null)chartFragment.clearData();
        removeFragment(chartFragment);
        fab.setVisibility(View.INVISIBLE);
        showScanFragment();
    }

    @Override
    protected void onDestroy() {
        serviceManager.stop();
        serviceManager.unregister();
        super.onDestroy();
    }
}
