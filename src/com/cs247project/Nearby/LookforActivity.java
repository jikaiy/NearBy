package com.cs247project.Nearby;

import java.util.ArrayList;

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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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


public class LookforActivity extends Activity {
	Button btnQuery;
	Button btnLinkToWelcome;
	EditText inputName;
	TextView LocationX;
	TextView LocationY;
	TextView query_erro_msg;
	GoogleMap map;
	//TODO:
	double latitude;
	double longitude;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.query_location);
		
		inputName = (EditText) findViewById(R.id.queryName);
		LocationX = (TextView) findViewById(R.id.queryLocationX);
		LocationY = (TextView) findViewById(R.id.queryLocationY);
		btnQuery = (Button) findViewById(R.id.btnQuery);
		btnLinkToWelcome = (Button) findViewById(R.id.btnLinkToWelcomeScreen2);
		query_erro_msg = (TextView) findViewById(R.id.query_error_msg);
		
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //map.setMyLocationEnabled(true);
		
		
		btnQuery.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				String name = inputName.getText().toString();
				queryLocation mqueryLocation = new queryLocation(LookforActivity.this, name);
				mqueryLocation.execute("");
				btnQuery.setEnabled(false);
			}
			
		});
		
		btnLinkToWelcome.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),MainActivity.class);
				startActivity(i);
				finish();
			}
		});
	}
	
	
	public class queryLocation extends AsyncTask<String, Void, Boolean>{
		
		
		Context mContext = null;
		String name = "";
		
		//result data;
		String locationX;
		String locationY;
		String successFlag;
		String error_msg;
		
		Exception exception =null;
		
		queryLocation(Context context, String nameToQuery){
			mContext = context;
			name = nameToQuery;
		}

		//protected void onPreExcute(){}
		//TODO: show a progress dialog
		@Override
		protected Boolean doInBackground(String... params) {
			try{
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("tag", "query"));
				nameValuePairs.add(new BasicNameValuePair("name", name));
				
				
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 1500);
				HttpConnectionParams.setSoTimeout(httpParameters, 1500);			

				HttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpPost httppost = new HttpPost(MainActivity.serverIP);
				//TODO: enter your localhost IP here
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));        
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				String result = EntityUtils.toString(entity);
				//debug
				Log.d("result:", result);

				JSONObject jsonObject = new JSONObject(result);
				
				successFlag = jsonObject.getString("success");
				
				if(successFlag.equals("1")) {
					JSONObject user = jsonObject.getJSONObject("user");
					
					locationX = user.getString("locationX");
					locationY = user.getString("locationY");
					return true;
				} else {
					error_msg = jsonObject.getString("error_msg");
					return false;
				}
				
			}catch(Exception e){
				Log.e("query Location","Error:", e);
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean valid){
			if(valid) {
				query_erro_msg.setText("");
				LocationX.setText(locationX+"  "+locationY);
			
				double latitude = Double.parseDouble(locationX);
				double longitude= Double.parseDouble(locationY);
				MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Hello! "+name);
				map.addMarker(marker);
				CameraPosition cameraPosition = new CameraPosition.Builder().target(
		                new LatLng(latitude, longitude)).zoom(17).build();
		 
		        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			} else {
				query_erro_msg.setText(error_msg);
				LocationX.setText("");
			}
			
			btnQuery.setEnabled(true);
			
			if(exception != null){
				Toast.makeText(mContext, exception.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
	}
}