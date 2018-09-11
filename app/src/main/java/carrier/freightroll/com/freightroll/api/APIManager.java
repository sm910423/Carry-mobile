package carrier.freightroll.com.freightroll.api;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class APIManager {
    public static void getDataFromServer (String url, RequestParams params, final APIInterface callback){
        APIService.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String e, Throwable throwable) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("errorString", e.toString());
                    callback.onFailure(object);
                } catch (JSONException _e) {
                    _e.printStackTrace();
                }
//                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    callback.onSuccess(response);
                } catch (JSONException e) {
                    Log.d("zzz result2", e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                try {
                    callback.onFailure(e);
                } catch (JSONException _e) {
                    _e.printStackTrace();
                }
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
}
