package com.example.healthcare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthcare.dummy.PatientListTitle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * An activity representing a list of PatientConditions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PatientConditionDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PatientConditionListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static FirebaseAuth mAuth;

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

        View recyclerView = findViewById(R.id.patientcondition_list);
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

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, PatientListTitle.ITEMS, mTwoPane));
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
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.rgb(134, 135, 255));
            gd.setCornerRadius(5);
            gd.setStroke(4, Color.rgb(255, 255, 255));
            holder.colorRangeView.setImageDrawable(gd);
            holder.patientNameView.setText(mValues.get(position).getPatientName());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
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
