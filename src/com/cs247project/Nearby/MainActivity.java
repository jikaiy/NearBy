package com.cs247project.Nearby;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	Button btnLinkToLoad;
	Button btnLinkToLoadWiFiP2P;
	Button btnLinkToQuery;
	Button btnLinkToSearchPeers;
	public static final String serverIP = "http://169.234.60.5/nearby_api/index.php";
	public static final int locationUploadInterval = 20000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnLinkToLoad = (Button) findViewById(R.id.btnLinkToLoad);
		btnLinkToQuery = (Button) findViewById(R.id.btnLinkToQuery);
		btnLinkToSearchPeers = (Button) findViewById(R.id.btnLinkToSearchPeers);
		
		
		
		btnLinkToLoad.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				Intent i = new Intent(MainActivity.this, LoadActivity.class);
				MainActivity.this.startActivity(i);
			}
		});
      
      btnLinkToSearchPeers.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				Intent i = new Intent(MainActivity.this, WifiP2PActivity.class);
				MainActivity.this.startActivity(i);
			}
		});
		
		btnLinkToQuery.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				Intent i = new Intent(MainActivity.this, LookforActivity.class);
				MainActivity.this.startActivity(i);
			}
		});
		
	}
	

}
