package com.n99dl.maplearn.Activities;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.n99dl.maplearn.Logic.DatabaseKey;
import com.n99dl.maplearn.Notification.APIService;
import com.n99dl.maplearn.Notification.Client;
import com.n99dl.maplearn.Notification.Data;
import com.n99dl.maplearn.Notification.MyResponse;
import com.n99dl.maplearn.Notification.Sender;
import com.n99dl.maplearn.Notification.Token;
import com.n99dl.maplearn.Model.WaveDetails;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.Logic.MyLocation;
import com.n99dl.maplearn.Model.User;
import com.n99dl.maplearn.R;
import com.n99dl.maplearn.utilities.GameLogic;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView username;
    private Button btn_wave;

    DatabaseReference reference;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    private enum ProfileMode {
        READ,
        EDIT
    };
    private ProfileActivity.ProfileMode mode;
    private boolean isFullNameSet;

    private TextView tv_username, tv_questDoneCount, tv_quizDoneCount, tv_email, tv_edit_profilePic, tv_fullname_title;
    private CircleImageView iv_profile_image;
    private EditText et_fullname;
    private ImageButton btn_settings;

    Intent intent;

    APIService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MapsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        profileImage = findViewById(R.id.iv_profile_image);
        username = findViewById(R.id.tv_username);
        btn_wave = findViewById(R.id.btn_wave);
        tv_questDoneCount = findViewById(R.id.tv_questDoneCount);
        tv_quizDoneCount = findViewById(R.id.tv_quizDoneCount);
        tv_email = findViewById(R.id.tv_email);
        iv_profile_image = findViewById(R.id.iv_profile_image);
        et_fullname = findViewById(R.id.et_fullname);
        tv_edit_profilePic = findViewById(R.id.tv_edit_profilePic);
        tv_fullname_title = findViewById(R.id.tv_fullname_title);
        btn_settings = findViewById(R.id.btn_settings);


        intent = getIntent();
        String userId = intent.getStringExtra("userid");
        final String profileType = intent.getStringExtra("profileType");
        if (profileType.equals("user")) {
            userId = GameManager.getInstance().getPlayer().getId();
        }

        storageReference = FirebaseStorage.getInstance().getReference("profile_image_uploads");

        processWaveFriendButton(GameManager.getInstance().getPlayer().getId(), userId);


        reference = FirebaseDatabase.getInstance().getReference(DatabaseKey.KEY_USER).child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                getSupportActionBar().setTitle(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_profile_default);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }
                tv_email.setText(user.getEmail());
                if (!user.getFullname().equals("default")) {
                    et_fullname.setText(user.getFullname());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference questReference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_DONE_QUEST).child(userId);
        questReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tv_questDoneCount.setText("" + snapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference quizReference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_QUIZ_HIGHSCORE).child(userId);
        quizReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tv_quizDoneCount.setText("" + snapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        et_fullname.setEnabled(false);
        et_fullname.setInputType(InputType.TYPE_NULL);
        btn_settings.setVisibility(View.GONE);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }

    @SuppressLint("NewApi")
    private void waveAt(String userid, String friendId, long time) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("user", userid);
        hashMap.put("friend", friendId);
        hashMap.put("time", time);
        String key = userid + "," + friendId;
        reference.child(DatabaseKey.KEY_WAVE_LIST).child(key).setValue(hashMap);
        reference = FirebaseDatabase.getInstance().getReference(DatabaseKey.KEY_LOCATION).child(friendId);
        //check location of this player, by get it from the databse
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyLocation location = snapshot.getValue(MyLocation.class);
                if (GameManager.getInstance().getSelectingQuest() != null)
                {
                    if (GameLogic.isLocationInQuestRadius(GameManager.getInstance().getSelectingQuest(), location)) {
                        GameManager.getInstance().markQuestAsDone();
                        findViewById(R.id.profile_activity_root).post(new Runnable() {
                            @Override
                            public void run() {
                                showQuestDonePopupWindowClick(findViewById(R.id.profile_activity_root));
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sendNotification(friendId, GameManager.getInstance().getPlayer().getUsername(), "wave");
        btn_wave.setEnabled(false);
        btn_wave.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
    }

    private void doneQuest() {
        //update quest
//        GameManager.getInstance().clearSelectingQuest();
        Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void processWaveFriendButton(final String userid, final String friendId) {
        DatabaseReference reference ;
        String key = userid + "," + friendId;
        reference =  FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_WAVE_LIST).child(key);
        final long[] time = {0};
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    WaveDetails waveDetails = dataSnapshot.getValue(WaveDetails.class);

                    if (userid.equals(waveDetails.getUser()) && friendId.equals(waveDetails.getFriend())) {
                        time[0] = waveDetails.getTime();
                        btn_wave.setText("   Wave ! (" + time[0] + " already!)      ");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btn_wave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waveAt(userid, friendId, time[0] + 1);
                Toast.makeText(ProfileActivity.this, "Wave success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference(DatabaseKey.KEY_USER).child(GameManager.getInstance().getPlayer().getId());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL",mUri);
                        reference.updateChildren(hashMap);

                        pd.dismiss();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ProfileActivity.this, "Uploading in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
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
                doneQuest();
                return true;
            }
        });
    }

    private void sendNotification(final String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Token token =  snapshot.getValue(Token.class);
                    Data data = new Data(GameManager.getInstance().getPlayer().getId(), R.drawable.ic_message_notify , username + ": " + message,
                            "Wave !", receiver);
                    Sender sender = new Sender(data, token.getToken());
                    Log.d("test noti", "onDataChange: " + token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    Log.d("test bug", "onResponse: " + response.code() + message);
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Log.d("test bug", "onResponse: success");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable throwable) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}