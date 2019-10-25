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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.compiler.PluginProtos;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    private static final int SELECT_IMAGE = 101;
    private TextView textName;
    private ImageView gallery;
    private FirebaseFirestore FirebaseFirestore;
    final String SAVE_TEXT = "saved_text";
    private SharedPreferences preferences;
    private Bitmap bitmap;
    private String name;
    private byte[] imageAsBytes;
    private String encoded;
    private String url;
    private ProgressBar spinner;
    private Drawable drawable;
    private StorageReference photoRef;
    private FirebaseStorage mFirebaseStorage;
    private UploadTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
        setContentView(R.layout.activity_main);
        textName = findViewById(R.id.textName);
        gallery = findViewById(R.id.imageView);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        mFirebaseStorage=FirebaseStorage.getInstance();
        photoRef = mFirebaseStorage.getReference();
        // getUserInfoListener();
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
                if (bitmap != null) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), intent.getData());
                        photoRef = mFirebaseStorage.getReferenceFromUrl(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        gallery.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Удаление фото");
                builder.setMessage("Вы хотите удалить эту фотографию?");

                // add the buttons
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (gallery != null) {
                            // gallery.setImageResource(0);
                            gallery.setImageResource(R.mipmap.ic_launcher_round);
                            preferences.edit().remove("key1").commit();
                            StorageReference desertRef = photoRef.child("images/desert.jpg");
                            desertRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Log.e("TAG", "onComplete: successful ");
                                    } else {
                                        Log.e("TAG", "onFailure: did not delete file");
                                    }
                                }
                            });
                            Toast.makeText(MainActivity.this, "Вы успешно удалили фото!", Toast.LENGTH_SHORT).show();
                            if (url != null) {
                                photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.e("TAG", "onComplete: successful ");
                                        } else {
                                            Log.e("TAG", "onFailure: did not delete file");
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                builder.setNegativeButton("Нет", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
        loadText();
        getUserInfo();
          Glide.with(this).load("data:image/jpeg;base64,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").centerCrop().into(gallery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_signOut:
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
                break;
            case R.id.action_share:
                if (url != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    intent.setType("text/plain");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Загрузите картинку", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getUserInfo() {
        if (FirebaseFirestore != null) {
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
            Intent intent = new Intent(this, EditNameActivity.class);
            intent.putExtra("key20", name);
        }
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                Log.e("TAG", "onActivityResult: " + bitmap);
//                Glide.with(this).load(data.getData()).centerCrop().into(gallery);
                gallery.setImageBitmap(bitmap);
                upload(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void upload(Uri uri) {
        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        uploadTask = reference.putFile(uri);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.e("TAG", "onProgress: " + progress);
                if (progress == 100.0) {
                    spinner.setVisibility(View.GONE);
                } else {
                    spinner.setVisibility(View.VISIBLE);
                }
            }
        })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    url = task.getResult().toString();
                    Log.e("TAG", "onComplete: " + url);
                    textName.setText(url);
                    saveUser(url);
                } else {
                    Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUser(final String url) {
//        Map<String,String>map=new HashMap<>();
//        map.put("avatar",url);
        String userId = FirebaseAuth.getInstance().getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("avatar", url)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Успешно", Toast.LENGTH_SHORT).show();
                            try {
                                photoRef = mFirebaseStorage.getReferenceFromUrl(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveText() {
        if (bitmap == null) {
            try {
                Toast.makeText(this, "Успешно", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "bitmap: " + bitmap);
                bitmap = ((BitmapDrawable) gallery.getDrawable()).getBitmap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (gallery == null) {
            Log.e("TAG", "gallery: " + gallery);
        }
        if (bitmap != null) {
            bitmap = ((BitmapDrawable) gallery.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            encoded = Base64.encodeToString(b, Base64.DEFAULT);
        }
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(SAVE_TEXT, textName.getText().toString());
        ed.putString("key1", encoded);
        ed.putString("", url);
        ed.commit();
    }

    private void loadText() {
        preferences = getPreferences(MODE_PRIVATE);
        String savedText = preferences.getString(SAVE_TEXT, "");
        String savedImage = preferences.getString("", "");
        String saveBitmap = preferences.getString("key1", "");
        textName.setText(savedText);
//        Glide.with(this).load(savedImage).centerCrop().into(gallery);
        imageAsBytes = Base64.decode(saveBitmap.getBytes(), Base64.DEFAULT);
        gallery.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
        saveText();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

