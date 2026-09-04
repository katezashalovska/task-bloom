package com.smartfamily.studentplanner.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;
import android.Manifest;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.databinding.ActivityMapsBinding;
import com.smartfamily.studentplanner.utils.ThemeManager;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Circle currentLocationCircle;
    private Marker selectedMarker;
    private boolean isCameraMovedByUser = false;

    private static final int LOCATION_REQUEST_CODE = 1001;

    // Key for SharedPreferences
    private static final String PREF_PERMISSION_HINT_SHOWN = "permission_hint_shown";
    
    // Flag to track if hint was already shown
    private boolean permissionHintShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if hint was already shown before
        permissionHintShown = getSharedPreferences("location_prefs", MODE_PRIVATE)
                .getBoolean(PREF_PERMISSION_HINT_SHOWN, false);

        // Apply theme before setting content view
        applyActivityTheme();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = createLocationCallback();

        setupButtons();
        initializeMapFragment();
    }

    // Setup buttons and their functionality
    private void setupButtons() {
        binding.btnShowLocation.setOnClickListener(v -> checkLocationPermissionAndShow());
        binding.returnButton.setOnClickListener(v -> finish());
        binding.selectLocationButton.setVisibility(View.GONE);

        // Hide buttons in view-only mode
        if (isViewOnlyMode()) {
            binding.selectLocationButton.setVisibility(View.INVISIBLE);
            binding.btnShowLocation.setVisibility(View.INVISIBLE);
        } else {
            binding.selectLocationButton.setOnClickListener(v -> returnSelectedLocation());
            
            updatePermissionButtonVisibility();
        }
    }

    private void updatePermissionButtonVisibility() {
        if (!hasLocationPermission() &&
            !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            binding.btnShowLocation.setContentDescription(getString(R.string.open_settings));
            
            binding.btnShowLocation.setOnLongClickListener(v -> {
                showPermissionInstructionsDialog();
                return true;
            });
            
            binding.btnShowLocation.setOnClickListener(v -> {
                openAppSettings();
            });
        } else {
            binding.btnShowLocation.setContentDescription(getString(R.string.btn_show_location_label));
            
            binding.btnShowLocation.setOnLongClickListener(null);
            
            binding.btnShowLocation.setOnClickListener(v -> checkLocationPermissionAndShow());
        }
    }

    // Initialize the map fragment
    private void initializeMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // Create location callback to handle updates
    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                LatLng location = new LatLng(
                        Objects.requireNonNull(locationResult.getLastLocation()).getLatitude(),
                        Objects.requireNonNull(locationResult.getLastLocation()).getLongitude()
                );
                moveToLocation(location);
            }
        };
    }

    // Start location updates with specified parameters
    private void startLocationUpdates() {
        if (isViewOnlyMode()) return; // Do not start updating the location in the viewing mode

        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(0)
                .setMinUpdateIntervalMillis(5000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location ->
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null));
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        boolean isDarkMode = com.smartfamily.studentplanner.utils.ThemeManager.isDarkModeEnabled(this);
        if (isDarkMode) {
            mMap.setMapStyle(com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));
        } else {
            mMap.setMapStyle(null);
        }

        initializeMapListeners();
        setupInitialCameraPosition();
    }

    // Set listeners for map interactions such as clicks and camera movement
    private void initializeMapListeners() {
        if (!isViewOnlyMode()) {
            mMap.setOnMapClickListener(this::addSelectedMarker);
        } else {
            mMap.setOnMapClickListener(null);  // Disable click listener in view-only mode
        }

        // Handle camera movement
        mMap.setOnCameraMoveListener(() -> updateCircleRadius(mMap.getCameraPosition().zoom));
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isCameraMovedByUser = true;  // Track if user moved the camera
            }
        });
    }

    // Set initial camera position based on provided location or user location
    private void setupInitialCameraPosition() {
        LatLng providedLocation = getProvidedLocation(); // Get provided location

        if (isViewOnlyMode() && providedLocation != null) {
            // In the viewing mode, we always show the transferred location
            addSelectedMarker(providedLocation);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(providedLocation, 15));
        } else if (providedLocation != null) {
            // If the transferred location exists, put its marker
            addSelectedMarker(providedLocation);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(providedLocation, 15));
        } else {
            // Otherwise, we get the user's current location
            getCurrentLocation();
        }
    }

    // Add or update a marker at the selected position
    private void addSelectedMarker(LatLng position) {
        if (selectedMarker != null) {
            selectedMarker.remove();  // Remove the previous marker if it exists
        }
        selectedMarker = mMap.addMarker(new MarkerOptions().position(position));  // Add new marker
        
        // Show the select location button if we're not in view-only mode
        if (!isViewOnlyMode()) {
            binding.selectLocationButton.setVisibility(View.VISIBLE);
        }
    }

    // Check if the location permission is granted and show user location if so
    private void checkLocationPermissionAndShow() {
        if (hasLocationPermission()) {
            if (!isViewOnlyMode()) {
                showUserLocation();  // Show the user's location
            }
        } else {
            // Just request permission directly, no additional dialogs
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    // Get and move to the current location of the user
    private void getCurrentLocation() {
        if (isViewOnlyMode()) return; // We do not receive the current location in the viewing mode

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            moveToLocation(locationLatLng);
                        } else {
                            showToast(R.string.toast_location_not_retrieved);
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    // Show the user's location on the map
    private void showUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            isCameraMovedByUser = false;  // Reset flag indicating camera movement by user
                            LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            moveToLocation(locationLatLng);  // Move the camera to the user's location
                        }
                    });
        }
    }

    // Move the camera to a specific location and update the circle
    private void moveToLocation(LatLng location) {
        updateCurrentLocationCircle(location);
        if (!isCameraMovedByUser) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));  // Move the camera to the new location
        }
    }

    // Update the circle representing the user's current location on the map
    private void updateCurrentLocationCircle(LatLng location) {
        if (currentLocationCircle == null) {
            currentLocationCircle = mMap.addCircle(new CircleOptions()
                    .center(location)
                    .radius(10)
                    .strokeColor(ContextCompat.getColor(this, R.color.circle_stroke_color))
                    .fillColor(ContextCompat.getColor(this, R.color.circle_fill_color))
                    .strokeWidth(4f));  // Create the circle around the user's location
        } else {
            currentLocationCircle.setCenter(location);  // Update the position of the existing circle
        }
    }

    // Adjust the radius of the circle based on the zoom level
    private void updateCircleRadius(float zoomLevel) {
        if (currentLocationCircle != null) {
            double radius = 30 * Math.pow(2, 15 - zoomLevel);  // Adjust the radius based on zoom
            currentLocationCircle.setRadius(radius);
        }
    }

    // Return the selected location when the user confirms
    private void returnSelectedLocation() {
        if (selectedMarker != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedMarker.getPosition().latitude);
            resultIntent.putExtra("longitude", selectedMarker.getPosition().longitude);
            setResult(RESULT_OK, resultIntent); // Return the selected location as a result
        } else {
            setResult(RESULT_CANCELED); // Handle the case of no choice
        }
        finish();
    }

    // Check if the activity is in view-only mode (can be used to disable actions like selecting a location)
    private boolean isViewOnlyMode() {
        return getIntent().getBooleanExtra("isViewOnly", false);
    }

    // Get the location provided via the intent (e.g., for a task location)
    private LatLng getProvidedLocation() {
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        return (latitude != 0.0 && longitude != 0.0) ? new LatLng(latitude, longitude) : null;
    }

    // Check if the location permission is granted
    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request the location permission if not granted
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    // Show a toast message with a resource ID
    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void showPermissionInstructionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.location_permission_title)
               .setMessage(R.string.location_permission_settings_message)
               .setPositiveButton(R.string.open_settings, (dialog, which) -> openAppSettings())
               .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
               .show();
    }

    // Open app settings so user can manually grant permission
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    // Handle the result of a location permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showUserLocation();  // Show the user's location if permission granted
            }
        }
    }

    // Stop location updates when the activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Show a one-time hint about location permission
    private void showLocationPermissionHint() {
        if (!permissionHintShown &&
            !hasLocationPermission() && 
            !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            
            permissionHintShown = true;
            getSharedPreferences("location_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_PERMISSION_HINT_SHOWN, true)
                .apply();
            
            Toast.makeText(this, R.string.location_permission_toast_short, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        showLocationPermissionHint();
        
        if (mMap != null) {
            applyMapTheme();
        }
        
        if (!isViewOnlyMode()) {
            updatePermissionButtonVisibility();
        }
        
        if (hasLocationPermission()) {
            startLocationUpdates();
        }
    }

    // Apply dark theme to the map if enabled
    private void applyMapTheme() {
        boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
        
        if (isDarkMode) {
            // Apply dark theme to the map
            boolean success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)
            );

        } else {
            // Apply light theme (default)
            mMap.setMapStyle(null);
        }
    }

    // Apply activity theme
    private void applyActivityTheme() {
        boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
        
        if (isDarkMode) {
            // Apply dark theme to the activity
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_dark));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_dark));
        } else {
            // Apply light theme (default)
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_light));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_light));
        }
    }
}