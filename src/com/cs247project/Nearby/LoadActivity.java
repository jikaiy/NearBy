package com.cs247project.Nearby;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoadActivity extends Activity{
	Button btnLoad;
	Button btnGetLocation;
	Button btnLinkToWelcome;
	EditText inputName;
	//EditText inputLocationX;
	//EditText inputLocationY;
	TextView location_msg;
	TextView load_erro_msg;
	
	GetGPSLocation gpsLocation;
	
	double latitude;
	double longitude;
	Timer timer;
	boolean timerActive = false;
	String userName = "";
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_location);
		
		btnLoad = (Button) findViewById(R.id.btnLoad);
		btnGetLocation = (Button)findViewById(R.id.btnGetLocation);
		btnLinkToWelcome = (Button) findViewById(R.id.btnLinkToWelcomeScreen);
		inputName = (EditText) findViewById(R.id.Name);
		
		location_msg = (TextView) findViewById(R.id.location_msg);
		
		load_erro_msg  =(TextView) findViewById(R.id.load_error_msg);
		
		
		btnGetLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gpsLocation = new GetGPSLocation(LoadActivity.this);
				
				if(gpsLocation.canGetLocation()){
					latitude = gpsLocation.getLatitude();
					longitude = gpsLocation.getLongitude();
					location_msg.setText("latitude: "+latitude+", logitude: "+longitude);
					Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
				}else{
					gpsLocation.showSettingsAlert();
				}
			}
		});
		
		
		
		btnLoad.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(!timerActive) {
					Log.d("gpsload", "first time set a timer");
					userName = inputName.getText().toString();
					timer = new Timer();
					TimerTask gpsTask = new gpsTimerTask(userName);
					timer.scheduleAtFixedRate(gpsTask, 0, MainActivity.locationUploadInterval);
					timerActive = true;
				} else {
					String newUserName = inputName.getText().toString();
					if(!userName.equals(newUserName)) {
						timer.cancel();
						Log.d("gpsload","change a new name");
						TimerTask gpsTask = new gpsTimerTask(newUserName);
						timer = new Timer();
						timer.scheduleAtFixedRate(gpsTask, 0, MainActivity.locationUploadInterval);
					} else {
						Log.d("gpsload", "same name, no need to restart the timer!");
					}
				}
			}
		});
		
		
		btnLinkToWelcome.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(i);
				finish();
			}
		});
	}
	
	
	public static class loadLocation extends AsyncTask<String, Void, Boolean>{

		String name = "";
		String locationX = "";
		String locationY = "";
		
		//result data
		String successFlag;
		
		Exception exception = null;
		
		loadLocation(String strName, String strLocationX, String strLocationY){
			name = strName;
			locationX = strLocationX;
			locationY = strLocationY;
		}
		
		
		//protected void onPreExecute()
		//TODO: show a progress dialog
		
		@Override
		protected Boolean doInBackground(String... params) {
			try{
				
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("tag", "load"));
				nameValuePairs.add(new BasicNameValuePair("name", name));
				nameValuePairs.add(new BasicNameValuePair("locationX", locationX));
				nameValuePairs.add(new BasicNameValuePair("locationY", locationY));
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 1500);
				HttpConnectionParams.setSoTimeout(httpParameters, 1500);
				
				HttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpPost httppost = new HttpPost(MainActivity.serverIP);
				//TODO: enter localhost IP here
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				Log.d("result:", result);
				
				JSONObject jsonObject = new JSONObject(result);
				successFlag = jsonObject.getString("success");
				if(successFlag.equals("1")) {
					return true;
				}
				else {
					return false;
				}
						
			}catch(Exception e){
				Log.e("loadActivity","Error:", e);
				exception = e;
				
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean valid){
			//btnLoad.setEnabled(true);
			/*
			if(valid) {
				Toast.makeText(mContext, "Load successed!", Toast.LENGTH_LONG).show();
			} else if(exception!=null) {
				Toast.makeText(mContext, "Load failed! " + exception.getMessage(), Toast.LENGTH_LONG).show();
			}*/
		}
		
	}
	
	public class gpsTimerTask extends TimerTask {
		private String userName;
		public gpsTimerTask(String userName) {
			this.userName = userName;
		}
		@Override
		public void run(){
			gpsLocation = new GetGPSLocation(LoadActivity.this);
			Log.d("timer", "uploading location of user " + this.userName);
			if(gpsLocation.canGetLocation()){
				latitude = gpsLocation.getLatitude();
				longitude = gpsLocation.getLongitude();
				String locationX = String.valueOf(latitude);
				String locationY = String.valueOf(longitude);
				
				//load the location to server
				loadLocation mloadLocation = new loadLocation(this.userName, locationX, locationY);
			    mloadLocation.execute("");
			}
		}
	}
}