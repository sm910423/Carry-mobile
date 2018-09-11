package carrier.freightroll.com.freightroll.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import carrier.freightroll.com.freightroll.models.AuthModel;

public class PreferenceManager {
    private static SharedPreferences mSharedPreferences;

    private static final String PREFERENCE_USER_INFO = "PREFERENCE_USER_INFO";
    private static final String KEY_TOKEN = "KEY_TOKEN";
    private static final String KEY_USERID = "KEY_USERID";

    private static final String PREFERENCE_SETTINGS = "PREFERENCE_SETTINGS";
    private static final String KEY_DISTANCE = "KEY_DISTANCE";
    private static final String KEY_TRUCK = "KEY_TRUCK";
    private static final String KEY_DATE = "KEY_DATE";

    public static String getToken(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_TOKEN, null);
    }

    public static void setToken(Context mContext, String token) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public static int getUserID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt(KEY_USERID, 0);
    }

    public static void setUserID(Context mContext, int userID) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_USERID, userID);
        editor.apply();
    }

    public static void setProfile(Context mContext, AuthModel model) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        editor.putString(KEY_TOKEN, model.getToken());
        editor.putInt(KEY_USERID, model.getUserId());

        editor.apply();
    }

    public static AuthModel getProfile(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_USER_INFO, Context.MODE_PRIVATE);

        AuthModel model = new AuthModel();
        model.setToken(mSharedPreferences.getString(KEY_TOKEN, null));
        model.setUserId(mSharedPreferences.getInt(KEY_USERID, 0));

        return model;
    }

    public static int getDistance(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt(KEY_DISTANCE, 0);
    }

    public static void setDistance(Context mContext, int distance) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_DISTANCE, distance);
        editor.apply();
    }

    public static int getTruck(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt(KEY_TRUCK, 0);
    }

    public static void setTruck(Context mContext, int truck) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_TRUCK, truck);
        editor.apply();
    }

    public static String getDate(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        return  mSharedPreferences.getString(KEY_DATE, null);
    }

    public static void setDate(Context mContext, String date) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_DATE, date);
        editor.apply();
    }

    public static boolean removeAllPreferences(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        return  editor.commit();
    }

}
