package edu.ncsu.wifilocator;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	static final LatLng CENTER = new LatLng(35.769301, -78.676406);
	private GoogleMap map;
	private MainApplication application;
	private int NUM_BUTTONS = 4;
	private Button interval[], calibrate[];
    
	ArrayList<LatLng> pointsList;
	 
    // url to get all existing points list
    private static String url_points = "http://people.engr.ncsu.edu/ywang51/nprg/gen_json_for_android.php";
    
    // JSON Node names
    private static final String TAG_POINTS = "points";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
     
    // contacts JSONArray
    JSONArray points = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pointsList = new ArrayList<LatLng>();
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();
		
		//map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		
		// Move the camera instantly to the center with a zoom of 20.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 20));
		
		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
		
		application = (MainApplication) MainActivity.this.getApplication();
        application.mainActivity = this;
        
        interval = new Button[NUM_BUTTONS];
        interval[0] = (Button) findViewById(R.id.button1);
        interval[1] = (Button) findViewById(R.id.button2);
        interval[2] = (Button) findViewById(R.id.button3);
        interval[3] = (Button) findViewById(R.id.button4);
        
        calibrate = new Button[2];
        calibrate[0] = (Button) findViewById(R.id.enable_exist);
        calibrate[1] = (Button) findViewById(R.id.disable_exist);
        
        setTimeInterval();
        showPoints(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void updateLocation(LatLng cor){
		map.clear();
		map.addMarker(new MarkerOptions()
		.position(cor)
		.title(""+cor));
		//.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
	}
	
	public void updateStatus(String s){
		Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
	}

	public void setTimeInterval(){
		for (int i = 0; i < NUM_BUTTONS; i++) {
			final Button theButton = interval[i];
			theButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                // TODO Auto-generated method stub
	            	switch (v.getId()) {
	                	case R.id.button1:
	                		application.updateTimerInterval(5000);
	                		break;
	                	case R.id.button2:
	                		application.updateTimerInterval(10000);
	                		break;
	                	case R.id.button3:
	                		application.updateTimerInterval(15000);
	                		break;
	                	case R.id.button4:
	                		application.updateTimerInterval(20000);
	                		break;
	            	}
	            }
	        }); 
	    } 
	}
	
	public void showPoints(final Context c){
		for (int i = 0; i < 2; i++) {
			final Button button = calibrate[i];
			button.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					switch (v.getId()) {
						case R.id.enable_exist:
							new PostJSONDataAsyncTask(c, null, url_points, false){
								@Override
					            protected void onPreExecute()
					            {
					                super.onPreExecute();
					            }
					            
					            // Override the onPostExecute to do whatever you want
					            @Override
					            protected void onPostExecute(String response)
					            {
					                super.onPostExecute(response);
					                Log.d("wifiloc", response);
					                if (response != null)
					                {
					                	JSONObject json = null;
					                	try{
					                		json = new JSONObject(response);
					                	} catch (JSONException e){
					                		e.printStackTrace();
					                        Log.d("wifiloc", "Error parsing JSON");
					                	}
					        			
					        			if(json == null){
					                    	Log.d("wifiloc", "Error parsing server response");
					                        return;
					                    }
					                    
					        			pointsList.clear();
					        			
					                    // If returned object length is 
					                    if(json.length() > 0){
					            			try {
					            	            // Getting Array of existing points
					            	            points = json.getJSONArray(TAG_POINTS);
					            	             
					            	            // looping through All points
					            	            for(int i = 0; i < points.length(); i++){
					            	                JSONObject c = points.getJSONObject(i);
					            	                 
					            	                // Storing each json item in variable
					            	                double lat = c.getDouble(TAG_LAT);
					            	                double lng = c.getDouble(TAG_LNG);
					            	                
					            	                // adding each coordinate to ArrayList
					            	                LatLng temp = new LatLng(lat, lng);
					            	                pointsList.add(temp);
					            	            }
					            	        } catch (JSONException e) {
					            	            e.printStackTrace();
					            	        }
					                    }
					                    else {
					                        //TODO Do something here if no teams have been made yet
					                    }
					                    
					                    Log.d("wifiloc", "Update Success");
					                    
					                    for(int i = 0; i < pointsList.size(); i++){
					            			map.addMarker(new MarkerOptions()
					            			.position(pointsList.get(i))
					            			.title(""+pointsList.get(i))
					            			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
					            		}
					                }
					                else
					                {
					                    // Toast.makeText(context, "Error connecting to server", Toast.LENGTH_LONG).show();
					                	Log.d("wifiloc", "Error Connecting to Server");
					                }

					            }
							}.execute();
							break;
						case R.id.disable_exist:
							map.clear();
							break;
					}
				}
			});
		}
	}
}
