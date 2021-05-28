package com.n99dl.maplearn.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.n99dl.maplearn.Fragments.MainFragmentManager;
import com.n99dl.maplearn.Fragments.QuestFragment;
import com.n99dl.maplearn.Logic.DatabaseKey;
import com.n99dl.maplearn.Logic.DefaultValue;
import com.n99dl.maplearn.MapObject.PlayerMarker;
import com.n99dl.maplearn.MapObject.QuestMarker;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.MapObject.MarkerType;
import com.n99dl.maplearn.Logic.MyLocation;
import com.n99dl.maplearn.Logic.Player;
import com.n99dl.maplearn.Model.Quest;
import com.n99dl.maplearn.Model.User;
import com.n99dl.maplearn.R;
import com.n99dl.maplearn.utilities.GameLogic;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public enum MapMode {
        NORMAL_VIEW,
        NEARBY_PLAYER_VIEW
    }

    public static final int FASTEST_UPDATE_INTERVAL = 2;
    private static final int DEFAULT_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private ActionBar actionBar;
    private Toolbar tb_normal, tb_nearby_quest;
    private GoogleMap mMap;
    private SupportMapFragment googleSupportMapFragment;
    private Fragment mapFragment;
    private MapMode mapMode;
    private DatabaseReference reference, currentUserReference;
    private FirebaseUser firebaseUser;
    private LocationManager manager;

    //UI
    private LinearLayout control_btn_layout;
    private CircleImageView iw_profile_image;
    private TextView tv_username, tv_nearby_quest_guide;
    private ImageButton btn_back, btn_nav, btn_add_friend;
    private BottomNavigationView bottom_navigation;
    private RelativeLayout splash_layout;

    private boolean isRequestCameraRecenter = true;
    private final int MIN_TIME = 1000;
    private final int MIN_DISTANCE = 0;
    private boolean isAQuestDone;

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

        Toast.makeText(this, "Create this activity", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_maps);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert googleSupportMapFragment != null;
        googleSupportMapFragment.getMapAsync(this);

        questMarkerList = new ArrayList<>();
        playerMarkerList = new ArrayList<>();

        //Ui and button
        loadUI();

        //end ui

        updateGPS();
        startLocationUpdate();

    }

    private Fragment selectingFragment;
    private void loadUI() {
        control_btn_layout = findViewById(R.id.control_btn_layout);
        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                if (menuItem.getItemId() == R.id.nav_home) {
                    control_btn_layout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show(mapFragment)
                            .commit();

                } else {
                    control_btn_layout.setVisibility(View.GONE);
                    getSupportFragmentManager().beginTransaction()
                            .hide(mapFragment)
                            .commit();
                }
                switch (menuItem.getItemId()) {
                    case R.id.nav_profile:
                        selectedFragment = MainFragmentManager.getInstance().getProfileFragment();
                        break;
                    case R.id.nav_home:
                        selectedFragment = MainFragmentManager.getInstance().getEmptyFragment();
                        break;
                    case R.id.nav_social:
                        selectedFragment = MainFragmentManager.getInstance().getSocialFragment();
                        break;
                    case R.id.nav_quest:
                        selectedFragment = new QuestFragment();
                        break;
                }
                selectingFragment = selectedFragment;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                return true;
            }
        });
        bottom_navigation.setVisibility(View.GONE);

        iw_profile_image = findViewById(R.id.iw_profile_image);
        tv_username = findViewById(R.id.tv_username);
        btn_nav = findViewById(R.id.btn_nav);
        tv_nearby_quest_guide = findViewById(R.id.tv_nearby_quest_guide);
        btn_nav.setVisibility(View.VISIBLE);
        btn_add_friend = findViewById(R.id.btn_add_friend);
        btn_add_friend.setVisibility(View.GONE);
        tv_nearby_quest_guide.setVisibility(View.GONE);
        splash_layout = findViewById(R.id.splash_layout);

        btn_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Player player = GameManager.getInstance().getPlayer();
                if (mMap != null && player != null) {

                    MyLocation myLocation = player.getLocation();
                    LatLng me = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(me)
                            .zoom(16.0f)
                            .bearing(0)
                            .tilt(25)
                            .build();
                    int duration = 1000; //1sec
                    CameraUpdate center = CameraUpdateFactory.newCameraPosition(cameraPosition);
                    mMap.animateCamera(center, duration, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                    Toast.makeText(MapsActivity.this, "move cam", Toast.LENGTH_SHORT).show();
                } else if (mMap == null) {
                    Toast.makeText(MapsActivity.this, "map null", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "no player", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, FindUserActivity.class);
                startActivity(intent);
            }
        });
        iw_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
                intent.putExtra("profileType","user");
                startActivity(intent);
            }
        });
    }
    private void readUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserReference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_USER).child(uid);
        currentUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        User user = snapshot.getValue(User.class);
                        Player player = new Player(user);
                        GameManager.getInstance().setPlayer(player);
                        tv_username.setText(player.getUsername());

                        readLocationChanges();
                        if (player.getImageURL().equals("default")) {
                            Toast.makeText(MapsActivity.this, "default image", Toast.LENGTH_SHORT).show();
                            iw_profile_image.setImageResource(R.mipmap.ic_profile_default);
                        } else {
                            //iw_profile_image.setImageResource(R.mipmap.ic_profile_default);
                            Glide.with(MapsActivity.this).load(player.getImageURL()).into(iw_profile_image);
                        }
                        //buffer time
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideSplashScreen();
                            }
                        }, 2000);
                        //Toast.makeText(MapsActivity.this, "Logged In, UID: " + player.getId(), Toast.LENGTH_SHORT).show();
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

    private void hideSplashScreen() {
        splash_layout.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.VISIBLE);
    }

    public QuestMarker getQuestMarker(long id) {
        for (QuestMarker questMarker: questMarkerList) {
            if (questMarker.getId() == id)
                return questMarker;
        }
        return null;
    }
    private void startLocationUpdate() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult!= null) {
                    super.onLocationResult(locationResult);
                    saveLocation(locationResult.getLastLocation());
                }

            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }
    private void updateGPS() {
        //get permission from user to track gps
        //get current loc from fused client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission provided
            Log.d("gps", "updateGPS: permission granted");
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null)
                    saveLocation(location);
                    else
                        Log.d("gps", "updateGPS: null location");
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }
    private void readLocationChanges() {
        reference = FirebaseDatabase.getInstance().getReference(DatabaseKey.KEY_LOCATION).child(GameManager.getInstance().getPlayer().getId());
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
                        Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Marker marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.quest_mark))
                    .position(questPosition).title(questName));
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
        uiOnMapReady();
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        // Add player marker at default position
        addPlayerMarker();
        //load quest marker on map
        loadQuest();
    }
    private void addPlayerMarker() {

        LatLng me = new LatLng(DefaultValue.PLAYER_DEFAULT_LAT, DefaultValue.PLAYER_DEFAULT_LONG);
        myMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.player_marker)).position(me).title("Me"));
        MarkerType markerType = new MarkerType(MarkerType.Type.PLAYER);
        myMarker.setTag(markerType);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
    }
    private void uiOnMapReady() {
        //Additional UI
        tb_normal = (Toolbar) findViewById(R.id.toolbar);
        //tb_normal.setVisibility(View.GONE);
        tb_nearby_quest = findViewById(R.id.tb_nearby_quest);
        tb_nearby_quest.setVisibility(View.GONE);
        btn_back = findViewById(R.id.btn_back);
        if (tb_normal == null) {
            Toast.makeText(this, "No tool bar", Toast.LENGTH_SHORT).show();
        } else {
            setSupportActionBar(tb_normal);
            actionBar = getSupportActionBar();
            actionBar.setTitle("");
        }
    }
    private void saveLocation(Location location) {
        Log.d("gps", "updateGPS: location changed");
        if (GameManager.getInstance().getPlayer() != null) {
            reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_LOCATION).child(GameManager.getInstance().getPlayer().getId());
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

    public void showInQuestPlayers(final Quest quest) {
        if (mapMode == MapMode.NEARBY_PLAYER_VIEW) return;
//        actionBar.setTitle("Nearby players");
//        actionBar.setDisplayHomeAsUpEnabled(true);
        tv_nearby_quest_guide.setVisibility(View.VISIBLE);
        bottom_navigation.setVisibility(View.GONE);
        if (tb_normal != null)
        tb_normal.setVisibility(View.GONE);
        if (tb_nearby_quest != null)
        tb_nearby_quest.setVisibility(View.VISIBLE);
        btn_add_friend.setVisibility(View.VISIBLE);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameManager.getInstance().changMode(GameManager.Mode.MAP_VIEW);
                Intent intent = new Intent(MapsActivity.this, QuestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                setBackToMapView();
            }
        });
        mapMode = MapMode.NEARBY_PLAYER_VIEW;
        playerMarkerList.clear();
        reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_LOCATION);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    MyLocation location = dataSnapshot.getValue(MyLocation.class);
                    assert location != null;
                    if (GameLogic.isLocationInQuestRadius(quest, location)) {
                        String id = dataSnapshot.getKey();
                        assert id != null;
                        if (!id.equals(GameManager.getInstance().getPlayer().getId())) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Marker newMaker = mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.other_player_mark))
                                    .position(latLng)
                                    .title("Other_PLayer"));
                            MarkerType markerType = new MarkerType(MarkerType.Type.OTHER_PLAYER);
                            newMaker.setTag(markerType);
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
        if (mMap != null)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(questBounds.getCenter(), 17.0f));
    }

    private void removeAllOtherPlayerMarker() {
        for (PlayerMarker playerMarker: playerMarkerList) {
            playerMarker.getMarker().remove();
        }
        playerMarkerList.clear();
    }

    private void setBackToMapView() {
        if (actionBar != null) {
            removeAllOtherPlayerMarker();
            this.mapMode = MapMode.NORMAL_VIEW;
            bottom_navigation.setVisibility(View.VISIBLE);
            tb_normal.setVisibility(View.VISIBLE);
            tb_nearby_quest.setVisibility(View.GONE);
            tv_nearby_quest_guide.setVisibility(View.GONE);
            //actionBar.setTitle("");
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
        }
        btn_add_friend.setVisibility(View.GONE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MarkerType markerType = (MarkerType)marker.getTag();
        if (markerType == null) return false;
        if (markerType.getType() == MarkerType.Type.QUEST && mapMode == MapMode.NORMAL_VIEW) {
            questClicked(markerType.getQuest());
        }
        return true;
    }

    private void questClicked(Quest quest) {
        if (GameLogic.isInQuestRadius(quest, GameManager.getInstance().getPlayer())) {
            GameManager.getInstance().setSelectingQuest(quest);
            Intent intent = new Intent(this, QuestActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You have to be within the quest circle to do this quest", Toast.LENGTH_LONG).show();
        }
    }

    protected void onResume() {
        super.onResume();
        if (GameManager.getInstance().getGameMode() == GameManager.Mode.NEARBY_PLAYER_QUEST) {
            showInQuestPlayers(GameManager.getInstance().getSelectingQuest());
        } else {
            setBackToMapView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_res, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.it_logout:
                GameManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                finish();
                return true;
        }
        return false;
    }
    public void showQuestDonePopupWPopUp() {
        findViewById(R.id.map_activity_root).post(new Runnable() {
            @Override
            public void run() {
                showQuestDonePopupWindowClick(findViewById(R.id.map_activity_root));
            }
        });
    }
    public void showQuestDonePopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.quest_done_popup_window, null);
        TextView tv_pop_up = popupView.findViewById(R.id.tv_pop_up);
        tv_pop_up.setText("Congratulation! Your quest in " + GameManager.getInstance().getSelectingQuest().getName() + " is done!");

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
}
