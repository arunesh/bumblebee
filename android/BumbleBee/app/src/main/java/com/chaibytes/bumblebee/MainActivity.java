package com.chaibytes.bumblebee;

import android.app.ActionBar;
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

import com.chaibytes.bumblebee.data.MotionData;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PedometerTracker.PedometerCallback, LocationTracker.LocationCallback {
    public static final String TAG = "BumbleBee";
    private GoogleMap googleMap;
    private PedometerTracker pedometerTracker;
    private Smotion mMotion;
    private RelativeLayout mainRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_rel_layout);
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
    protected void onStop() {
        super.onStop();
        stopPedometerTracker();
    }

    private void moveMapTo(LatLng latLng) {
        // googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34, 151)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void startPedometerTracker() {
        pedometerTracker.start(MotionTest.MODE_PEDOMETER_PERIODIC);
    }

    private void stopPedometerTracker() {
        pedometerTracker.stop();
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
            pedometerTracker = new PedometerTracker(Looper.getMainLooper(), mMotion,
                    isPedometerUpDownAvailable);
            pedometerTracker.setCallback(this);
        }

        pedometerTracker.initialize();
    }

    @Override
    public void onDataAvailable(MotionData motionData) {
        showCurrentLocation();
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(mainRelativeLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
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
}
