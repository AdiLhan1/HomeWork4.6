package com.example.homework46;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity {
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId, code,myCode;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private Button confirm, sendCode;
    private EditText phoneNum, phoneCode;
    private PhoneAuthCredential credential;
    private FirebaseAuth mAuth;
    private boolean isCodeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        phoneNum = findViewById(R.id.edit_phoneNum);
        phoneCode = findViewById(R.id.edit_phoneCode);
        confirm = findViewById(R.id.btn_confirm);
        sendCode = findViewById(R.id.btn_codesend);
        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myCode=phoneCode.getText().toString().trim();
                if (TextUtils.isEmpty(myCode)){
                    phoneCode.setError("Напишите код!");
                }
                String smsCode=credential.getSmsCode();
                    signInWithPhoneAuthCredential(credential);

            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d("TAG", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w("TAG", "onVerificationFailed", e);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                isCodeSent = true;
                mVerificationId = verificationId;
                code = String.valueOf(token);
                 credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                String myCode2=credential.getSmsCode().toString();
                if (isCodeSent){
                    Toast.makeText(ProfileActivity.this, myCode2, Toast.LENGTH_LONG).show();
                }



            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                            finish();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void onClick(View view) {
        String phone = phoneNum.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            phoneNum.setError("Напиши правильный номер");
            return;
        }
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                30,
                TimeUnit.SECONDS,
                this,
                callbacks);
        confirm.setVisibility(View.GONE);
        phoneNum.setVisibility(View.GONE);
        sendCode.setVisibility(View.VISIBLE);
        phoneCode.setVisibility(View.VISIBLE);
    }
}
