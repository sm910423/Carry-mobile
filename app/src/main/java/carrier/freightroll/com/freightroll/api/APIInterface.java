package carrier.freightroll.com.freightroll.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface APIInterface {
    void onSuccess(JSONObject param) throws JSONException;
    void onSuccess(JSONArray param) throws JSONException;
    void onFailure(JSONObject param) throws JSONException;
}
