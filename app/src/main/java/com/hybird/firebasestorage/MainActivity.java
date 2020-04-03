package com.hybird.firebasestorage;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openUploadPage(View view)
    {
        try
        {
            startActivity(new Intent(this,UploadScreen.class));
        }
        catch (Exception e)
        {
            Toast.makeText(this, "openUploadPage:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openDownloadPage(View view)
    {
        try
        {
            startActivity(new Intent(this,DownloadPage.class));
        }
        catch (Exception e)
        {
            Toast.makeText(this, "openDownloadPage:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
