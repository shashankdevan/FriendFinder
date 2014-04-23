package com.example.friendfinder;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by shashank on 4/21/14.
 */
public class SignUp extends Activity{

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
//        String[] params = getIntent().getStringArrayExtra("Credentials");
//
//
//        DownloadSession mysession = new DownloadSession();
//        mysession.delegate = this;
//        mysession.execute(params);
//        //new downloadSession().execute(params);
//    }
//    @Override
//    public void getData(String responseString) {
//        // TODO Auto-generated method stub
//        Toast.makeText(this,responseString, Toast.LENGTH_LONG).show();
//    }
//
//private static class DownloadSession extends AsyncTask<String, Integer, String>{
//
//    public SendData delegate;
//
//    @Override
//    protected void onPostExecute(String result) {
//        // TODO Auto-generated method stub
//        super.onPostExecute(result);
//        delegate.getData(result);
//    }
//
//    @Override
//    protected void onPreExecute() {
//        // TODO Auto-generated method stub
//        super.onPreExecute();
//    }
//
//    @Override
//    protected void onProgressUpdate(Integer... values) {
//        // TODO Auto-generated method stub
//        super.onProgressUpdate(values);
//    }
//
//    @Override
//    protected String doInBackground(String... arg0) {
//        String[] parameters = arg0;
//
//        String time = parameters[0];
//        String latitude = parameters[1];
//        String longitude = parameters[2];
//        String id = parameters[3];
//
//
//        HttpClient client = new DefaultHttpClient();
//
//        HttpPost post = new HttpPost("http://mpss.csce.uark.edu/~nilanb/dataupload.php");
//
//        List<NameValuePair> value = new LinkedList<NameValuePair>();
//        String  responseString = "";
//        value.add(new BasicNameValuePair("time", time));
//        value.add(new BasicNameValuePair("latitude", latitude));
//        value.add(new BasicNameValuePair("longitude", longitude));
//        value.add(new BasicNameValuePair("id", id));
//
//        try
//        {
//            post.setEntity(new UrlEncodedFormEntity(value));
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        try
//        {
//            ResponseHandler<String> responseHandler = new BasicResponseHandler();
//            responseString = client.execute(post, responseHandler);
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//
//
//        for(int i=0;i<100;i++)
//        {
//            try {
//                Thread.sleep(100);
//                publishProgress(i);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//
//        return responseString;
//    }

}