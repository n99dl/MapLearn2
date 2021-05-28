package com.n99dl.maplearn.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.n99dl.maplearn.Adapter.UserAdapter;
import com.n99dl.maplearn.Logic.DatabaseKey;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.Logic.Player;
import com.n99dl.maplearn.Model.User;
import com.n99dl.maplearn.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUserActivity extends AppCompatActivity {

    private CircleImageView civ_search;
    private EditText et_search_user;

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add friend");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(FindUserActivity.this));

        civ_search = findViewById(R.id.civ_search);
        et_search_user = findViewById(R.id.et_search_user);

        mUsers = new ArrayList<>();

        et_search_user.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    //Perform your Actions here.
                    searchUser(et_search_user.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });

        civ_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser(et_search_user.getText().toString());
            }
        });
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                Intent intent = new Intent(FindUserActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }

    private void searchUser(String key) {
        final Player player = GameManager.getInstance().getPlayer();
        Query query = FirebaseDatabase.getInstance().getReference(DatabaseKey.KEY_USER).orderByChild("username")
                .startAt(key)
                .endAt(key);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert player != null;

                    if (!user.getId().equals(player.getId())) {
                        mUsers.add(user);
                    }
                }

                userAdapter = new UserAdapter(FindUserActivity.this, mUsers);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
