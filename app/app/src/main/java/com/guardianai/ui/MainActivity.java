package com.guardianai.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.guardianai.R;
import com.guardianai.auth.TokenManager;
import com.guardianai.data.local.AppDatabase;
import com.guardianai.data.models.*;
import com.guardianai.data.repository.*;
import com.guardianai.data.sensors.SensorDetectionManager;
import com.guardianai.data.sync.NetworkMonitor;
import com.guardianai.data.sync.SyncManager;
import com.guardianai.data.tracking.LocationTrackingService;
import com.guardianai.data.tracking.TrackingStateManager;
import com.guardianai.ui.auth.LoginActivity;
import com.guardianai.utils.UpdateManager;
import com.guardianai.utils.ValidationUtils;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TrackingStateManager.StateListener {

    private static final int PERMISSION_REQUEST_LOCATION = 1001;

    // Repositories
    private UserRepository userRepository;
    private GuardianRepository guardianRepository;
    private LocationRepository locationRepository;
    private SafetyRepository safetyRepository;
    private SOSRepository sosRepository;
    private ThreatRepository threatRepository;
    private WeatherRepository weatherRepository;

    // Sensor Manager
    private SensorDetectionManager sensorDetectionManager;
    private boolean isHeatmapActive = false;

    // Navigation Tabs Container Views
    private View layoutTabHome;
    private View layoutTabMap;
    private View layoutTabGuardians;
    private View layoutTabHistory;
    private View layoutTabSettings;

    // Bottom Navigation Buttons
    private Button btnNavHome, btnNavMap, btnNavGuardians, btnNavHistory, btnNavSettings;

    // Header Components
    private TextView textHeaderNetworkStatus;

    // Home Tab Components
    private TextView textSafetyScore, textRiskCategory, textScoreDisclaimer, textLocationStatus, textTrackingStatus, textWeatherStatus;
    private LinearLayout containerSafetyFactors, containerDashboardThreats;
    private Button btnRequestLocationPermission, btnSubmitLocation, btnTriggerSOS, btnToggleTracking;

    // Map Tab Components
    private MapView mapView;
    private Button btnFilterAll, btnFilterCrime, btnFilterWeather, btnFilterEnv, btnToggleHeatmap;
    private String selectedMapCategoryFilter = "ALL";
    private List<ThreatDto> loadedThreats = new ArrayList<>();

    // Guardian Tab Components
    private LinearLayout containerGuardianList;
    private Button btnAddGuardian;

    // History Tab Components
    private LinearLayout containerHistoryList;

    // Settings & Profile Components
    private TextView textProfileName, textProfileEmail, textProfilePhone;
    private EditText editProfileName, editProfilePhone;
    private TextView textOfflineQueueCount, textAppVersionInfo;
    private Button btnSaveProfile, btnLogout, btnForceSyncQueue, btnLaunchAiSupport, btnDeleteAccount, btnCheckUpdates;

    // Location State
    private double currentLat = 37.7749;
    private double currentLon = -122.4194;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Osmdroid Configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        // Initialize Repositories
        userRepository = new UserRepository(this);
        guardianRepository = new GuardianRepository(this);
        locationRepository = new LocationRepository(this);
        safetyRepository = new SafetyRepository(this);
        sosRepository = new SOSRepository(this);
        threatRepository = new ThreatRepository(this);
        weatherRepository = new WeatherRepository(this);

        // Sensor Manager Initialization
        sensorDetectionManager = new SensorDetectionManager(this);

        initViews();
        setupNavigation();
        checkLocationPermissionState();
        checkNetworkState();
        loadHomeData();

        // Register tracking state observer
        TrackingStateManager.getInstance().addListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        if (sensorDetectionManager != null) {
            sensorDetectionManager.startListening(() -> {
                Toast.makeText(MainActivity.this, "🚨 SHAKE DETECTED! Emergency SOS Triggered via Motion Sensor.", Toast.LENGTH_LONG).show();
                showSOSConfirmationDialog();
            });
        }
        checkNetworkState();
    }

    @Override
    protected void onPause() {
        if (sensorDetectionManager != null) {
            sensorDetectionManager.stopListening();
        }
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        TrackingStateManager.getInstance().removeListener(this);
        super.onDestroy();
    }

    private void initViews() {
        layoutTabHome = findViewById(R.id.layoutTabHome);
        layoutTabMap = findViewById(R.id.layoutTabMap);
        layoutTabGuardians = findViewById(R.id.layoutTabGuardians);
        layoutTabHistory = findViewById(R.id.layoutTabHistory);
        layoutTabSettings = findViewById(R.id.layoutTabSettings);

        btnNavHome = findViewById(R.id.btnNavHome);
        btnNavMap = findViewById(R.id.btnNavMap);
        btnNavGuardians = findViewById(R.id.btnNavGuardians);
        btnNavHistory = findViewById(R.id.btnNavHistory);
        btnNavSettings = findViewById(R.id.btnNavSettings);

        textHeaderNetworkStatus = findViewById(R.id.textHeaderNetworkStatus);

        // Home Views
        textSafetyScore = findViewById(R.id.textSafetyScore);
        textRiskCategory = findViewById(R.id.textRiskCategory);
        textScoreDisclaimer = findViewById(R.id.textScoreDisclaimer);
        textLocationStatus = findViewById(R.id.textLocationStatus);
        textTrackingStatus = findViewById(R.id.textTrackingStatus);
        textWeatherStatus = findViewById(R.id.textWeatherStatus);
        containerSafetyFactors = findViewById(R.id.containerSafetyFactors);
        containerDashboardThreats = findViewById(R.id.containerDashboardThreats);
        btnRequestLocationPermission = findViewById(R.id.btnRequestLocationPermission);
        btnSubmitLocation = findViewById(R.id.btnSubmitLocation);
        btnTriggerSOS = findViewById(R.id.btnTriggerSOS);
        btnToggleTracking = findViewById(R.id.btnToggleTracking);

        // Map Views
        mapView = findViewById(R.id.mapView);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterCrime = findViewById(R.id.btnFilterCrime);
        btnFilterWeather = findViewById(R.id.btnFilterWeather);
        btnFilterEnv = findViewById(R.id.btnFilterEnv);
        btnToggleHeatmap = findViewById(R.id.btnToggleHeatmap);
        Button btnCalcSafeRoute = findViewById(R.id.btnCalcSafeRoute);

        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(new GeoPoint(currentLat, currentLon));
        }

        if (btnCalcSafeRoute != null) {
            btnCalcSafeRoute.setOnClickListener(v -> showSafeRouteDialog());
        }

        if (btnToggleHeatmap != null) {
            btnToggleHeatmap.setOnClickListener(v -> {
                isHeatmapActive = !isHeatmapActive;
                btnToggleHeatmap.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(isHeatmapActive ? "#EF4444" : "#8B5CF6")));
                btnToggleHeatmap.setText(isHeatmapActive ? "🔥 Heatmap Active" : "🔥 Crime Heatmap");
                renderMapMarkers();
            });
        }

        // Guardian Views
        containerGuardianList = findViewById(R.id.containerGuardianList);
        btnAddGuardian = findViewById(R.id.btnAddGuardian);

        // History Views
        containerHistoryList = findViewById(R.id.containerHistoryList);

        // Settings Views
        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);
        textProfilePhone = findViewById(R.id.textProfilePhone);
        editProfileName = findViewById(R.id.editProfileName);
        editProfilePhone = findViewById(R.id.editProfilePhone);
        textOfflineQueueCount = findViewById(R.id.textOfflineQueueCount);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnForceSyncQueue = findViewById(R.id.btnForceSyncQueue);
        btnLaunchAiSupport = findViewById(R.id.btnLaunchAiSupport);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        textAppVersionInfo = findViewById(R.id.textAppVersionInfo);
        btnCheckUpdates = findViewById(R.id.btnCheckUpdates);

        // Home Actions
        btnTriggerSOS.setOnClickListener(v -> showSOSConfirmationDialog());
        btnRequestLocationPermission.setOnClickListener(v -> requestLocationPermission());
        btnSubmitLocation.setOnClickListener(v -> submitCurrentLocation());
        btnToggleTracking.setOnClickListener(v -> toggleTrackingService());

        // Map Filter Actions
        btnFilterAll.setOnClickListener(v -> setMapCategoryFilter("ALL"));
        btnFilterCrime.setOnClickListener(v -> setMapCategoryFilter("CRIME"));
        btnFilterWeather.setOnClickListener(v -> setMapCategoryFilter("WEATHER"));
        btnFilterEnv.setOnClickListener(v -> setMapCategoryFilter("ENVIRONMENTAL"));

        // Guardian Actions
        btnAddGuardian.setOnClickListener(v -> showAddGuardianDialog());

        // Settings Actions
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        btnLogout.setOnClickListener(v -> performLogout());
        if (btnLaunchAiSupport != null) {
            btnLaunchAiSupport.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SupportChatActivity.class);
                startActivity(intent);
            });
        }
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
        }

        Button btnComputerVisionInfo = findViewById(R.id.btnComputerVisionInfo);
        if (btnComputerVisionInfo != null) {
            btnComputerVisionInfo.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("📷 On-Device Computer Vision Architecture")
                        .setMessage("Guardian AI On-Device Computer Vision Engine:\n\n• Privacy-First Processing: Camera feeds are processed locally on-device without cloud video streaming.\n• Threat Recognition: Detects suspicious movements, sudden falls, and crowd disturbance patterns.\n• On-Demand Activation: Runs strictly when user toggles Active Guard Mode.\n\nStatus: Architecture prepared for Phase 5 engine activation.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

        btnForceSyncQueue.setOnClickListener(v -> {
            SyncManager.getInstance(this).triggerSync();
            Toast.makeText(this, "Batch synchronization triggered.", Toast.LENGTH_SHORT).show();
            updateOfflineQueueCount();
        });

        if (btnCheckUpdates != null) {
            btnCheckUpdates.setOnClickListener(v -> performUpdateCheck());
        }
    }

    private void setupNavigation() {
        btnNavHome.setOnClickListener(v -> switchTab(0));
        btnNavMap.setOnClickListener(v -> switchTab(1));
        btnNavGuardians.setOnClickListener(v -> switchTab(2));
        btnNavHistory.setOnClickListener(v -> switchTab(3));
        btnNavSettings.setOnClickListener(v -> switchTab(4));

        switchTab(0); // Default to Home
    }

    private void switchTab(int tabIndex) {
        layoutTabHome.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        layoutTabMap.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        layoutTabGuardians.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        layoutTabHistory.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);
        layoutTabSettings.setVisibility(tabIndex == 4 ? View.VISIBLE : View.GONE);

        if (tabIndex == 0) loadHomeData();
        if (tabIndex == 1) loadMapData();
        if (tabIndex == 2) loadGuardiansData();
        if (tabIndex == 3) loadHistoryData();
        if (tabIndex == 4) loadSettingsData();
    }

    private void checkNetworkState() {
        boolean online = NetworkMonitor.getInstance(this).isOnline();
        if (online) {
            textHeaderNetworkStatus.setText("ONLINE");
            textHeaderNetworkStatus.setTextColor(Color.parseColor("#059669"));
        } else {
            textHeaderNetworkStatus.setText("OFFLINE");
            textHeaderNetworkStatus.setTextColor(Color.parseColor("#DC2626"));
        }
    }

    // ==========================================
    // TRACKING SERVICE TOGGLE & STATE LISTENER
    // ==========================================
    private void toggleTrackingService() {
        if (!hasLocationPermission()) {
            TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.PERMISSION_REQUIRED);
            requestLocationPermission();
            return;
        }

        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        TrackingStateManager.TrackingState currentState = TrackingStateManager.getInstance().getCurrentState();

        if (currentState == TrackingStateManager.TrackingState.RUNNING || currentState == TrackingStateManager.TrackingState.STARTING) {
            stopService(serviceIntent);
            TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.STOPPED);
            Toast.makeText(this, "Location tracking stopped.", Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Background location tracking service started.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStateChanged(TrackingStateManager.TrackingState newState) {
        runOnUiThread(() -> {
            switch (newState) {
                case RUNNING:
                    textTrackingStatus.setText("Status: ● RUNNING (Fused Provider Active)");
                    textTrackingStatus.setTextColor(Color.parseColor("#059669"));
                    btnToggleTracking.setText("Stop Background Tracking");
                    btnToggleTracking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#DC2626")));
                    break;
                case STARTING:
                    textTrackingStatus.setText("Status: ⏳ STARTING SERVICE...");
                    textTrackingStatus.setTextColor(Color.parseColor("#0EA5E9"));
                    btnToggleTracking.setText("Starting...");
                    break;
                case PERMISSION_REQUIRED:
                    textTrackingStatus.setText("Status: ⚠ PERMISSION REQUIRED");
                    textTrackingStatus.setTextColor(Color.parseColor("#D97706"));
                    btnToggleTracking.setText("Enable Location Permissions");
                    btnToggleTracking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D97706")));
                    break;
                case ERROR:
                    textTrackingStatus.setText("Status: ❌ SERVICE ERROR");
                    textTrackingStatus.setTextColor(Color.parseColor("#DC2626"));
                    btnToggleTracking.setText("Retry Background Tracking");
                    btnToggleTracking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#059669")));
                    break;
                case STOPPED:
                default:
                    textTrackingStatus.setText("Status: ○ STOPPED");
                    textTrackingStatus.setTextColor(Color.parseColor("#64748B"));
                    btnToggleTracking.setText("Start Background Tracking");
                    btnToggleTracking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#059669")));
                    break;
            }
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // ==========================================
    // 1. HOME TAB & SAFETY SCORE
    // ==========================================
    private void loadHomeData() {
        checkNetworkState();
        safetyRepository.getSafetyScore(currentLat, currentLon, new SafetyRepository.ApiCallback<SafetyScoreResponseDto>() {
            @Override
            public void onSuccess(SafetyScoreResponseDto result) {
                textSafetyScore.setText(String.format("%.0f", result.getScore()));
                textRiskCategory.setText(result.getCategory() + " RISK");
                
                // Dynamic Risk Category Color
                int riskColor = Color.parseColor("#059669"); // LOW
                if ("MODERATE".equals(result.getCategory())) riskColor = Color.parseColor("#D97706");
                if ("HIGH".equals(result.getCategory())) riskColor = Color.parseColor("#DC2626");
                if ("CRITICAL".equals(result.getCategory())) riskColor = Color.parseColor("#7F1D1D");
                textSafetyScore.setTextColor(riskColor);
                textRiskCategory.setTextColor(riskColor);

                if (result.getDisclaimer() != null) {
                    textScoreDisclaimer.setText(result.getDisclaimer());
                }

                // Render Safety Score Factors ("Why is my safety score X?")
                containerSafetyFactors.removeAllViews();
                if (result.getFactors() != null && !result.getFactors().isEmpty()) {
                    TextView textHeader = new TextView(MainActivity.this);
                    textHeader.setText("Score Contributing Factors:");
                    textHeader.setTextSize(12);
                    textHeader.setPadding(0, 4, 0, 4);
                    textHeader.setTextColor(Color.parseColor("#64748B"));
                    containerSafetyFactors.addView(textHeader);

                    for (SafetyScoreResponseDto.FactorDetailDto f : result.getFactors()) {
                        TextView item = new TextView(MainActivity.this);
                        item.setText("• " + f.getImpact() + " (-" + f.getWeight() + " pts)");
                        item.setTextSize(12);
                        item.setTextColor(Color.parseColor("#334155"));
                        item.setPadding(8, 2, 0, 2);
                        containerSafetyFactors.addView(item);
                    }
                }
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "Safety score query: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Load Real Live Weather & Environmental Risk
        weatherRepository.getWeather(currentLat, currentLon, new WeatherRepository.ApiCallback<WeatherDataDto>() {
            @Override
            public void onSuccess(WeatherDataDto weather) {
                if (textWeatherStatus != null) {
                    textWeatherStatus.setText(String.format(
                        "🌤 Weather: %s | %.1f°C | Wind: %.1fm/s | Visibility: %dkm",
                        weather.getWeatherCondition(), weather.getTemperature(), weather.getWindSpeed(), (int)(weather.getVisibility() / 1000)
                    ));
                    textWeatherStatus.setTextColor(Color.parseColor("#38BDF8"));
                }
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                if (textWeatherStatus != null) {
                    textWeatherStatus.setText("Weather data unavailable\nLive weather intelligence will appear when weather services are connected.");
                    textWeatherStatus.setTextColor(Color.parseColor("#94A3B8"));
                }
            }
        });

        // Load Threat Summary for Dashboard
        threatRepository.getNearbyThreats(currentLat, currentLon, 5000.0, null, new ThreatRepository.ApiCallback<ThreatDto.ThreatListResponseDto>() {
            @Override
            public void onSuccess(ThreatDto.ThreatListResponseDto result) {
                containerDashboardThreats.removeAllViews();
                if (result.getItems() == null || result.getItems().isEmpty()) {
                    TextView empty = new TextView(MainActivity.this);
                    empty.setText("No active threat alerts in your immediate radius.");
                    empty.setTextColor(Color.parseColor("#64748B"));
                    empty.setTextSize(12);
                    containerDashboardThreats.addView(empty);
                } else {
                    for (ThreatDto t : result.getItems()) {
                        TextView item = new TextView(MainActivity.this);
                        item.setText("⚠ " + t.getCategory() + ": " + t.getTitle() + " (Sev: " + t.getSeverity() + ")");
                        item.setTextColor(Color.parseColor("#DC2626"));
                        item.setTextSize(13);
                        item.setPadding(0, 2, 0, 2);
                        containerDashboardThreats.addView(item);
                    }
                }
            }

            @Override
            public void onError(String errorMessage, int statusCode) {}
        });
    }

    private void checkLocationPermissionState() {
        if (hasLocationPermission()) {
            textLocationStatus.setText(String.format("Location: %.4f, %.4f (Accurate)", currentLat, currentLon));
            btnRequestLocationPermission.setVisibility(View.GONE);
        } else {
            textLocationStatus.setText("Location permission not granted.");
            btnRequestLocationPermission.setVisibility(View.VISIBLE);
            TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.PERMISSION_REQUIRED);
        }
    }

    private void requestLocationPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Access Required")
                .setMessage("Guardian AI requires location access to evaluate your safety score, maintain background threat awareness, and send coordinates during emergency SOS alerts.")
                .setPositiveButton("Grant Access", (dialog, which) -> {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionState();
                Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.PERMISSION_REQUIRED);
            }
        }
    }

    private void submitCurrentLocation() {
        locationRepository.submitLocation(currentLat, currentLon, 5.0f, new LocationRepository.ApiCallback<LocationResponseDto>() {
            @Override
            public void onSuccess(LocationResponseDto result) {
                Toast.makeText(MainActivity.this, "Location telemetry submitted to backend successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "Failed to submit location: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // 2. REAL INTERACTIVE MAP TAB & THREAT FILTERS
    // ==========================================
    private void setMapCategoryFilter(String category) {
        selectedMapCategoryFilter = category;
        btnFilterAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(category.equals("ALL") ? "#0EA5E9" : "#64748B")));
        btnFilterCrime.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(category.equals("CRIME") ? "#0EA5E9" : "#64748B")));
        btnFilterWeather.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(category.equals("WEATHER") ? "#0EA5E9" : "#64748B")));
        btnFilterEnv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(category.equals("ENVIRONMENTAL") ? "#0EA5E9" : "#64748B")));

        renderMapMarkers();
    }

    private void loadMapData() {
        if (mapView == null) return;
        GeoPoint userPoint = new GeoPoint(currentLat, currentLon);
        mapView.getController().setCenter(userPoint);

        threatRepository.getNearbyThreats(currentLat, currentLon, 10000.0, null, new ThreatRepository.ApiCallback<ThreatDto.ThreatListResponseDto>() {
            @Override
            public void onSuccess(ThreatDto.ThreatListResponseDto result) {
                loadedThreats = result.getItems() != null ? result.getItems() : new ArrayList<>();
                renderMapMarkers();
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "Failed to load map threats: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderMapMarkers() {
        if (mapView == null) return;
        mapView.getOverlays().clear();

        // 1. User Current Location Marker
        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(new GeoPoint(currentLat, currentLon));
        userMarker.setTitle("Current Location");
        userMarker.setSnippet("Tracking active");
        mapView.getOverlays().add(userMarker);

        // 2. Active Threat Markers
        for (ThreatDto t : loadedThreats) {
            if (!selectedMapCategoryFilter.equals("ALL") && !t.getCategory().equalsIgnoreCase(selectedMapCategoryFilter)) {
                continue;
            }

            Marker threatMarker = new Marker(mapView);
            threatMarker.setPosition(new GeoPoint(t.getLatitude(), t.getLongitude()));
            threatMarker.setTitle(t.getCategory() + ": " + t.getTitle());
            threatMarker.setSnippet("Severity: " + t.getSeverity() + "/10 | Radius: " + (int) t.getRadius() + "m\n" + (t.getDescription() != null ? t.getDescription() : ""));
            
            threatMarker.setOnMarkerClickListener((marker, mapView) -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("⚠ " + t.getCategory() + " ALERT")
                        .setMessage("Title: " + t.getTitle() + "\n\nSeverity: " + t.getSeverity() + " / 10\nSource: " + t.getSource() + "\nConfidence: " + (int)(t.getConfidence() * 100) + "%\n\nDescription:\n" + (t.getDescription() != null ? t.getDescription() : "No details provided."))
                        .setPositiveButton("OK", null)
                        .show();
                return true;
            });

            mapView.getOverlays().add(threatMarker);
        }

        mapView.invalidate();
    }

    private void showSafeRouteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText editDestination = new EditText(this);
        editDestination.setHint("Enter Destination Address / Landmark");
        editDestination.setPadding(32, 24, 32, 24);

        builder.setTitle("🚦 Route Threat Density Evaluator")
                .setMessage("Enter destination to evaluate candidate routes against live spatial threat data and nearby incidents:")
                .setView(editDestination)
                .setPositiveButton("Evaluate Routes", (dialog, which) -> {
                    String dest = editDestination.getText().toString().trim();
                    if (!ValidationUtils.isNotEmpty(dest)) {
                        Toast.makeText(MainActivity.this, "Please enter a destination.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Estimate destination coordinates slightly offset for corridor query
                    double destLat = currentLat + 0.02;
                    double destLon = currentLon + 0.02;

                    RouteAnalysisRequestDto req = new RouteAnalysisRequestDto(currentLat, currentLon, destLat, destLon, dest);
                    weatherRepository.analyzeRoute(req, new WeatherRepository.ApiCallback<RouteAnalysisResponseDto>() {
                        @Override
                        public void onSuccess(RouteAnalysisResponseDto result) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Destination: ").append(result.getDestinationName()).append("\n");
                            sb.append("Active Regional Threats: ").append(result.getTotalActiveThreatsInRegion()).append("\n\n");

                            for (RouteAnalysisResponseDto.CorridorOptionDto c : result.getCorridors()) {
                                sb.append("• ").append(c.getCorridorName()).append(":\n");
                                sb.append("  Dist: ").append(c.getDistanceKm()).append(" km | Risk: ").append(c.getRiskLevel()).append("\n");
                                sb.append("  Threats: ").append(c.getNearbyThreatCount()).append(" | ").append(c.getDescription()).append("\n\n");
                            }

                            sb.append("Recommended: ").append(result.getRecommendedCorridor());

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Route Threat Corridor Options")
                                    .setMessage(sb.toString())
                                    .setPositiveButton("Select Recommended Route", (d2, w2) -> {
                                        Toast.makeText(MainActivity.this, "Selected: " + result.getRecommendedCorridor() + ". Live protection active.", Toast.LENGTH_SHORT).show();
                                    })
                                    .setNegativeButton("Close", null)
                                    .show();
                        }

                        @Override
                        public void onError(String errorMessage, int statusCode) {
                            Toast.makeText(MainActivity.this, "Route analysis failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================
    // 3. SOS ALERT WITH COUNTDOWN DIALOG
    // ==========================================
    private void showSOSConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sos_countdown, null);
        builder.setView(dialogView);

        TextView textCountdown = dialogView.findViewById(R.id.textSOSCountdown);
        Button btnCancelSOS = dialogView.findViewById(R.id.btnCancelSOS);

        AlertDialog dialog = builder.setCancelable(false).create();
        dialog.show();

        CountDownTimer timer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                textCountdown.setText(String.valueOf(secondsLeft));
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
                dispatchSOSIncident();
            }
        };

        btnCancelSOS.setOnClickListener(v -> {
            timer.cancel();
            dialog.dismiss();
            Toast.makeText(MainActivity.this, "SOS Alert Cancelled.", Toast.LENGTH_SHORT).show();
        });

        timer.start();
    }

    private void dispatchSOSIncident() {
        sosRepository.triggerSOS(currentLat, currentLon, "MANUAL", new SOSRepository.ApiCallback<SOSResponseDto>() {
            @Override
            public void onSuccess(SOSResponseDto result) {
                com.guardianai.services.GuardianFirebaseMessagingService.showEmergencyNotification(
                        MainActivity.this,
                        "🚨 EMERGENCY SOS ACTIVATED",
                        "Emergency incident logged! Guardians alerted via broadcast service."
                );

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("EMERGENCY SOS ACTIVATED")
                        .setMessage("SOS Incident created successfully!\nIncident ID: " + result.getId() + "\nStatus: " + result.getStatus() + "\nBroadcast: Guardian notifications dispatched.")
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "SOS Trigger failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==========================================
    // 4. GUARDIANS TAB
    // ==========================================
    private void loadGuardiansData() {
        containerGuardianList.removeAllViews();
        guardianRepository.listGuardians(new GuardianRepository.ApiCallback<List<GuardianDto>>() {
            @Override
            public void onSuccess(List<GuardianDto> guardians) {
                if (guardians.isEmpty()) {
                    TextView textEmpty = new TextView(MainActivity.this);
                    textEmpty.setText("No trusted guardians added yet.");
                    textEmpty.setPadding(16, 16, 16, 16);
                    containerGuardianList.addView(textEmpty);
                    return;
                }

                for (GuardianDto g : guardians) {
                    View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_guardian, containerGuardianList, false);
                    TextView textName = itemView.findViewById(R.id.textGuardianName);
                    TextView textDetails = itemView.findViewById(R.id.textGuardianDetails);
                    Button btnEdit = itemView.findViewById(R.id.btnEditGuardian);
                    Button btnDelete = itemView.findViewById(R.id.btnDeleteGuardian);

                    textName.setText(g.getName() + " (" + g.getRelationship() + ")");
                    textDetails.setText("Phone: " + g.getPhone() + (g.getEmail() != null ? " | Email: " + g.getEmail() : ""));

                    btnEdit.setOnClickListener(v -> showEditGuardianDialog(g));
                    btnDelete.setOnClickListener(v -> confirmDeleteGuardian(g));

                    containerGuardianList.addView(itemView);
                }
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "Error loading guardians: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddGuardianDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_guardian, null);
        builder.setView(dialogView);

        EditText editName = dialogView.findViewById(R.id.editGuardianName);
        EditText editPhone = dialogView.findViewById(R.id.editGuardianPhone);
        EditText editEmail = dialogView.findViewById(R.id.editGuardianEmail);
        EditText editRelationship = dialogView.findViewById(R.id.editGuardianRelationship);
        CheckBox checkNotify = dialogView.findViewById(R.id.checkGuardianNotify);

        builder.setTitle("Add Trusted Guardian")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String phone = editPhone.getText().toString().trim();
                    String email = editEmail.getText().toString().trim();
                    String rel = editRelationship.getText().toString().trim();

                    if (!ValidationUtils.isNotEmpty(name) || !ValidationUtils.isNotEmpty(phone) || !ValidationUtils.isNotEmpty(rel)) {
                        Toast.makeText(MainActivity.this, "Please fill required fields (Name, Phone, Relationship)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    GuardianCreateRequest req = new GuardianCreateRequest(
                            name, phone, ValidationUtils.isNotEmpty(email) ? email : null, rel, checkNotify.isChecked()
                    );

                    guardianRepository.createGuardian(req, new GuardianRepository.ApiCallback<GuardianDto>() {
                        @Override
                        public void onSuccess(GuardianDto result) {
                            Toast.makeText(MainActivity.this, "Guardian added successfully.", Toast.LENGTH_SHORT).show();
                            loadGuardiansData();
                        }

                        @Override
                        public void onError(String errorMessage, int statusCode) {
                            Toast.makeText(MainActivity.this, "Failed to add guardian: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditGuardianDialog(GuardianDto g) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_guardian, null);
        builder.setView(dialogView);

        EditText editName = dialogView.findViewById(R.id.editGuardianName);
        EditText editPhone = dialogView.findViewById(R.id.editGuardianPhone);
        EditText editEmail = dialogView.findViewById(R.id.editGuardianEmail);
        EditText editRelationship = dialogView.findViewById(R.id.editGuardianRelationship);
        CheckBox checkNotify = dialogView.findViewById(R.id.checkGuardianNotify);

        editName.setText(g.getName());
        editPhone.setText(g.getPhone());
        if (g.getEmail() != null) editEmail.setText(g.getEmail());
        editRelationship.setText(g.getRelationship());
        checkNotify.setChecked(g.isNotificationEnabled());

        builder.setTitle("Edit Guardian")
                .setPositiveButton("Update", (dialog, which) -> {
                    GuardianUpdateRequest req = new GuardianUpdateRequest(
                            editName.getText().toString().trim(),
                            editPhone.getText().toString().trim(),
                            editEmail.getText().toString().trim(),
                            editRelationship.getText().toString().trim(),
                            checkNotify.isChecked()
                    );

                    guardianRepository.updateGuardian(g.getId(), req, new GuardianRepository.ApiCallback<GuardianDto>() {
                        @Override
                        public void onSuccess(GuardianDto result) {
                            Toast.makeText(MainActivity.this, "Guardian updated.", Toast.LENGTH_SHORT).show();
                            loadGuardiansData();
                        }

                        @Override
                        public void onError(String errorMessage, int statusCode) {
                            Toast.makeText(MainActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteGuardian(GuardianDto g) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Guardian")
                .setMessage("Are you sure you want to remove " + g.getName() + " from your guardians?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    guardianRepository.deleteGuardian(g.getId(), new GuardianRepository.ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(MainActivity.this, "Guardian deleted.", Toast.LENGTH_SHORT).show();
                            loadGuardiansData();
                        }

                        @Override
                        public void onError(String errorMessage, int statusCode) {
                            Toast.makeText(MainActivity.this, "Delete failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================
    // 5. INCIDENT HISTORY TAB
    // ==========================================
    private void loadHistoryData() {
        containerHistoryList.removeAllViews();
        sosRepository.listSOSIncidents(0, 20, new SOSRepository.ApiCallback<SOSResponseDto.SOSListResponseDto>() {
            @Override
            public void onSuccess(SOSResponseDto.SOSListResponseDto result) {
                if (result.getItems() == null || result.getItems().isEmpty()) {
                    TextView textEmpty = new TextView(MainActivity.this);
                    textEmpty.setText("No past SOS incidents recorded.");
                    textEmpty.setPadding(16, 16, 16, 16);
                    containerHistoryList.addView(textEmpty);
                    return;
                }

                for (SOSResponseDto sos : result.getItems()) {
                    View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_sos, containerHistoryList, false);
                    TextView textTrigger = itemView.findViewById(R.id.textSOSTrigger);
                    TextView textDetails = itemView.findViewById(R.id.textSOSDetails);

                    textTrigger.setText("SOS: " + sos.getTriggerType() + " | Status: " + sos.getStatus());
                    textDetails.setText(String.format("Location: %.4f, %.4f\nTime: %s", sos.getLatitude(), sos.getLongitude(), sos.getCreatedAt()));

                    containerHistoryList.addView(itemView);
                }
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "Failed to load incident history: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // 6. SETTINGS & PROFILE TAB ARCHITECTURE
    // ==========================================
    private void loadSettingsData() {
        userRepository.getUserProfile(new UserRepository.ApiCallback<UserDto>() {
            @Override
            public void onSuccess(UserDto user) {
                textProfileName.setText(user.getFullName());
                textProfileEmail.setText(user.getEmail());
                textProfilePhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not provided");

                editProfileName.setText(user.getFullName());
                if (user.getPhoneNumber() != null) editProfilePhone.setText(user.getPhoneNumber());
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "Error loading profile: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        updateOfflineQueueCount();
        if (textAppVersionInfo != null) {
            textAppVersionInfo.setText("Installed Version: v" + UpdateManager.getCurrentVersionName(this) + " (Build " + UpdateManager.getCurrentVersionCode(this) + ")");
        }
    }

    private void updateOfflineQueueCount() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int count = AppDatabase.getInstance(MainActivity.this).locationDao().getRecordCount();
                runOnUiThread(() -> textOfflineQueueCount.setText("Room Offline Queue: " + count + " pending record(s)"));
            } catch (Exception e) {
                runOnUiThread(() -> textOfflineQueueCount.setText("Room Offline Queue: Ready"));
            }
        });
    }

    private void saveProfileChanges() {
        String newName = editProfileName.getText().toString().trim();
        String newPhone = editProfilePhone.getText().toString().trim();

        if (!ValidationUtils.isNotEmpty(newName)) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.updateUserProfile(newName, newPhone, new UserRepository.ApiCallback<UserDto>() {
            @Override
            public void onSuccess(UserDto result) {
                Toast.makeText(MainActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                loadSettingsData();
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                Toast.makeText(MainActivity.this, "Failed to update profile: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogout() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        stopService(serviceIntent);
        TrackingStateManager.getInstance().setState(TrackingStateManager.TrackingState.STOPPED);

        TokenManager.getInstance(this).clearToken();
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("⚠ Delete Account & Data")
                .setMessage("Are you sure you want to permanently delete your Guardian AI account, trusted guardians, and location history? This action cannot be undone.")
                .setPositiveButton("Permanently Delete", (dialog, which) -> {
                    userRepository.deleteAccount(new UserRepository.ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(MainActivity.this, "Account and data deleted successfully.", Toast.LENGTH_LONG).show();
                            performLogout();
                        }

                        @Override
                        public void onError(String errorMessage, int statusCode) {
                            Toast.makeText(MainActivity.this, "Failed to delete account: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performUpdateCheck() {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Checking for updates...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        UpdateManager.checkForUpdates(this, new UpdateManager.UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(UpdateManager.UpdateInfo info, int currentVersionCode) {
                progressDialog.dismiss();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("🎉 Update Available! (v" + info.getVersionName() + ")")
                        .setMessage("A new version of Guardian AI is available.\n\nCurrent Version: Build " + currentVersionCode + "\nLatest Version: Build " + info.getVersionCode() + "\n\nChangelog:\n" + info.getChangelog())
                        .setPositiveButton("Update Now", (dialog, which) -> downloadAndInstallUpdate(info.getDownloadUrl()))
                        .setNegativeButton("Later", null)
                        .show();
            }

            @Override
            public void onNoUpdateAvailable(int currentVersionCode) {
                progressDialog.dismiss();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Up to Date")
                        .setMessage("You are using the latest version of Guardian AI (Build " + currentVersionCode + ").")
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Check updates failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void downloadAndInstallUpdate(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            Toast.makeText(this, "Invalid download URL.", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.ProgressDialog downloadProgress = new android.app.ProgressDialog(this);
        downloadProgress.setTitle("Downloading Update");
        downloadProgress.setMessage("Please wait while the update is being downloaded...");
        downloadProgress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        downloadProgress.setIndeterminate(false);
        downloadProgress.setMax(100);
        downloadProgress.setCancelable(false);
        downloadProgress.show();

        UpdateManager.downloadAndInstallApk(this, downloadUrl, new UpdateManager.ProgressCallback() {
            @Override
            public void onProgress(int progress, long downloadedBytes, long totalBytes) {
                if (progress >= 0) {
                    downloadProgress.setProgress(progress);
                    downloadProgress.setMessage(String.format("Downloading update... %d%% (%d MB / %d MB)", progress, downloadedBytes / (1024 * 1024), totalBytes / (1024 * 1024)));
                } else {
                    downloadProgress.setMessage(String.format("Downloading update... (%d MB downloaded)", downloadedBytes / (1024 * 1024)));
                }
            }

            @Override
            public void onDownloadComplete(java.io.File apkFile) {
                downloadProgress.dismiss();
                Toast.makeText(MainActivity.this, "Download complete! Opening installer...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                downloadProgress.dismiss();
                Toast.makeText(MainActivity.this, "Download failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
