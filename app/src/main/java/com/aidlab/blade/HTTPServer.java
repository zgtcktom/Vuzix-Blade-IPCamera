package com.aidlab.blade;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static android.content.ContentValues.TAG;

public class HTTPServer implements Runnable {
    private final int port = 8080;

    public boolean isRunning;

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        isRunning = true;
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (isRunning) {
            try {
                Socket socket = server.accept();
                Log.d(TAG, "Connection");
                Thread socketThread = new Thread(new HTTPSocket(socket));
                socketThread.start();
            } catch (SocketTimeoutException timeoutE) {
                Log.d(TAG, "SoTimeout");
            } catch (IOException ioE) {
                ioE.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (port != server.getLocalPort()) {
                    server.close();
                    server = new ServerSocket(port);
                    server.setSoTimeout(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
