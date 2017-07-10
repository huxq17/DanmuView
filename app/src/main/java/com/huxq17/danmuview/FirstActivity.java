package com.huxq17.danmuview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FirstActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
    }

    public void onClick(View view) {
        int id = view.getId();
        Intent intent = new Intent(this, MainActivity.class);
        switch (id) {
            case R.id.bt_surfaceview:
                intent.putExtra(MainActivity.FLAG, MainActivity.FLAG_SURFACE_VIEW);
                break;
            case R.id.bt_glsurfaceview:
                intent.putExtra(MainActivity.FLAG, MainActivity.FLAG_GL_SURFACE_VIEW);
                break;
            case R.id.bt_textureview:
                intent.putExtra(MainActivity.FLAG, MainActivity.FLAG_TEXTURE_VIEW);
                break;
        }
        startActivity(intent);
    }

}
