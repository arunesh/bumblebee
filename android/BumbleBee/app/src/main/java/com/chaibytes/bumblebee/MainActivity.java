package com.chaibytes.bumblebee;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaibytes.bumblebee.backend.MotionDataLoader;
import com.chaibytes.bumblebee.data.MotionData;
import com.chaibytes.bumblebee.data.UserLocation;
import com.chaibytes.bumblebee.location.LocationTracker;
import com.chaibytes.bumblebee.util.ServicesProviderSingleton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.motion.Smotion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        PedometerTracker.PedometerCallback,
        LocationTracker.LocationCallback, ItemPickerDialogFragment.OnItemSelectedListener {
    private static final int PED_UNINITIALIZED = 1000;
    private static final int PED_INITIALIZED = 1001;
    private static final int PED_STARTED = 1002;
    private static final int PED_STOPPED = 1003;

    public static final String TAG = "BumbleBee";
    private GoogleMap googleMap;
    private PedometerTracker pedometerTracker;
    private Smotion mMotion;
    private RelativeLayout mainRelativeLayout;
    private LineChartView lineChartView;
    private ArrayList<Integer> chartData;
    private CircleImageView circleImageView;
    private ArrayList<UserLocation> locationHistory;
    private TextView chartLabelTv;
    private int pedState = PED_UNINITIALIZED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        locationHistory = new ArrayList<>();
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_rel_layout);
        lineChartView = (LineChartView) findViewById(R.id.chart);
        circleImageView = (CircleImageView) findViewById(R.id.profile_image);
        chartLabelTv = (TextView) findViewById(R.id.chart_label);
        setupLineChatView();
        View decorView = getWindow().getDecorView();

        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();
/*

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
*/

        // Get the SupportMapFragment and register for the callback
        // when the map is ready for use.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ServicesProviderSingleton.init(this);
        ServicesProviderSingleton.getInstance().startLocationServices();
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilePicker();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "onMapReady called.");
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        // Position the map's camera near Sydney, Australia.
        this.googleMap = googleMap;
        initializePedometerTracker();
        startPedometerTracker();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPedometerTracker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPedometerTracker();
    }

    private void moveMapTo(LatLng latLng) {
        // googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34, 151)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void startPedometerTracker() {
        if (pedState == PED_STOPPED || pedState == PED_INITIALIZED) {
            pedometerTracker.start(MotionTest.MODE_PEDOMETER_PERIODIC);
            pedState = PED_STARTED;
        } else {
            Log.i(TAG, "Ignoring startPed() call. Ped not initialized or not stopped.");
        }
    }

    private void stopPedometerTracker() {
        if (pedState == PED_STARTED) {
            pedometerTracker.stop();
            pedState = PED_STOPPED;
        } else {
            Log.i(TAG, "Ignoring stopPed() call. Ped not started.");
        }
    }

    private void initializePedometerTracker() {
        // Smotion initialize
        mMotion = new Smotion();
        try {
            mMotion.initialize(this);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            showSnackBar("Unable to initialize Samsung Motion Detector.");
            return;
        } catch (SsdkUnsupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            showSnackBar("Unable to initialize Samsung Motion Detector.");
            return;
        }

        if (pedometerTracker == null) {
            boolean isPedometerUpDownAvailable = mMotion
                    .isFeatureEnabled(Smotion.TYPE_PEDOMETER_WITH_UPDOWN_STEP);
            pedometerTracker = new PedometerTracker(this, Looper.getMainLooper(), mMotion,
                    isPedometerUpDownAvailable);
            pedometerTracker.setCallback(this);
        }

        pedometerTracker.initialize();
        pedState = PED_INITIALIZED;
    }

    @Override
    public void onDataAvailable(MotionData motionData) {
        //showCurrentLocation();
        addPointToChart(motionData);
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(mainRelativeLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void updateLocation(UserLocation location) {
        if (location == null) {
            Log.i(TAG, "Attempt to update with null Location.");
            return;
        }
        if (!locationHistory.isEmpty()) {
            UserLocation lastLocation = locationHistory.get(locationHistory.size() - 1);
            if (lastLocation.getmLatitude() == location.getmLatitude() &&
                    lastLocation.getmLongitude() == lastLocation.getmLongitude()) {
                // No significant changes in location.
                Log.i(TAG, "Discarding location update.");
                return;
            }
        }
        googleMap.clear();
        addFriends();
        for (UserLocation userLocation : locationHistory) {
            addGraymarker(new LatLng(userLocation.getmLatitude(), userLocation.getmLongitude()));
        }
        LatLng newLatLng = new LatLng(location.getmLatitude(), location.getmLongitude());
        moveMapTo(newLatLng);
        addGreenMarker(newLatLng);
        locationHistory.add(location);
    }

    private void showCurrentLocation() {
        Log.e(TAG, "showCurrentLocation called.");
        Location location = ServicesProviderSingleton.getInstance().getLocationTracker().getLastLocation();
        if (location != null) {
            LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            moveMapTo(newLatLng);
            addGreenMarker(newLatLng);
        } else {
            Log.e(TAG, "NULL LOCATION");
            showSnackBar("Null Location.");
        }
    }

    private void addGreenMarker(LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng).icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(this,
                        R.drawable.ic_directions_walk_5dp))));

    }

    private void addGraymarker(LatLng latlng) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(latlng).icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(this,
                        R.drawable.ic_lens_black_24dp))));
    }

    private void addPersonMarker(LatLng latLng, String name) {
         Marker marker = googleMap.addMarker(new MarkerOptions().title(name)
                .position(latLng).icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(this,
                        R.drawable.ic_person_black_24dp))));
    }

    private void setupLineChatView() {
        chartData = new ArrayList<>();
        lineChartView.setInteractive(true /* isInteractive */);
        lineChartView.setZoomType(ZoomType.HORIZONTAL);
        lineChartView.setContainerScrollEnabled(true /* isEnabled */,
                ContainerScrollType.HORIZONTAL);
    }

    private void addPointToChart(MotionData motionData) {
        updateLocation(motionData.getUserLocation());
        setChartLabel(motionData);
        chartData.add((int)(motionData.getSpeed() * 10));

        List<PointValue> values = new ArrayList<PointValue>();
        int index = 0;
        int max = 0;
        for (int data : chartData) {
            values.add(new PointValue(index, data));
            max = Math.max(max, data);
            index ++;
        }

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(0xFF00BFFF).setCubic(true);
        line.setHasPoints(false);
        line.setStrokeWidth(2);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);
        lineChartView.setLineChartData(data);
        lineChartView.setViewportCalculationEnabled(false);
        resetViewport(max, values.size());
    }

    private void resetViewport(int maxX, int maxY) {
        int left = Math.max(maxY - 30, 0);
        int right = Math.max(maxY, 30);
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(lineChartView.getMaximumViewport());
        v.bottom = -5;
        v.top = Math.max(maxX, 100) + 15;
        v.left = left;
        v.right = right;
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onLocationReady() {
        showCurrentLocation();
    }

    public void showFilePicker() {
        ArrayList<String> fileList = MotionDataLoader.listFiles(this);
        ArrayList<ItemPickerDialogFragment.Item> pickerItems = new ArrayList<>();
        int index = 1;
        for (String filename : fileList) {
            File f = new File(filename);
            ItemPickerDialogFragment.Item item = new ItemPickerDialogFragment.Item(f.getName(), index);
            item.setStringValue(filename);
            pickerItems.add(item);
            index ++;
        }

        ItemPickerDialogFragment dialog = ItemPickerDialogFragment.newInstance(
                this.getString(R.string.title_file_picker),
                pickerItems,
                -1
        );
        dialog.show(getFragmentManager(), "ItemPicker");
    }

    @Override
    public void onItemSelected(ItemPickerDialogFragment fragment, ItemPickerDialogFragment.Item item, int index) {
        String selectedValue = item.getStringValue();
        if (pedState == PED_STARTED) {
            locationHistory.clear();
            googleMap.clear();
            pedometerTracker.addOfflineData(new MotionDataLoader(this, selectedValue).readAll());
        }
    }

    private void setChartLabel(MotionData motionData) {
        String label = motionData.getMotionState();
        if (label.equals(MotionData.WALK_STATE)) {
            chartLabelTv.setText(MotionData.WALK_STATE);
            chartLabelTv.setTextColor(0xFF00BFFF);
        } else if (label.equals(MotionData.RUN_STATE)) {
            chartLabelTv.setText(MotionData.RUN_STATE);
            chartLabelTv.setTextColor(Color.RED);
        } else {
            chartLabelTv.setText(MotionData.NONE_STATE);
            chartLabelTv.setTextColor(Color.GRAY);
        }
    }

    private void addFriends() {
        LatLng friend1 = new LatLng(37.402292, -122.049298);
        addPersonMarker(friend1, "Jason Brown");
        LatLng friend2 = new LatLng(37.397291, -122.047818);
        addPersonMarker(friend2, "Rohit Khan");
        LatLng friend3 = new LatLng(37.401716, -122.051816);
        addPersonMarker(friend3, "Margaret Britt");
    }
}
