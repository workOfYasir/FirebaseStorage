package com.hybird.firebasestorage;



import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadScreen extends AppCompatActivity {
    private EditText videoNameET;
    private VideoView videoToUploadIV;
    private Button uploadVideoBtn,select_video;
    private Uri objectUri;
    private MediaController objectMediaController;
    private StorageReference objectStorageReference;
    private FirebaseFirestore objectFirebaseFirestore;
    private boolean isVideoSelected=false;
    private Dialog objectDialog;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_screen);
        objectFirebaseFirestore=FirebaseFirestore.getInstance();
        objectStorageReference= FirebaseStorage.getInstance().getReference("Video");

        connectXML();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private void connectXML()
    {
        try
        {
            videoNameET=findViewById(R.id.videoNameET);
            videoToUploadIV=findViewById(R.id.videoToUploadIV);
            uploadVideoBtn=findViewById(R.id.videoUploadToServer);
            select_video=findViewById(R.id.select_video);
            select_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    videoToUploadIV.setVideoURI(null);
                    FromGallery();
                }
            });
            videoToUploadIV.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            objectMediaController = new MediaController(UploadScreen.this);
                            objectMediaController.setAnchorView(videoToUploadIV);
                        }
                    });
                }
            });
            videoToUploadIV.start();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "connectXML:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
 
    private void FromGallery()
    {
        try
        {
            Intent objectIntent=new Intent();
            objectIntent.setType("video/*");
            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent,123);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "selectVideoFromGallery:"+
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123 && resultCode==RESULT_OK && data!=null)
        {
            objectUri=data.getData();
            videoToUploadIV.setVideoURI(objectUri);
            isVideoSelected = true;
        }
    }
   
    public void uploadVideoToFS(View view)
    {
        try
        {
            if(isVideoSelected) {

                if (!videoNameET.getText().toString().isEmpty()) {

                    String videoFileName = videoNameET.getText().toString() + "." + getExtension(objectUri);
                    final StorageReference finalVideoRef = objectStorageReference.child(videoFileName);
        
                    UploadTask objectUploadTask = finalVideoRef.putFile(objectUri);
                    objectUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();

                            }

                            return finalVideoRef.getDownloadUrl();
                        }
                    })
                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> objectMap = new HashMap<>();
                                        objectMap.put("url", task.getResult().toString());

                                        objectFirebaseFirestore.collection("videoLinks")
                                                .document(videoNameET.getText().toString())
                                                .set(objectMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Toast.makeText(UploadScreen.this, "Video is Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(UploadScreen.this, "Fails to upload Video", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(UploadScreen.this, "Message from Firebase:" +
                                                task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadScreen.this, "Firebase Storage Response:"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Please Enter valid name.", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "Please select the video before uploading", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "uploadVideoToFirebaseStorage:"+
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private String getExtension(Uri objectUri)
    {
        try
        {
            ContentResolver objectContentResolver=getContentResolver();
            MimeTypeMap objectMimeTypeMap=MimeTypeMap.getSingleton();
            String extensionOfVideo=objectMimeTypeMap.getExtensionFromMimeType(objectContentResolver
                    .getType(objectUri));
            return extensionOfVideo;
        }
        catch (Exception e)
        {
            Toast.makeText(this, "getExtension:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null;

    }
}
