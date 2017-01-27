package com.anchovy.danusapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;

public class RegisterAccountActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "1498109142";
    private static final int REQUEST_CODE = 1;
    private DatabaseReference databaseReference;
    private EditText npmInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        Intent loginIntent = LineLoginApi.getLoginIntentWithoutLineAppAuth(this.getBaseContext(), CHANNEL_ID);
        startActivityForResult(loginIntent, REQUEST_CODE);

        //npmInput = (EditText) findViewById(R.id.npm_register_form);
        //passwordInput = (EditText) findViewById(R.id.password_register_form);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE) {
            Toast.makeText(RegisterAccountActivity.this, "FAILED TO CONNECT TO LINE!!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RegisterAccountActivity.this, LoginActivity.class));
            RegisterAccountActivity.this.finish();
        }

        LineLoginResult result = LineLoginApi.getLoginResultFromIntent(data);

        switch (result.getResponseCode()) {

            case SUCCESS:
                String email = npmInput.getText().toString();
                email = email.substring(4,6) + email.substring(2,4) + email.substring(7,9) + "@student.unpar.ac.id";
                databaseReference.child("Email").setValue(email);
                databaseReference.child("Password").setValue(passwordInput.getText().toString());


                Intent transitionIntent = new Intent(this, MainActivity.class);

                transitionIntent.putExtra("display_name", result.getLineProfile().getDisplayName());
                transitionIntent.putExtra("status_message", result.getLineProfile().getStatusMessage());
                transitionIntent.putExtra("user_id", result.getLineProfile().getUserId());
                transitionIntent.putExtra("picture_url", result.getLineProfile().getPictureUrl().toString());
                break;
            case CANCEL:
                Toast.makeText(RegisterAccountActivity.this, "FAILED TO CONNECT TO LINE!!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterAccountActivity.this, LoginActivity.class));
                RegisterAccountActivity.this.finish();
                break;
            default:
                Toast.makeText(RegisterAccountActivity.this, "FAILED TO CONNECT TO LINE!!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterAccountActivity.this, LoginActivity.class));
                RegisterAccountActivity.this.finish();
        }
    }

    public void registerButtonClicked(View view) {

    }

    public void LineLogin () {
        Intent loginIntent = LineLoginApi.getLoginIntentWithoutLineAppAuth(this.getBaseContext(), CHANNEL_ID);
        startActivityForResult(loginIntent, REQUEST_CODE);
    }
}
