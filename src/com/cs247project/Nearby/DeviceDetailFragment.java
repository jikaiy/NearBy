package com.cs247project.Nearby;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.cs247project.Nearby.LoadActivity.loadLocation;
import com.cs247project.Nearby.LoadActivity;;


/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    boolean isConnected=false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.devicedetail, null);
        mContentView.findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.groupOwnerIntent = 0;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true,
                        new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                ((WifiP2PActivity) getActivity()).cancelDisconnect();
                            }
                        }
                        );
                ((WifiP2PActivity) getActivity()).connect(config);

            }
        });
        
        //disconnect button
        mContentView.findViewById(R.id.disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	isConnected = false;
                        ((WifiP2PActivity) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.cancel).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((WifiP2PActivity) getActivity()).cancelDisconnect();
                    }
                });
        
        mContentView.findViewById(R.id.button1).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to send their name and location info
                    	EditText input = (EditText)(mContentView.findViewById(R.id.editText1));
                    	String name = input.getText().toString();
                    	
                    	GetGPSLocation getGPSLocation = new GetGPSLocation(getActivity());
                    	double latitude;
                    	double longitude;
                    	String message;
                    	
                    	// Make the message
                    	if(getGPSLocation.canGetLocation()) {
                    		latitude = getGPSLocation.getLatitude();
                    		longitude = getGPSLocation.getLongitude();
                    		message = name + "\n" + Double.toString(latitude) + "\n" + Double.toString(longitude);
                    	} else {
                    		message = name;
                    	}
                    	
                    	// Update status bar
                        ((TextView) mContentView.findViewById(R.id.status_text)).setText(
                        		"Sending location info about " + name );
                        Log.d(WifiP2PActivity.TAG, "Sending -----------> " + message);
                        
                        // Building the intent for data transfer service
                        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                        
                        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                        serviceIntent.putExtra(FileTransferService.EXTRAS_MESSAGE, message);
                        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                info.groupOwnerAddress.getHostAddress());
                        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
                        
                        // Start transmission
                        getActivity().startService(serviceIntent);
                    }
                });

        return mContentView;
    }


    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        if(this.getView().getVisibility()!=View.VISIBLE) 
        	this.getView().setVisibility(View.VISIBLE);   
        mContentView.findViewById(R.id.connect).setVisibility(View.GONE);
        mContentView.findViewById(R.id.disconnect).setVisibility(View.VISIBLE);
        if(info.groupFormed && !info.isGroupOwner) {
	        mContentView.findViewById(R.id.editText1).setVisibility(View.VISIBLE);
	        mContentView.findViewById(R.id.textView_name).setVisibility(View.VISIBLE);
	        mContentView.findViewById(R.id.button1).setVisibility(View.VISIBLE);
        } else if(info.groupFormed) {
        	mContentView.findViewById(R.id.txt_received).setVisibility(View.VISIBLE);
	        mContentView.findViewById(R.id.txt_name).setVisibility(View.VISIBLE);
	        mContentView.findViewById(R.id.button2).setVisibility(View.VISIBLE);
	        mContentView.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					
					if(mContentView.findViewById(R.id.txt_received)!=null) {
						String name_received = ((TextView) mContentView.findViewById(R.id.txt_received)).getText().toString();
						String lat_received = ((TextView) mContentView.
								findViewById(R.id.txt_lat)).getText().toString();
						String long_received = ((TextView) mContentView.
								findViewById(R.id.txt_long)).getText().toString();
						
						/*Intent intent = new Intent(getActivity(), LoadWifiP2PActivity.class);
						intent.putExtra("name", name_received);
						intent.putExtra("lat", lat_received);
						intent.putExtra("long", long_received);
						Log.d(WifiP2PActivity.TAG, "before "+ name_received);
						Log.d(WifiP2PActivity.TAG, "before "+ lat_received);
						Log.d(WifiP2PActivity.TAG, "before "+ long_received);
						getActivity().startActivity(intent);*/
						loadLocation mloadLocation = 
								new loadLocation(name_received, lat_received, long_received);
					    mloadLocation.execute("");
					    
					    new FileServerAsyncTask(getActivity(), mContentView).execute();
					}
					
				}
			});
        }
        
    	isConnected=true;
    	/*
        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
		*/
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
        	((TextView) mContentView.findViewById(R.id.info_line)).setText("Connected with another device and helping the device report location");
            new FileServerAsyncTask(getActivity(), mContentView).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
        	((TextView) mContentView.findViewById(R.id.info_line)).setText(
        			"Connected with another device and use it to update my location");
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

       
    }

    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        ((TextView) mContentView.findViewById(R.id.device_info)).setText(
        		"Device Name: " + device.deviceName + "\n" + 
        		"Device Address: " + device.deviceAddress + "\n" +
        		"Device Status: " + getDeviceStatus(device.status));

    }
    
    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private View mView;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View mView) {
            this.context = context;
            this.mView = mView;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WifiP2PActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WifiP2PActivity.TAG, "Server: connection done");

                Log.d(WifiP2PActivity.TAG, "server: receiving data ");
                InputStream inputstream = client.getInputStream();
                
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
                byte[] data = new byte[1024];  
                int count = -1;  
                while((count = inputstream.read(data,0,1024)) != -1)  
                    outStream.write(data, 0, count);  
                  
                data = null;  
                String message = new String(outStream.toByteArray(),"ISO-8859-1");  
               
                serverSocket.close();
                return message;
            } catch (IOException e) {
                Log.e(WifiP2PActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) { 
        	if (result != null) {
        		Log.d(WifiP2PActivity.TAG, "message received: " + result);
                String[] split_re = result.split("\n");
                double lat;
                double longi;
                String name;
                if(split_re.length == 1) {
                	name = split_re[0];
                	lat = 0;
                	longi = 0;
                } else {
                	name = split_re[0];
                	lat = Double.parseDouble(split_re[1]);
                	longi = Double.parseDouble(split_re[2]);
                }
                
                // Set status bar
                ((TextView) mView.findViewById(R.id.status_text)).setText(
                		"location message from " + name + " received.");
                // Set received info on UI
                ((TextView) mView.findViewById(R.id.txt_received)).setText(name);
                ((TextView) mView.findViewById(R.id.txt_lat)).setText(Double.toString(lat));
                ((TextView) mView.findViewById(R.id.txt_long)).setText(Double.toString(longi));
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            ((TextView) mView.findViewById(R.id.status_text)).setText(
            		"Waiting for location info from other device to relay");
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WifiP2PActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
    
  //clear the device info when disconnect
    public void blockDetail() {
    	mContentView.findViewById(R.id.connect).setVisibility(View.VISIBLE);
    	mContentView.findViewById(R.id.disconnect).setVisibility(View.GONE);
        TextView view = (TextView) mContentView.findViewById(R.id.peerdevice);
        view.setText("");
        this.getView().setVisibility(View.GONE);
        this.device=null;
        return;
    }

}
