package carrier.freightroll.com.freightroll.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import butterknife.ButterKnife;
import butterknife.BindView;
import carrier.freightroll.com.freightroll.R;

public class SignupActivity extends AppCompatActivity {
    @BindView(R.id.header_btn_icon_back) ImageButton _backImageButton;
    @BindView(R.id.header_btn_text_back) Button _backTextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

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
}
