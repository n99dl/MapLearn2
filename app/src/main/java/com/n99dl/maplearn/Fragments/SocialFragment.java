package com.n99dl.maplearn.Fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.n99dl.maplearn.Adapter.UserAdapter;
import com.n99dl.maplearn.Logic.DatabaseKey;
import com.n99dl.maplearn.R;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.Logic.Player;
import com.n99dl.maplearn.Model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SocialFragment extends Fragment {

    private CircleImageView civ_search;
    private EditText et_search_user;

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        GameManager.getInstance().clearSelectingQuest();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        civ_search = view.findViewById(R.id.civ_search);
        et_search_user = view.findViewById(R.id.et_search_user);

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
        return view;
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
                    if (!user.getId().equals(player.getId())) {
                        mUsers.add(user);
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
