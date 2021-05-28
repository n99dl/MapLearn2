package com.n99dl.maplearn.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.n99dl.maplearn.Adapter.QuestAdapter;
import com.n99dl.maplearn.Logic.DatabaseKey;
import com.n99dl.maplearn.R;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.Model.Quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DoneQuestListFragment extends Fragment {

    private RecyclerView recyclerView;

    private QuestAdapter questAdapter;
    private List<Quest> mQuests;
    private List<Long> questDoneIds;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_done_quest_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mQuests = new ArrayList<>();
        questDoneIds = new ArrayList<>();


        readUndoneQuest();

        questDoneIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("done_quest").child(GameManager.getInstance().getPlayer().getId());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questDoneIds.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    Long id = (Long)hashMap.get("id");
                    questDoneIds.add(id);
                }
                readUndoneQuest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    private void readUndoneQuest() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_QUEST_SET).child("set_1");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mQuests.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Quest quest = snapshot.getValue(Quest.class);
                    boolean appearing = false;
                    for (Long questId: questDoneIds) {
                        if (quest.getId() == questId) {
                            appearing = true;
                            break;
                        }
                    }

                    if (appearing)
                        mQuests.add(quest);
                }

                questAdapter = new QuestAdapter(getContext(), mQuests);
                recyclerView.setAdapter(questAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
