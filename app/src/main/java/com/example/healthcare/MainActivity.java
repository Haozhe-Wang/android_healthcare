package com.example.healthcare;

import android.app.*;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.*;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;


import com.google.firebase.firestore.*;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailField;
    private EditText passwordField;
    private final static String TAG= "Login_activity";
    private FirebaseAuth.AuthStateListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            transitToDashboard();
            finish();
        }
        setContentView(R.layout.activity_main);

        // set the Notification bar to transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
    }
    @Override
    public void onStart(){
        super.onStart();
        listener =new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    transitToDashboard();
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(listener);
    }
    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Required.");
            valid = false;
        } else {
            passwordField.setError(null);
        }

        return valid;
    }
    public void transitToDashboard(){
        Intent i = new Intent(this,PatientConditionListActivity.class);
//        i.putExtra("USER",user);
        startActivity(i);
    }
    public void errorInfoPopup(String text){
        if (text == null){return;}
        new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK)
                .setTitle("Login Error")
                .setMessage(text)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void login(View view){
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:email and password authorized");
                            FirebaseUser user = mAuth.getCurrentUser();
                            findViewById(R.id.loading).setVisibility(View.GONE);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference ref = db.document("users/"+user.getUid());
                            ref.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    String type = document.getString("type");
                                                    if (type == null){
                                                        errorInfoPopup("An error in User profile, " +
                                                                "Please contact our staff");
                                                        mAuth.signOut();
                                                    }
                                                    else if (!type.equals("doctor") ){
                                                        errorInfoPopup("Sorry! You are not a doctor." +
                                                                "You can't use the app at the moment. " +
                                                                "New feature is coming soon");
                                                        mAuth.signOut();
                                                    }
                                                } else {
                                                    errorInfoPopup("An error occurred when fetching user data, " +
                                                            "Please try again later");
                                                    mAuth.signOut();
                                                }
                                            } else {
                                                errorInfoPopup("An error occurred when fetching user data, " +
                                                        "Please try again later");
                                                mAuth.signOut();
                                            }
                                        }

                                    });

//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            findViewById(R.id.loading).setVisibility(View.GONE);
                            if (task.getException() != null) {
                                errorInfoPopup(task.getException().getMessage());
                            }
//                            Toast toast=Toast.makeText(getApplicationContext(), "Authentication failed.",
//                                    Toast.LENGTH_LONG);
//                            toast.setMargin(50,50);
//                            toast.show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });

    }
}
