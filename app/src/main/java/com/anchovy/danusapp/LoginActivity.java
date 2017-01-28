package com.anchovy.danusapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.linecorp.linesdk.LineApiResponse;
import com.linecorp.linesdk.api.LineApiClient;
import com.linecorp.linesdk.api.LineApiClientBuilder;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;

public class LoginActivity extends AppCompatActivity {

    private EditText npmInput, passwordInput;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog dialog;

    private static final String CHANNEL_ID = "1498109142";
    private static final int REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        npmInput = (EditText)findViewById(R.id.npm_login_form);
        passwordInput = (EditText)findViewById(R.id.password_login_form);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        dialog = new ProgressDialog(this);
    }


    public void signInButtonClicked(View view) {
        dialog.setMessage("Logging in ...");
        dialog.show();
        String email = npmInput.getText().toString();
        email = email.substring(4,6) + email.substring(2,4) + email.substring(7,10) + "@student.unpar.ac.id";
        String password = passwordInput.getText().toString();
        if (email.length() < 10 || password.length() < 6)  {
            Toast.makeText(LoginActivity.this, "NPM salah atau password terlalu pendek", Toast.LENGTH_LONG);
            return;
        }
        Toast.makeText(this, email, Toast.LENGTH_LONG).show();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    DatabaseReference ref = databaseReference.child("Users").child(firebaseAuth.getCurrentUser().getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("line_ID")) {
                                dialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                LoginActivity.this.finish();
                            } else {
                                dialog.dismiss();
                                //register form
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(LoginActivity.this, "Network Error!!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        } else {
            Toast.makeText(LoginActivity.this, "Field is empty!", Toast.LENGTH_LONG).show();
        }
    }

    public void registerButtonClicked(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle( "Pemberitahuan!" )
                .setMessage("Proses registrasi membutuhkan akun line.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                        }})
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        Intent loginIntent = LineLoginApi.getLoginIntent(LoginActivity.this.getApplicationContext(), CHANNEL_ID);
                        startActivityForResult(loginIntent, REQUEST_CODE);
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Error!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        if (requestCode != REQUEST_CODE) {
            alertDialog.setMessage("Internal Error!!");
            alertDialog.show();
        }

        final LineLoginResult result = LineLoginApi.getLoginResultFromIntent(data);

        switch (result.getResponseCode()) {

            case SUCCESS:
                firebaseAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        DatabaseReference ref = databaseReference.child("Line");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(result.getLineProfile().getUserId())) {
                                    alertDialog.setMessage("Anda sudah pernah membuat account dengan LINE ini!\n" +
                                            "Silahkan login menggunakan npm dan password atau gunakan fitur 'forgot password'");
                                    alertDialog.show();
                                    LineApiClient client;
                                    LineApiClientBuilder apiClientBuilder = new LineApiClientBuilder(getApplicationContext(), CHANNEL_ID);
                                    client = apiClientBuilder.build();
                                    client.logout();
                                } else {
                                    Intent transitionIntent = new Intent(LoginActivity.this, RegisterAccountActivity.class);

                                    transitionIntent.putExtra("display_name", result.getLineProfile().getDisplayName());
                                    transitionIntent.putExtra("status_message", result.getLineProfile().getStatusMessage());
                                    transitionIntent.putExtra("user_id", result.getLineProfile().getUserId());
                                    transitionIntent.putExtra("picture_url", result.getLineProfile().getPictureUrl().toString());

                                    startActivity(transitionIntent);
                                    LoginActivity.this.finish();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(LoginActivity.this, "FAILED TO CONNECT", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Cant connect to server!", Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case CANCEL:
                alertDialog.setMessage("Failed to connect to LINE");
                alertDialog.show();
                break;
            default:
                alertDialog.setMessage("Failed to connect to LINE");
                alertDialog.show();
        }
    }
}

