package com.example.criinfo.More.TournamentsTabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.criinfo.More.MyTeamTabs.MyTeamMatch;
import com.example.criinfo.More.Schedule;
import com.example.criinfo.R;
import com.example.criinfo.Utils.Utils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TournamentsScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TournamentsScheduleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    SharedPreferences sharedPreferences;
    FirestoreRecyclerAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    ProgressBar progressBar;
    String tournamentId, userId;

    public TournamentsScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TournamentsScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TournamentsScheduleFragment newInstance(String param1, String param2) {
        TournamentsScheduleFragment fragment = new TournamentsScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_tournaments_schedule, container, false);

        recyclerView = view.findViewById(R.id.tournamentShowSchedule);

        sharedPreferences = getContext().getSharedPreferences(Utils.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        tournamentId = sharedPreferences.getString("tournamentId", "");

        final Query query = FirebaseFirestore.getInstance()
                .collection("schedule").whereEqualTo("LeagueId",tournamentId);


        FirestoreRecyclerOptions<Schedule> options = new FirestoreRecyclerOptions.Builder<Schedule>()
                .setQuery(query, Schedule.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Schedule ,MatchHolder>(options) {
            @Override
            public void onBindViewHolder(final MatchHolder holder, final int position, final Schedule model) {


                    holder.league.setText(model.getMatchType());
                    holder.date.setText(model.getMatchDate());

                    if(model.getWinner().isEmpty())
                    {
                        holder.winner.setText("Result is Yet to be decalared");
                    }
                    else
                    {
                        holder.winner.setText(model.getWinner());
                    }
                holder.team1score.setText(model.getTeam1score());
                holder.team2score.setText(model.getTeam2score());

                    db.collection("tournaments").document(model.getLeagueId())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                                }
                            });


                    db.collection("teams").document(model.getTeam1())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    holder.team1.setText(task.getResult().getString("sortname"));
                                    Glide.with(getActivity()).load(task.getResult().getString("image"))
                                            .placeholder(R.drawable.team)
                                            .into(holder.oneImage);
                                }
                            });

                    db.collection("teams").document(model.getTeam2())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    holder.team2.setText(task.getResult().getString("sortname"));
                                    Glide.with(getActivity()).load(task.getResult().getString("image"))
                                            .placeholder(R.drawable.team)
                                            .into(holder.twoImage);
                                }
                            });


            }

            @Override
            public MatchHolder onCreateViewHolder(ViewGroup group, int i) {
                // Using a custom layout called R.layout.message for each item, we create a new instance of the viewholder
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.tournamentschedulelist, group, false);
                return new MatchHolder(view);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private class MatchHolder extends RecyclerView.ViewHolder {
        TextView team1, team2, date, league,winner,team1score,team2score;
        CircularImageView oneImage, twoImage;
        LinearLayout match;

        public MatchHolder(@NonNull View itemView) {
            super(itemView);
            team1 = itemView.findViewById(R.id.team1);
            team2 = itemView.findViewById(R.id.team2);
            league = itemView.findViewById(R.id.league_name);
            oneImage = itemView.findViewById(R.id.team_one_image);
            twoImage = itemView.findViewById(R.id.team_two_image);
            date = itemView.findViewById(R.id.matchdescription);
            match = itemView.findViewById(R.id.itemlayout);
            winner = itemView.findViewById(R.id.winner);
            team1score = itemView.findViewById(R.id.team1score);
            team2score = itemView.findViewById(R.id.team2score);
        }
    }
}