package com.clickandearn.clicker1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    Button signin;

    public static final String EMAIL = "EMAIL";
    public static final int AGB_VERSION = 1;

    public static final String agb = "Kein Geld für dich!";

    int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signin = findViewById(R.id.signinBt);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.signinBt) {
                    signIn();
                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        Intent log_intent = getIntent();
        boolean flag = log_intent.getBooleanExtra("Flag", false);

        if(!flag) {
            if (account != null) {
                String email = account.getEmail();
                if (HelperFunctions.getAGBStatus(email, this) == AGB_VERSION) {
                    if (HelperFunctions.userExists(account.getEmail(), this)) {
                        Intent intent = new Intent(MainActivity.this, AdActivity.class);
                        intent.putExtra(EMAIL, account.getEmail());
                        startActivity(intent);
                    }
                }else {
                    Intent intent = new Intent(MainActivity.this, AgbActivity.class);
                    intent.putExtra(EMAIL, account.getEmail());
                    startActivity(intent);
                }
            }
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Toast.makeText(this, "Try to choose the next activity", Toast.LENGTH_LONG).show();

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();

            boolean testCallDBagb = HelperFunctions.userExists(email, this);
            Toast.makeText(this, ""+testCallDBagb, Toast.LENGTH_LONG).show();

            if(HelperFunctions.userExists(account.getEmail(), this)) {
                if(HelperFunctions.getAGBStatus(email, this) == AGB_VERSION) {
                    Intent intent = new Intent(MainActivity.this, AdActivity.class);
                    intent.putExtra(EMAIL, email);

                    Toast.makeText(this, "Exists and AGB is newest", Toast.LENGTH_LONG).show();

                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, AgbActivity.class);
                    intent.putExtra(EMAIL, email);
                    Toast.makeText(this, "Exists but old AGB", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }

            }else{
                Intent intent = new Intent(MainActivity.this, AgbActivity.class);
                intent.putExtra(EMAIL, email);
                Toast.makeText(this, "Not existing", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Failed to change Activity", Toast.LENGTH_LONG).show();
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        boolean flag = intent.getBooleanExtra("Flag", false);

        if(flag) {
            signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show();
        }
    }
}
