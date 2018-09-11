package carrier.freightroll.com.freightroll.models;

public class AuthModel {
    private int _iUserId;
    private String _strToken;

    public int getUserId() { return _iUserId; }

    public void setUserId(int iUserId) { _iUserId = iUserId; }

    public String getToken() { return _strToken; }

    public void setToken(String strToken) { _strToken = strToken; }
}
