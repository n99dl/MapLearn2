package com.n99dl.maplearn;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.n99dl.maplearn.MapObject.PlayerMarker;
import com.n99dl.maplearn.MapObject.QuestMarker;
import com.n99dl.maplearn.data.GameManager;
import com.n99dl.maplearn.data.MarkerType;
import com.n99dl.maplearn.data.MyLocation;
import com.n99dl.maplearn.data.Quest;
import com.n99dl.maplearn.data.User;
import com.n99dl.maplearn.utilities.GameLogic;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    enum MapMode {
        NORMAL_VIEW,
        NEARBY_PLAYER_VIEW
    }

    public static final int FASTEST_UPDATE_INTERVAL = 2;
    private static final int DEFAULT_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private GoogleMap mMap;
    private MapMode mapMode;
    private DatabaseReference reference, currentUserReference;
    private FirebaseUser firebaseUser;
    private LocationManager manager;


    private Button btn_logout;
    private boolean isRequestCameraRecenter = true;
    private final int MIN_TIME = 1000;
    private final int MIN_DISTANCE = 0;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    //Marker list for quests
    private List<QuestMarker> questMarkerList;
    private List<PlayerMarker> playerMarkerList;

    Marker myMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GameManager.getInstance().setMapsActivity(this);
        mapMode = MapMode.NORMAL_VIEW;
        readUser();

        setContentView(R.layout.activity_maps);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                saveLocation(locationResult.getLastLocation());
            }
        };

        questMarkerList = new ArrayList<>();
        playerMarkerList = new ArrayList<>();

        btn_logout = findViewById(R.id.btn_logout);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                finish();
            }
        });

        updateGPS();
        readLocationChanges();
        startLocationUpdate();
    }

    private void readUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        User player = snapshot.getValue(User.class);
                        GameManager.getInstance().setPlayer(player);
                        Toast.makeText(MapsActivity.this, "Logged In, UID: " + player.getId(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public QuestMarker getQuestMarker(long id) {
        for (QuestMarker questMarker: questMarkerList) {
            if (questMarker.getId() == id)
                return questMarker;
        }
        return null;
    }
    private void startLocationUpdate() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }
    private void updateGPS() {
        //get permission from user to track gps
        //get current loc from fused client
        //update UI
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission provided
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    saveLocation(location);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }
    private void readLocationChanges() {
        reference = currentUserReference.child("location");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if (location != null) {
                            myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                            GameManager.getInstance().getPlayer().setLocation(location);
                            if (isRequestCameraRecenter) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                                isRequestCameraRecenter = false;
                            }
                        }
                    } catch (Exception e) {
                        //Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void loadQuest() {
        if (mMap == null) return;
        List<Quest> questList = GameManager.getInstance().getAvailableQuest();
        if (questList.size() > questMarkerList.size())
        {
            for (QuestMarker questMarker: questMarkerList) {
                questMarker.selfRemoveFormMap();
            }
        }
        for (Quest quest: questList) {
            LatLng questPosition = new LatLng(quest.getLatitude(), quest.getLongitude());
            String questName = quest.getName();
            Marker marker = mMap.addMarker(new MarkerOptions().position(questPosition).title(questName));
            MarkerType markerType = new MarkerType(MarkerType.Type.QUEST);
            markerType.setQuest(quest);
            marker.setTag(markerType);
            Circle circle = mMap.addCircle(new CircleOptions()
                            .center(questPosition)
                            .radius(GameLogic.MIN_DISTANCE_TO_QUEST)
                            .strokeColor(Color.GREEN)
                            .fillColor((865730457)));
            QuestMarker questMarker = new QuestMarker(marker, circle, quest.getId());
            questMarkerList.add(questMarker);
            Color color = new Color();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        // Add a marker in Sydney and move the camera
        LatLng me = new LatLng(21.03730791971016, 105.78235822524783);
        myMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(me).title("Me"));
        MarkerType markerType = new MarkerType(MarkerType.Type.PLAYER);
        //markerType.setPlayer(quest);
        myMarker.setTag(markerType);
        //mMap.setMinZoomPreference(12);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
        loadQuest();
    }

    private void saveLocation(Location location) {
        currentUserReference.child("location").setValue(location);
        if (GameManager.getInstance().getPlayer() != null) {
            reference = FirebaseDatabase.getInstance().getReference().child("all_users_locations").child(GameManager.getInstance().getPlayer().getId());
            MyLocation myLocation = new MyLocation(location.getLatitude(), location.getLongitude());
            reference.setValue(myLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                }
                else {
                    Toast.makeText(this, "This app require permission ro be granted to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void questClicked(Quest quest) {
        if (GameLogic.isInQuestRadius(quest, GameManager.getInstance().getPlayer())) {
//            showInQuestPlayers(quest);
            Intent intent = new Intent(this, QuestActivity.class);
            intent.putExtra("questId", quest.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "You have to be within the quest circle to do this quest", Toast.LENGTH_LONG).show();
        }
    }

    public void showInQuestPlayers(final Quest quest) {
        if (mapMode == MapMode.NEARBY_PLAYER_VIEW) return;
        mapMode = MapMode.NEARBY_PLAYER_VIEW;
        playerMarkerList.clear();
        reference = FirebaseDatabase.getInstance().getReference().child("all_users_locations");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    MyLocation location = dataSnapshot.getValue(MyLocation.class);
                    if (GameLogic.isLocationInQuestRadius(quest, location)) {
                        String id = dataSnapshot.getKey();
                        if (!id.equals(GameManager.getInstance().getPlayer().getId())) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Marker newMaker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).position(latLng).title("Me"));
                            MarkerType markerType = new MarkerType(MarkerType.Type.OTHER_PLAYER);
                            newMaker.setTag(markerType);
                            Log.d("quest test: ", "" + id);
                            PlayerMarker playerMarker = new PlayerMarker(newMaker, id);
                            playerMarkerList.add(playerMarker);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        LatLngBounds questBounds = new LatLngBounds(
                new LatLng(quest.getLatitude() - GameLogic.MIN_DISTANCE_TO_QUEST_IN_LATLONG, quest.getLongitude() - GameLogic.MIN_DISTANCE_TO_QUEST_IN_LATLONG),
                new LatLng(quest.getLatitude() + GameLogic.MIN_DISTANCE_TO_QUEST_IN_LATLONG, quest.getLongitude() + GameLogic.MIN_DISTANCE_TO_QUEST_IN_LATLONG)
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(questBounds.getCenter(), 17.0f));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MarkerType markerType = (MarkerType)marker.getTag();
        if (markerType.getType() == MarkerType.Type.QUEST) {
            Toast.makeText(this, "Quest clicked", Toast.LENGTH_SHORT).show();
            questClicked(markerType.getQuest());
        } else {
            Toast.makeText(this, "Not a quest clicked", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (GameManager.getInstance().getGameMode() == GameManager.Mode.NEARBY_PLAYER_QUEST) {
            showInQuestPlayers(GameManager.getInstance().getSelectingQuest());
        }
    }

}
