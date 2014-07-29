package com.cs247project.Nearby;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GetGPSLocation extends Service implements LocationListener{
	
	
	private Context mContext;
	boolean isGPSEnable = false;
	boolean isNetworkEnable = false;
	boolean canGetLocation = false;
	
	Location location;
	double latitude;
	double longitude;
	
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BW_UPDATES = 1000*60*1;
	
	protected LocationManager locationManager;
	
	public GetGPSLocation(Context context){
		this.mContext=context;
		getLocation();
	}
	
	
	public Location getLocation(){
		try{
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
			isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if(!isGPSEnable && !isNetworkEnable){
				
			}else{
				this.canGetLocation = true;
				if(isNetworkEnable){
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network","Network Enabled");
					if(locationManager!=null){
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(location!=null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				
				if(isGPSEnable){
					if(location==null){
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS","GPS Enabled");
						if(locationManager!=null){
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if(location!=null){
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		return location;
	}
	
	
	public void stopGPS(){
		if(locationManager!=null){
			locationManager.removeUpdates(GetGPSLocation.this);
		}
	}
	
	public double getLatitude(){
		return this.latitude;
	}
	
	public double getLongitude(){
		return this.longitude;
	}
	
	public boolean canGetLocation(){
		return this.canGetLocation;
	}
	
	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will launch Settings Options
	 * */
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
   	 
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	mContext.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
	}

	

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
