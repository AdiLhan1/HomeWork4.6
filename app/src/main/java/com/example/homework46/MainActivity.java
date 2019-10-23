package com.example.homework46;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_IMAGE = 101;
    private TextView textName;
    private ImageView exit,gallery;
    private FirebaseFirestore FirebaseFirestore;
    final String SAVE_TEXT = "saved_text";
    private SharedPreferences preferences;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
        setContentView(R.layout.activity_main);
        textName = findViewById(R.id.textName);
        exit = findViewById(R.id.sign_out);
        gallery=findViewById(R.id.imageView);
        // getUserInfoListener();
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Выход из аккаунта");
                builder.setMessage("Вы хотите выйти из аккаунта?");

                // add the buttons
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    }
                });
                builder.setNegativeButton("Нет", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
            }
        });
        loadText();
        getUserInfo();
    }


    private void getUserInfo() {
           if (FirebaseFirestore!=null) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users")
                .document((FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String name = task.getResult().getString("name");
                        }
                    }
                });
    }
       }


    //    public void getUserInfoListener() {
//        FirebaseFirestore.getInstance().collection("users")
//                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                        if (documentSnapshot != null && documentSnapshot.exists()){
//                            String name=documentSnapshot.getString("name");
//                            textName.setText(name);
//                        }
//                    }
//                });
//    }

    public void onClickEdit(View view) {
        startActivityForResult(new Intent(this, EditNameActivity.class), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            name = data.getStringExtra("key");
            textName.setText(name);
            Intent intent = new Intent(this,EditNameActivity.class);
            intent.putExtra("key20",name);
        }
        if (requestCode==SELECT_IMAGE && resultCode == RESULT_OK && data!= null){
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                Log.e("TAG", "onActivityResult: "+bitmap );
                gallery.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private void saveText() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(SAVE_TEXT, textName.getText().toString());
        ed.commit();
    }
    private void loadText() {
        preferences = getPreferences(MODE_PRIVATE);
        String savedText = preferences.getString(SAVE_TEXT, "");
            textName.setText(savedText);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveText();
    }
}

