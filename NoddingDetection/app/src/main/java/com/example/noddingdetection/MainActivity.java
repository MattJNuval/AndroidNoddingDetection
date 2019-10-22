package com.example.noddingdetection;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.otaliastudios.cameraview.CameraView;

import java.util.List;

public class MainActivity
        extends CommonActivity implements AdapterView.OnItemSelectedListener  {

    private boolean cameraAllowed;
    private CameraControl cameraControl;
    private int cameraPreviewVisible;
    private Button swapCamera;

    private Spinner spinner;

    public MainActivity() {
        super();
        this.cameraAllowed = false;
        this.cameraPreviewVisible = -1;
        return;
    }

    private void swapViewsAnimated(final View v1, final View v2) {
        v1.animate().alpha(0.0f).setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        v1.clearAnimation();
                        v2.setVisibility(View.INVISIBLE);
                        v1.setVisibility(View.GONE);
                        v2.animate().alpha(1.0f).setDuration(500)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        v2.clearAnimation();
                                        v2.setVisibility(View.VISIBLE);
                                        return;
                                    }
                        });
                        return;
                    }
                });
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        final Toolbar toolbar = super.findViewById(R.id.toolbar);
        super.setSupportActionBar(toolbar);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.camera_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        swapCamera = (Button) findViewById(R.id.swapCameraBtn);

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                )
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            cameraAllowed = true;
                        }
                        else {
                            cameraAllowed = false;
                        }
                        return;
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        return;
                    }
                }).check();

        if (this.cameraAllowed) {
            this.cameraControl = new CameraControl(this);
            this.cameraControl.onCreate();
        }

        final CameraView cameraPreviewView = (CameraView)super.findViewById(R.id.camera_preview_view);
        this.cameraPreviewVisible = cameraPreviewView.getVisibility();
        final ConstraintLayout detailsView = (ConstraintLayout)super.findViewById(R.id.detail_view);
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_clear) {
            if (this.cameraControl != null) {
                this.cameraControl.clearPosition();
            }
            super.vibrate(300);
            return true;
        }
        else if (id == R.id.action_clear_cap) {
            if (this.cameraControl != null) {
                this.cameraControl.clearCapturedPosition();
            }
            super.vibrate(300);
            return true;
        }
        else if (id == R.id.action_clear_status) {
            if (this.cameraControl != null) {
                this.cameraControl.clearStatus();
            }
            super.vibrate(300);
            return true;
        }
        else if (id == R.id.action_close) {
            super.vibrate(100);
            final Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            super.startActivity(homeIntent);
            return true;
        }
        else if(id == R.id.swap_camera) {
            if(swapCamera.getText() == "Front View") {
                cameraControl.swapLens();
                swapCamera.setText("Rear View");
                vibrate(300);
            } else {
                cameraControl.swapLens();
                swapCamera.setText("Front View");
                vibrate(300);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.cameraControl != null) {
            try {
                this.cameraControl.onDestroy();
            }
            catch (Exception e) {
                e.printStackTrace();
                super.showError(e.getMessage());
            }
        }
        return;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        final CameraView cameraPreviewView = (CameraView)super.findViewById(R.id.camera_preview_view);
        this.cameraPreviewVisible = cameraPreviewView.getVisibility();
        final ConstraintLayout detailsView = (ConstraintLayout)super.findViewById(R.id.detail_view);

        if(position == 0) {
            swapViewsAnimated(detailsView, cameraPreviewView);
            cameraPreviewVisible = View.VISIBLE;
        } else if(position == 1) {
            swapViewsAnimated(cameraPreviewView, detailsView);
            cameraPreviewVisible = View.GONE;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onSwap(View view)  {
        if(swapCamera.getText() == "Front View") {
            cameraControl.swapLens();
            swapCamera.setText("Rear View");
            vibrate(300);
        } else {
            cameraControl.swapLens();
            swapCamera.setText("Front View");
            vibrate(300);
        }

    }
}