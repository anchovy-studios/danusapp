package com.anchovy.danusapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterAccountActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "1498109142";
    private static final int GALERY_REQUEST_CODE = 1;
    private DatabaseReference databaseReference;
    private EditText npmInput, passwordInput, usernameLine, statusMessageLine;
    private String display_name, status_message, user_id, picture_url;
    private ImageButton dpImageButton;
    private Uri dpURI;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        display_name = getIntent().getStringExtra("display_name");
        status_message = getIntent().getStringExtra("status_message");
        user_id = getIntent().getStringExtra("user_id");
        picture_url = getIntent().getStringExtra("picture_url");

        dpURI = null;

        npmInput = (EditText)findViewById(R.id.email_register_form);
        npmInput.setText(picture_url);
        passwordInput = (EditText)findViewById(R.id.password_register_form);
        usernameLine = (EditText)findViewById(R.id.username_line_edit);
        usernameLine.setText(display_name);
        statusMessageLine = (EditText)findViewById(R.id.display_message_line_edit);
        statusMessageLine.setText(status_message);

        dpImageButton = (ImageButton)findViewById(R.id.image_button_dp);
        Picasso.with(this.getApplicationContext()).load(picture_url).into(dpImageButton);

        //npmInput = (EditText) findViewById(R.id.npm_register_form);
        //passwordInput = (EditText) findViewById(R.id.password_register_form);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void registerAccountClicked(View view) {
        String email = npmInput.getText().toString();
        String password = passwordInput.getText().toString();
        if (email.length() < 10 || password.length() < 6)  {
            Toast.makeText(RegisterAccountActivity.this, "NPM salah atau password terlalu pendek", Toast.LENGTH_LONG).show();
            return;
        }
        email = email.substring(4,6) + email.substring(2,4) + email.substring(7,10) + "@student.unpar.ac.id";
        display_name = usernameLine.getText().toString();
        status_message = statusMessageLine.getText().toString();

        final ProgressDialog dialog = new ProgressDialog(this);

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(display_name) && dpURI != null) {
            //dialog.setMessage("Membuat akun ... ");
            //dialog.show();
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(RegisterAccountActivity.this, "auth success", Toast.LENGTH_LONG).show();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Display Picture").child(firebaseAuth.getCurrentUser().getUid());
                    storageReference.putFile(dpURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(RegisterAccountActivity.this, "put file success", Toast.LENGTH_LONG).show();
                            final Uri downloadURL = taskSnapshot.getDownloadUrl();
                            final DatabaseReference userReference = databaseReference.child("Users");
                            final DatabaseReference lineReference = databaseReference.child("Line").child(user_id)
                                    .child(firebaseAuth.getCurrentUser().getUid());

                            userReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Toast.makeText(RegisterAccountActivity.this, "value 1", Toast.LENGTH_LONG).show();
                                    userReference.child(user_id).setValue(firebaseAuth.getCurrentUser().getUid());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(RegisterAccountActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG);
                                }
                            });

                            lineReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Toast.makeText(RegisterAccountActivity.this, "value 2", Toast.LENGTH_SHORT).show();
                                    lineReference.child("Line ID").setValue(user_id);
                                    lineReference.child("Username").setValue(display_name);
                                    lineReference.child("Status Message").setValue(status_message);
                                    lineReference.child("DP Picture").setValue(downloadURL.toString());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(RegisterAccountActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG);

                                }
                            });
                            dialog.dismiss();
                            startActivity(new Intent(RegisterAccountActivity.this, MainActivity.class));
                            RegisterAccountActivity.this.finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterAccountActivity.this);
                            alertDialog.setTitle("ERROR!");
                            alertDialog.setMessage(e.getMessage());
                            alertDialog.setCancelable(true);
                            alertDialog.setNeutralButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                            alertDialog.show();
                            dialog.dismiss();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(RegisterAccountActivity.this, "NPM, password, dan user name wajib diisi!", Toast.LENGTH_LONG);
        }
    }

    public void dpImageButtonClicked(View view) {
        Intent galeryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galeryIntent.setType("image/*");
        startActivityForResult(galeryIntent, GALERY_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle( "Yakin ingin kembali?" )
                .setMessage("Data tidak akan disimpan dan akan kembali ke menu login!")
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            RegisterAccountActivity.this.finishAndRemoveTask();

                        } else {
                            RegisterAccountActivity.this.finish();
                        }
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                dpURI = result.getUri();
                dpImageButton.setImageURI(dpURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
