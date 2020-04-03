package com.hybird.firebasestorage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hybird.firebasestorage.MainActivity;
import com.hybird.firebasestorage.R;

public class DownloadPage extends AppCompatActivity {
    private EditText videoNameET;
    private VideoView videoToDownloadIV;
    private Button downloadVideoBtn;
    private FirebaseFirestore objectFirebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_page);
        videoNameET=findViewById(R.id.videoNameET);
        videoToDownloadIV=findViewById(R.id.videoToDownloadIV);
        downloadVideoBtn=findViewById(R.id.downloadVideoBtn);
        objectFirebaseFirestore=FirebaseFirestore.getInstance();
        downloadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageUrl();
            }
        });
    }

    private void getImageUrl()
    {
        objectFirebaseFirestore.collection("UploadedVideosLinks")
                .document(videoNameET.getText().toString())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    String url=documentSnapshot.getString("url");
                    Glide.with(DownloadPage.this)
                            .load(url)
                            .into(videoToDownloadIV);
                }
                else
                {
                    Toast.makeText(DownloadPage.this, "No Such Document Exists", Toast.LENGTH_SHORT).show();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DownloadPage.this, "Fails to get url :"
                                +e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void moveToBack(View view)
    {
        try
        {
            startActivity(new Intent(this, MainActivity.class));
        }
        catch (Exception e)
        {
            Toast.makeText(this, "moveToBack:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
