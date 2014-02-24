package edu.ncsu.wifilocator;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class MainApplication extends Application {
	
	WifiManager wifi;
	BroadcastReceiver wifiDataReceiver = null;
	DefaultHttpClient httpClient;
	Timer timer;
	
	public MainActivity mainActivity = null;
	boolean shouldScan = false;
	
	// url to get the estimated position
    private static String url_position = "http://people.engr.ncsu.edu/ywang51/nprg/get_position.php";
    
    // JSON Node names
    private static final String TAG_POINTS = "coordinate";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    
    // contacts JSONArray
    JSONArray position = null;
    
    private int timer_t = 4000;
    
    @Override
	public void onCreate() {
		super.onCreate();
		//Log.d(APP_NAME, "APPLICATION onCreate");
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            //Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            //wifi.setWifiEnabled(true);
        } 
        
        // http://stackoverflow.com/questions/2253061/secure-http-post-in-android
     	/*HttpParams params = new BasicHttpParams();

     	HttpConnectionParams.setConnectionTimeout(params, 10000);
     	HttpConnectionParams.setSoTimeout(params, 10000);

     	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
     	HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
     	HttpProtocolParams.setUseExpectContinue(params, true);

     	SchemeRegistry schReg = new SchemeRegistry();
     	schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
     	//schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
     	ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

     	httpClient = new DefaultHttpClient(conMgr, params);*/
        
        // Register to get the Signal Strengths
     	wifiDataReceiver = new BroadcastReceiver(){
     		@Override
     		public void onReceive(Context c, Intent intent){			            	
     			if(shouldScan){
     				// Scan signal strengths if it is time
     			    List<ScanResult> results = wifi.getScanResults();
     			    
     			    String postParameters = "success=1";
     			    //JSONObject jsonObjSend = new JSONObject();
     			    
     			    for(int i = 0; i < results.size(); i++){
     			    	ScanResult result = results.get(i);
     			    	String parameters = "&";
     			    	//N.C. State access points only
     			    	//if(!result.SSID.equals("ncsu") && !result.SSID.equals("ncsu-guest")){
							// Skip this one
     			    		//continue;
						//}

     			    	
     			    	/*try {
     			    		JSONObject entry = new JSONObject();
     			    		entry.put("mac", result.BSSID);
     			    		entry.put("strength", Integer.toString(result.level));
     			            jsonObjSend.put("AP", entry);
     			            //Log.d("wifiloc", jArray.toString());

     			        } catch (Exception e) {
     			            Log.e("wifiloc", "" + e.getMessage());
     			        }*/
     			    	
						// Add this signal strength reading as a parameter where the name is the BSSID
						String measurementString = result.BSSID + "=" + Integer.toString(result.level);
						//if(first){
							//postParameters = measurementString;
							//first = false;
						//}
						//else{
							parameters = parameters + measurementString;
							postParameters = postParameters + parameters;
						//}
     			    }
     			   //Toast.makeText(getBaseContext(), postParameters, Toast.LENGTH_LONG).show();
     			    //Log.d("wifiloc", jsonObjSend.toString());
     			    //send data to the server
     			   new PostJSONDataAsyncTask(c, postParameters, url_position, false){
    	                // Override the onPostExecute to do whatever you want
    	                @Override
    	                protected void onPostExecute(String response)
    	                {
    	                    super.onPostExecute(response);
    	                    
    	                    if (response != null){
    	                    	Log.d("wifiloc", response);
    	                    	
    	                    	JSONObject json = null;
    	                    	
    	                    	try{
    	                    		json = new JSONObject(response);
    	                    	} catch (JSONException e){
    	                            Log.d("wifiloc", e.toString());
    	                    	}
    	            			
    	            			if(json == null){
    	                        	Log.d("wifiloc", "Error parsing server response");
    	                            return;
    	                        }
    	            			
    	                        // If returned object length is 
    	                        if(json.length() > 0){
    	                			try {
    	                				if (json.has("error")){
        	                        		String errorMessage = json.getString("error").toString();
        	                                // Check errors
        	                                if(errorMessage.equalsIgnoreCase("Could not find any matches in the db"))
        	                                {
        	                                	if(mainActivity != null)
            	                                {
            	                					Log.d("wifiloc", errorMessage);
            	                                    mainActivity.updateStatus(errorMessage);
            	                                }
        	                                }
        	                        	}
        	                        	else{
        	                        		double lat = json.getDouble(TAG_LAT);
        	                				double lng = json.getDouble(TAG_LNG);
        	                				LatLng coordinate = new LatLng(lat, lng);
        	                				//Log.d("wifiloc", lat + " $$ " + lng);
        	                				
        	                				if(mainActivity != null)
        	                                {
        	                					Log.d("wifiloc", "good to draw");
        	                                    mainActivity.updateLocation(coordinate);
        	                                }
        	                	            // Getting Array of existing points
        	                	            //position = json.getJSONArray(TAG_POINTS);
        	                	             
        	                	            // looping through All points
        	                	            /*for(int i = 0; i < points.length(); i++){
        	                	                JSONObject c = points.getJSONObject(i);
        	                	                 
        	                	                // Storing each json item in variable
        	                	                String val1 = c.getString("val1");
        	                	                String val2 = c.getString("val2");
        	                	                
        	                	                // adding each coordinate to ArrayList
        	                	                Log.d("test", val1 + " $$ " + val2);
        	                	            }*/
        	                        	}
    	                	        } catch (JSONException e) {
    	                	            e.printStackTrace();
    	                	        }
    	                        }
    	                        else {
    	                            //TODO Do something here if no teams have been made yet
    	                        }
    	                    }
    	                    else
    	                    {
    	                        // Toast.makeText(context, "Error connecting to server", Toast.LENGTH_LONG).show();
    	                    	Log.d("wifiloc", "Error Connecting to Server");
    	                    }
    	                    
    	                    //Log.d("wifiloc", "Update Success");
    	                }
    	            }.execute();
					
                    //Log.d("wifiloc", "Successfully sent location info! :D");
     			    
                    shouldScan = false;
     			}
     		}
     	};
     		    
     	registerReceiver(wifiDataReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); 
     	
     	timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateLocationTask(), 50, timer_t);
        
	}

	@Override
	public void onTerminate() {
		// This doesn't get called on real Android Devices... See docs
		//Log.d(APP_NAME, "APPLICATION onTerminate");
		unregisterReceiver(wifiDataReceiver);
    	wifiDataReceiver = null;
    	timer.cancel();
		super.onTerminate();      
	}
	
	public void updateTimerInterval(int t){
		timer_t = t;
		timer.cancel();
		timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateLocationTask(), 50, timer_t);
	}
	
	class UpdateLocationTask extends TimerTask {
        public void run() {
        	shouldScan = true;
        	if(wifi.startScan() == true){
        		// Great
        	} else {
        		// TODO We could do something like try again in 5 seconds
        	}
        }
    }
}
