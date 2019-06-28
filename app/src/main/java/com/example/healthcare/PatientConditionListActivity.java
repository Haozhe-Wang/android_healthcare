package com.example.healthcare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.healthcare.dummy.PatientListTitle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * An activity representing a list of PatientConditions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PatientConditionDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 *
 * TODO may change the recyclerView, check if possible to populate one item at a time, can delete item by position in patientListTile
 */

public class PatientConditionListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static FirebaseAuth mAuth;
    private static String TAG = "patient_condition_list";
    private static NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null){
            transitToLogin();
            finish();
        }


        setContentView(R.layout.activity_patientcondition_list);

        /*// get the extra information stored in mainActivity data
        Bundle userMetaData = getIntent().getExtras();
        if (userMetaData == null){
            return;
        }
        FirebaseUser user = userMetaData.getString("USER");*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.patientcondition_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        final View recyclerView = findViewById(R.id.patientcondition_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        if (!mTwoPane){
            LinearLayout toolbarContainer = findViewById(R.id.toolbarContainer);
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) toolbarContainer.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.AuthStateListener listener =new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    transitToLogin();
                    finish();
                }
            }
        };

        mAuth.addAuthStateListener(listener);
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        final PatientConditionListActivity parent = this;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String doctorUID = user.getUid();
        DocumentReference ref = db.document("users/"+doctorUID);
        ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    List<String> patientUIDs;
                    try {
                        patientUIDs = (List<String>) documentSnapshot.get("patients");
                    }
                    catch (Exception ex){
                        patientUIDs = new ArrayList<>();
                        Log.e(TAG,"Cannot fetch patients or No patient: ", ex);
                    }
                    if (patientUIDs != null && patientUIDs.size()>0) {
                        for (final String patientUID : patientUIDs) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            if (!PatientListTitle.ITEM_MAP.containsKey(patientUID)) {
                                final DocumentReference patientConditionRef = db.document("users/" + patientUID +
                                        "/action/patientConditions");
                                patientConditionRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if (documentSnapshot.exists()){
                                            final PatientListTitle.DummyItem patient;
                                            if (!PatientListTitle.ITEM_MAP.containsKey(patientUID)){
                                                patient = PatientListTitle.addItem(patientUID,colorRange.NO_COLOR,null);
                                                patient.setPosition(PatientListTitle.ITEMS.indexOf(patient));
                                            }else{
                                                patient = PatientListTitle.ITEM_MAP.get(patientUID);
                                            }
                                            String latestUpdateTime = documentSnapshot.getString("latestUpdateTime");
                                            HashMap<String,Object> recentCondition = (HashMap<String,Object>) documentSnapshot.getData().get(latestUpdateTime);
                                            if (recentCondition!=null && recentCondition.containsKey("changedRange")) {
                                                List<String> changedRanges = (List<String>) recentCondition.get("changedRange");
                                                if (changedRanges != null && changedRanges.size()>0){
                                                    alertInfoPopup("patient update info",patientUID);
                                                }
                                                /*if (changedRanges != null && recentCondition.containsKey("ranges")) {
                                                    for (String changedRange : changedRanges) {
                                                        HashMap<String, String> ranges = (HashMap<String, String>) recentCondition.get("ranges");
                                                        if (ranges != null && ranges.containsKey(changedRange)) {
                                                            String color = ranges.get(changedRange);
                                                            if (color != null){

                                                                colorRange conditionColor =colorRange.valueOf(color.toUpperCase());
                                                                patient.setConditionColor(conditionColor);
                                                            }

                                                        }
                                                    }
                                                }*/
                                            }
                                            if (recentCondition!=null && recentCondition.containsKey("ranges")) {
                                                HashMap<String, String> ranges = (HashMap<String, String>) recentCondition.get("ranges");
                                                colorRange color = colorRange.NO_COLOR;
                                                for (Map.Entry range : ranges.entrySet()){
                                                    colorRange tempColor = colorRange.valueOf(range.getValue().toString().toUpperCase());
                                                    if(tempColor.getLevel() > color.getLevel()){
                                                        color=tempColor;
                                                    }
                                                }
                                                patient.setConditionColor(color);
                                            }
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            DocumentReference patientRef = db.document("users/" + patientUID);
                                            patientRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    String patientName = documentSnapshot.getString("firstName") +
                                                            " " + documentSnapshot.getString("lastName");
                                                    patient.setPatientName(patientName);
                                                    recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(parent, PatientListTitle.ITEMS, mTwoPane));
                                                }
                                            });
                                            Log.v(TAG,"patient: "+patient.getPatientName());

                                        }
                                    }
                                });
                            }
                        }
                    }

//                    recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(parent, PatientListTitle.ITEMS, mTwoPane));
                }else{
                    Log.e(TAG,"populate error, no such document");
                }
            }
        });
        /*ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.v(TAG,"SET UP LISTENER");
                if (task.isSuccessful()){
                    List<String> patientUIDs;
                    DocumentSnapshot documentSnapshot = task.getResult();
                    try {
                        patientUIDs = (List<String>) documentSnapshot.get("patients");
                    }
                    catch (Exception ex){
                        patientUIDs = new ArrayList<>();
                        Log.e(TAG,"Cannot fetch patients or No patient: ", ex);
                    }
                    if (patientUIDs != null && patientUIDs.size()>0) {
                        for (final String patientUID : patientUIDs) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            if (!PatientListTitle.ITEM_MAP.containsKey(patientUID)) {
                                final DocumentReference patientConditionRef = db.document("users/" + patientUID +
                                        "/action/patientConditions");
                                patientConditionRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            final PatientListTitle.DummyItem patient;
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if (!PatientListTitle.ITEM_MAP.containsKey(patientUID)){
                                                patient = PatientListTitle.addItem(patientUID,null,null);
                                                patient.setPosition(PatientListTitle.ITEMS.indexOf(patient));
                                            }else{
                                                patient = PatientListTitle.ITEM_MAP.get(patientUID);
                                            }
                                            String latestUpdateTime = documentSnapshot.getString("latestUpdateTime");
                                            HashMap<String,Object> recentCondition = (HashMap<String,Object>) documentSnapshot.getData().get(latestUpdateTime);
                                            if (recentCondition.containsKey("changedRange")) {
                                                List<String> changedRanges = (List<String>) recentCondition.get("changedRange");
                                                if (changedRanges != null && changedRanges.size() > 0 && recentCondition.containsKey("ranges")) {
                                                    for (String changedRange : changedRanges) {
                                                        HashMap<String, String> ranges = (HashMap<String, String>) recentCondition.get("ranges");
                                                        if (ranges != null && ranges.containsKey(changedRange)) {
                                                            String color = ranges.get(changedRange);
                                                            if (color != null){
                                                                colorRange conditionColor =colorRange.valueOf(color.toUpperCase());
                                                                patient.setConditionColor(conditionColor);
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            DocumentReference patientRef = db.document("users/" + patientUID);
                                            patientRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    String patientName = documentSnapshot.getString("firstName") +
                                                            " " + documentSnapshot.getString("lastName");
                                                    patient.setPatientName(patientName);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    }

                    recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(parent, PatientListTitle.ITEMS, mTwoPane));
                }else{
                    Log.e(TAG,"populate error, no such document");
                }
            }
        });*/

    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    public void alertInfoPopup(String text, final String patientUID){
        if (text == null){return;}
        new AlertDialog.Builder(this,AlertDialog.THEME_TRADITIONAL)
                .setTitle("Patient Condition Updated")
                .setMessage(text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mTwoPane) {
                            Bundle arguments = new Bundle();
                            arguments.putString(PatientConditionDetailFragment.ARG_ITEM_ID, patientUID);
                            PatientConditionDetailFragment fragment = new PatientConditionDetailFragment();
                            fragment.setArguments(arguments);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.patientcondition_detail_container, fragment)
                                    .commit();
                        } else {
//                            Context context = view.getContext();
                            Intent intent = new Intent(getApplicationContext(), PatientConditionDetailActivity.class);
                            intent.putExtra(PatientConditionDetailFragment.ARG_ITEM_ID, patientUID);

//                            context.startActivity(intent);
                            startActivity(intent);
                        }
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.btn_star)
                .show();
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final PatientConditionListActivity mParentActivity;
        private final List<PatientListTitle.DummyItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PatientListTitle.DummyItem item = (PatientListTitle.DummyItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(PatientConditionDetailFragment.ARG_ITEM_ID, item.getPatientUID());
                    PatientConditionDetailFragment fragment = new PatientConditionDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.patientcondition_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PatientConditionDetailActivity.class);
                    intent.putExtra(PatientConditionDetailFragment.ARG_ITEM_ID, item.getPatientUID());

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(PatientConditionListActivity parent,
                                      List<PatientListTitle.DummyItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        //	obtain	a	ViewHolder	object
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patientcondition_list_content, parent, false);
            return new ViewHolder(view);
        }

        //populate	the	view hierarchy	within	the	ViewHolder	object	with	the	data	to	be	displayed
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            ShapeDrawable sd = getRangeCircle(mValues.get(position).getConditionColor());
            holder.colorRangeView.setBackground(sd);
            holder.patientNameView.setText(mValues.get(position).getPatientName());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }
        private ShapeDrawable getRangeCircle(colorRange color){
            ShapeDrawable sd = new ShapeDrawable(new OvalShape());
            sd.setIntrinsicHeight(100);
            sd.setIntrinsicWidth(100);
            if (color == colorRange.RED) {
                sd.getPaint().setColor(Color.RED);
            }else if (color == colorRange.GREEN) {
                sd.getPaint().setColor(Color.GREEN);
            }else if(color == colorRange.AMBER){
                sd.getPaint().setColor(Color.YELLOW);
            }else if(color == colorRange.NO_COLOR){
                sd.getPaint().setColor(Color.rgb(200,200,200));

            }
            return sd;
        }
        public void changeRangeCircleColor(colorRange color){
//            if (color == colorRange.RED) {
//                gd.setColor(Color.RED);
//            }else if (color == colorRange.GREEN) {
//                gd.setColor(Color.GREEN);
//            }else {
//                gd.setColor(Color.YELLOW);
//            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        //The	ViewHolder	class	contains	an	text variables
        // together	with a constructor	method that initializes	those variables
        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView colorRangeView;
            final TextView patientNameView;

            ViewHolder(View view) {
                super(view);
                colorRangeView = (ImageView) view.findViewById(R.id.color_range_view);
                patientNameView = (TextView) view.findViewById(R.id.patient_name);
            }
        }
    }
    public void logOutUser(View view){
        mAuth.signOut();
    }
    public void transitToLogin(){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }
}
