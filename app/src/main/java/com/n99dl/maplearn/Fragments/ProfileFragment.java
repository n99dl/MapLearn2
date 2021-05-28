package com.n99dl.maplearn.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.n99dl.maplearn.Logic.DatabaseKey;
import com.n99dl.maplearn.R;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.Logic.Player;
import com.n99dl.maplearn.Model.User;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private TextView tv_username, tv_questDoneCount, tv_quizDoneCount, tv_email, tv_edit_profilePic, tv_fullname_title;
    private CircleImageView iv_profile_image;
    private EditText et_fullname;
    private ImageButton btn_settings;
    DatabaseReference reference;
    private enum ProfileMode {
        READ,
        EDIT
    };
    private ProfileMode mode;
    private boolean isFullNameSet;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mode = ProfileMode.READ;
        tv_username = view.findViewById(R.id.tv_username);
        tv_questDoneCount = view.findViewById(R.id.tv_questDoneCount);
        tv_quizDoneCount = view.findViewById(R.id.tv_quizDoneCount);
        tv_email = view.findViewById(R.id.tv_email);
        iv_profile_image = view.findViewById(R.id.iv_profile_image);
        et_fullname = view.findViewById(R.id.et_fullname);
        tv_edit_profilePic = view.findViewById(R.id.tv_edit_profilePic);
        tv_fullname_title = view.findViewById(R.id.tv_fullname_title);
        btn_settings = view.findViewById(R.id.btn_settings);

        Player player = GameManager.getInstance().getPlayer();
        tv_username.setText(player.getUsername());
        tv_email.setText(player.getEmail());
        tv_questDoneCount.setText(String.valueOf(player.getTotalQuestDone()));
        tv_quizDoneCount.setText(String.valueOf(player.getTotalQuizDone()));
        if (!player.getFullname().equals("default")) {
            isFullNameSet = true;
            et_fullname.setText(player.getFullname());
        } else isFullNameSet = false;

        storageReference = FirebaseStorage.getInstance().getReference("profile_image_uploads");

        DatabaseReference image_reference = FirebaseDatabase.getInstance().getReference()
                .child(DatabaseKey.KEY_USER)
                .child(player.getId());
        image_reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                if (user.getImageURL().equals("default")) {
                    iv_profile_image.setImageResource(R.mipmap.ic_profile_default);
                } else {
                    Glide.with(Objects.requireNonNull(getContext()))
                            .load(user.getImageURL())
                            .into(iv_profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        et_fullname.setEnabled(false);
        et_fullname.setInputType(InputType.TYPE_NULL);

        reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_USER).child(player.getId()).child("fullname");
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMode();
            }
        });
        iv_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        iv_profile_image.setEnabled(false);
        return view;
    }

    private void changeMode() {
        if (mode == ProfileMode.READ) {
            iv_profile_image.setEnabled(true);
            tv_edit_profilePic.setVisibility(View.VISIBLE);
            tv_fullname_title.setText("Full name (tap text bellow to edit)");
            et_fullname.setEnabled(true);
            et_fullname.setInputType(InputType.TYPE_CLASS_TEXT);
            btn_settings.setImageResource(R.drawable.ic_check);
            if (!isFullNameSet) {
                et_fullname.setHint("tap to edit");
                et_fullname.setText("");
            }
            mode = ProfileMode.EDIT;
        } else {
            iv_profile_image.setEnabled(false);
            String fullnameText = et_fullname.getText().toString();
            if (fullnameText.equals("")) {
                Toast.makeText(getContext(), "Please enter your full name!", Toast.LENGTH_LONG).show();
            } else {
                isFullNameSet = true;
                reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_USER).child(GameManager.getInstance().getPlayer().getId()).child("fullname");
                reference.setValue(fullnameText);
                tv_edit_profilePic.setVisibility(View.GONE);
                tv_fullname_title.setText("Full name");
                et_fullname.setEnabled(false);
                et_fullname.setInputType(InputType.TYPE_NULL);
                btn_settings.setImageResource(R.drawable.ic_settings);
                mode = ProfileMode.READ;
            }
        }
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(getContext());
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

                        reference = FirebaseDatabase.getInstance().getReference(DatabaseKey.KEY_USER)
                                .child(GameManager.getInstance().getPlayer().getId());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL",mUri);
                        reference.updateChildren(hashMap);

                        pd.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Uploading in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }
}
