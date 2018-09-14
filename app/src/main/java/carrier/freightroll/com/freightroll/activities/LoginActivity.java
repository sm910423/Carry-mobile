package carrier.freightroll.com.freightroll.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.BindView;
import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.api.APIInterface;
import carrier.freightroll.com.freightroll.api.APIManager;
import carrier.freightroll.com.freightroll.app.EndPoints;
import carrier.freightroll.com.freightroll.helpers.PreferenceManager;
import carrier.freightroll.com.freightroll.models.AuthModel;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "zzz LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int PASSWORD_MIN_LENGTH = 6;
//    private static final int PASSWORD_MAX_LENGTH = 10;

    @BindView(R.id.edt_email) EditText _emailText;
    @BindView(R.id.edt_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.header_btn_icon_back) ImageButton _backImageButton;
    @BindView(R.id.header_btn_text_back) Button _backTextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        _backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBack();
            }
        });
        _backTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBack();
            }
        });
    }

    public void goToBack() {
        finish();
    }

    public void login() {
        if (!validate()) {
//            onLoginFailed("");
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);

        // TODO: Implement your own authentication logic here.
        APIManager.post(EndPoints.LOGIN, params, new APIInterface() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                progressDialog.dismiss();
                onLoginSuccess(response);
            }

            @Override
            public void onSuccess(JSONArray response) throws JSONException {
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(JSONObject response) throws JSONException {
                progressDialog.dismiss();
                onLoginFailed(response.get("errorString").toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        // disable going back to the MainActivity
//        moveTaskToBack(true);
//    }

    public void onLoginSuccess(JSONObject response) throws JSONException {
        AuthModel model = new AuthModel();
        model.setUserId(response.getInt("user_id"));
        model.setToken(response.getString("auth_token"));

        PreferenceManager.setProfile(LoginActivity.this, model);

        _loginButton.setEnabled(true);

        Intent intent = new Intent(LoginActivity.this, TabsNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
//        Intent intent = getIntent();
//        setResult(RESULT_OK, intent);
//        finish();
    }

    public void onLoginFailed(String errorString) {
        Toast.makeText(getBaseContext(), errorString, Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < PASSWORD_MIN_LENGTH/* || password.length() > PASSWORD_MAX_LENGTH*/) {
            String msg = "greater than " + Integer.toString(PASSWORD_MIN_LENGTH) + " alphanumeric characters";
            _passwordText.setError(msg);
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
