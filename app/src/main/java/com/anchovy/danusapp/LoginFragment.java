package com.anchovy.danusapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment implements View.OnClickListener{

    private EditText npmLoginInput, passwordLoginInput;
    private FirebaseAuth firebaseAuth;

    public LoginFragment() {
        /** Default Constructor */
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        npmLoginInput = (EditText)view.findViewById(R.id.npm_input_login_frag);
        passwordLoginInput = (EditText)view.findViewById(R.id.password_input_login_frag);

        Button registerButton = (Button)view.findViewById(R.id.register_button_login_frag);
        registerButton.setOnClickListener(this);

        Button loginButton = (Button)view.findViewById(R.id.login_button_login_frag);
        loginButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button_login_frag :
                changeToRegisterFragment();
                break;
            case R.id.login_button_login_frag :
                MainActivity.hideKeyboard(getActivity());
                String email = npmLoginInput.getText().toString();
                String password = passwordLoginInput.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty((password))) {
                    email = email.substring(4,6) + email.substring(2,4) + email.substring(7,10) + "@student.unpar.ac.id";
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "NPM atau Password harus diisi!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void changeToRegisterFragment () {
        getFragmentManager().beginTransaction().replace(R.id.activity_login, new RegisterFragment()).addToBackStack(null).commit();
    }
}