package carrier.freightroll.com.freightroll.api;

import android.app.Application;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import carrier.freightroll.com.freightroll.app.EndPoints;
import carrier.freightroll.com.freightroll.helpers.PreferenceManager;

public class APIService {
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("X-Freightroll-Token", "4xoyc-DZnibCm3hrvmDT");
        client.addHeader("X-Freightroll-Client-Id", "77ngxng42vb4k7pdzc89nn");
        client.setTimeout(5*60*1000);
        client.setConnectTimeout(5*60*1000);
        client.setResponseTimeout(5*60*1000);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("X-Freightroll-Token", "4xoyc-DZnibCm3hrvmDT");
        client.addHeader("X-Freightroll-Client-Id", "77ngxng42vb4k7pdzc89nn");
        client.setTimeout(5*60*1000);
        client.setConnectTimeout(5*60*1000);
        client.setResponseTimeout(5*60*1000);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return EndPoints.BACKEND_BASE_URL + relativeUrl;
    }
}
