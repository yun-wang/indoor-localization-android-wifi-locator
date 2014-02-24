package edu.ncsu.wifilocator;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PostJSONDataAsyncTask extends AsyncTask<Object, Void, String>{
    //private Exception exception;
    private Context context;
    public ProgressDialog dialog;
    private String string;
    private String postURL;
    private boolean showProgressDialog;
    
    /**
     * 
     * @param context
     * @param jsonObject // the object to post
     * @param postURL    // the address to post to
     */
    public PostJSONDataAsyncTask(Context context, String string, String postURL, boolean showProgressDialog){
        this.context = context;
        this.string = string;
        this.postURL = postURL;
        this.showProgressDialog = showProgressDialog;
    }
    
    @Override
    protected void onPreExecute(){
        this.dialog = new ProgressDialog(context);
        this.dialog.setMessage("Updating");
        if (showProgressDialog) {
            this.dialog.show();
        }
    }
    
    protected String doInBackground(Object... arg){

    	try{
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = null;
            
            // If this is null, it means we'll just execute HTTP directly without POST
            if(string == null){
                // response = httpclient.execute(new HttpGet(postURL));
                responseBody = httpclient.execute(new HttpGet(postURL), responseHandler);
            }
            // Execute HTTP POST
            else {
            	
            	HttpPost httppost = new HttpPost(postURL);
            	StringEntity tmp = null;
            	httppost.setHeader("User-Agent", "Agent_Smith");
				// TODO Can we not set
				httppost.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
				httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
				tmp = new StringEntity(string, "UTF-8");
				httppost.setEntity(tmp);
            	//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				//nameValuePairs.add(new BasicNameValuePair("name", string));
				//Log.d("test", valueIWantToSend + "~~~~~~");
				//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				/*for(int i = 0; i < nameValuePairs.size(); i++){
					Log.d("test", nameValuePairs.get(i).toString());
				}*/
			
                /*JSONArray postjson = new JSONArray();
                postjson.put(jsonObject);
    
                // Post the data:
                
                httppost.setHeader("json", jsonObject.toString());
                httppost.getParams().setParameter("jsonpost", postjson);
    
                // Execute HTTP Post Request
                Log.d("sigstr", jsonObject.toString());*/
    
                // response = httpclient.execute(httppost);
                responseBody = httpclient.execute(httppost, responseHandler);
                Log.d("wifiloc", responseBody);
            }
            
            return responseBody;
        }
        catch (Exception e){
            Log.e("", e.toString());
            //this.exception = e;
            return null;
        }
    }
    
    // This should be overridden on each 
    protected void onPostExecute(String response){
        if(dialog.isShowing()){
            dialog.dismiss();
        }
    }

}
