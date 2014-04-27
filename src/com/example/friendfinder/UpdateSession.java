package com.example.friendfinder;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static com.example.friendfinder.Global.UPDATE_URL;

public class UpdateSession extends AsyncTask<String, Integer, ServerResponse> {
    public DataReceiver delegate;

    @Override
    protected ServerResponse doInBackground(String... params) {
        ServerResponse serverResponse = null;

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(UPDATE_URL);

        List<NameValuePair> value = new LinkedList<NameValuePair>();
        value.add(new BasicNameValuePair("time", params[0]));
        value.add(new BasicNameValuePair("latitude", params[1]));
        value.add(new BasicNameValuePair("longitude", params[2]));
        value.add(new BasicNameValuePair("id", params[3]));

        try {
            post.setEntity(new UrlEncodedFormEntity(value));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpResponse httpResponse = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String responseLine;
            String responseString = "";
            while ((responseLine = reader.readLine()) != null)
                responseString += responseLine + "\n";
            serverResponse = new ServerResponse(httpResponse.getStatusLine().getStatusCode(), responseString);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverResponse;
    }

    @Override
    protected void onPostExecute(ServerResponse response) {
        super.onPostExecute(response);
        delegate.receive(response);
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(values);
    }

}
