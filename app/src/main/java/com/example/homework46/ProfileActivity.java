package com.example.homework46;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity {
    EditText editTextPhone, editTextCode;
    Button btn_numSend, btn_codeSend;
    FirebaseAuth mAuth;
    String codeSent;
    private CountryCodePicker ccp;
    private String selected_country_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        editTextCode = findViewById(R.id.edit_phoneCode);
        editTextPhone = findViewById(R.id.edit_phoneNum);
        ccp=findViewById(R.id.ccr);
        ccp.registerCarrierNumberEditText(editTextPhone);
        btn_numSend = findViewById(R.id.btn_numSend);
        btn_codeSend = findViewById(R.id.btn_codeSend);
        btn_numSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode();
                btn_numSend.setVisibility(View.GONE);
                editTextPhone.setVisibility(View.GONE);
                btn_codeSend.setVisibility(View.VISIBLE);
                editTextCode.setVisibility(View.VISIBLE);
            }
        });
        btn_codeSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySignInCode();
            }
        });
    }

    private void verifySignInCode() {
        String code = editTextCode.getText().toString();
        if (TextUtils.isEmpty(code)) {
            editTextCode.setError("Code is required");
            editTextCode.requestFocus();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithCredential:success");
                            Toast.makeText(ProfileActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {
        if (TextUtils.isEmpty(selected_country_code)) {
            editTextPhone.setError("Выберите код страны!");
            editTextPhone.requestFocus();
            Toast.makeText(this, "Вы не ввели код страны", Toast.LENGTH_SHORT).show();
            return;
        }
        String phoneNumber = selected_country_code + editTextPhone.getText().toString();
            if (TextUtils.isEmpty(phoneNumber)) {
                editTextPhone.setError("Phone number is required");
                editTextPhone.requestFocus();
                return;
            }
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    30,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//            editTextCode.setText(phoneAuthCredential.getSmsCode());
//            editTextCode.setText(codeSent);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }
    };

    public void onCountryPickerClick(View view) {
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                selected_country_code = ccp.getSelectedCountryCodeWithPlus();
                Toast.makeText(ProfileActivity.this, selected_country_code, Toast.LENGTH_SHORT).show();
            }
        });
    }
}