package fiware.routetransfer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 *   Listens for incoming Bluetooth connections
 *
 */
public class ConnectionListener extends Thread {
    private Handler handler;

    private boolean cancelled = false;

    private BluetoothServerSocket btServerSocket;

    public ConnectionListener(Handler handler) {
        this.handler = handler;
    }

    private void sendMessage(String coords) {
        Message msg = handler.obtainMessage(Application.COORDS_MSG);
        Bundle bundle = new Bundle();
        bundle.putString(Application.COORDS_KEY, coords);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void kill() {
        try {
            btServerSocket.close();
        }
        catch(IOException ioe) {
            Log.e(Application.TAG,"Error while closing server socket");
        }
        finally {
            cancelled = true;
        }
        Log.d(Application.TAG, " Server socket finished!");
    }

    @Override
    public void run() {
        cancelled = false;

        UUID uuid = UUID.fromString("29B966E5-FBAD-4A05-B40E-86205D77AF72");

        try {
            btServerSocket =
                    BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("name",uuid);
            Log.d(Application.TAG, "Listening for incoming BT Connections");

            while(true && !cancelled) {
                BluetoothSocket bts = btServerSocket.accept();

                Log.d(Application.TAG, "Connected!!!");

                byte[] buffer = new byte[100];
                int bytesRead = bts.getInputStream().read(buffer);
                String coords = new String(Arrays.copyOf(buffer, bytesRead));
                Log.d(Application.TAG, "Coordinates: " + coords);

                bts.close();
                // Now send data to the UI part
                sendMessage(coords);
            }
        } catch (IOException e) {
            Log.e(Application.TAG, "listen() failed", e);
        }
    }
}
