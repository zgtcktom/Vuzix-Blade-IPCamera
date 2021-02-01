package com.aidlab.blade;

import android.hardware.camera2.CameraAccessException;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.vuzix.hud.actionmenu.ActionMenuActivity;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainActivity extends ActionMenuActivity {

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    public static ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private static byte[] frame;
    private final String TAG = "CommunicationTemplate";
    TextView textView;
    private EditText textArea;
    private PreviewCamera previewCamera;
    private HTTPServer server;
    private Thread serverThread;

    public static byte[] getFrame() {
        try {
            lock.readLock().lock();
            return frame;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void setFrame(ByteArrayOutputStream stream) {
        try {
            lock.writeLock().lock();
            frame = stream.toByteArray();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        textArea = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.navText);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            previewCamera.captureSession.stopRepeating();
            previewCamera.captureSession.close();
            previewCamera.cameraDevice.close();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        server.isRunning = false;
        try {
            serverThread.join();
            Log.d(TAG, "Server thread ended");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finish();
    }

    private void showAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        String ipString = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
        textView.setText(ipString);
        Log.d(TAG, ipString);
    }

    @Override
    protected void onResume() {
        super.onResume();

        previewCamera = new PreviewCamera(this, findViewById(R.id.textureView));
        previewCamera.start();

        server = new HTTPServer();
        serverThread = new Thread(server);
        serverThread.start();

        showAddress();

        try {
            previewCamera.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
