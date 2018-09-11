package carrier.freightroll.com.freightroll.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.fragments.AccountFragment;
import carrier.freightroll.com.freightroll.fragments.BoardFragment;
import carrier.freightroll.com.freightroll.fragments.MessagesFragment;
import carrier.freightroll.com.freightroll.fragments.ShipmentsFragment;

public class TabsNavigationActivity extends AppCompatActivity {
    private enum TABS { BOARD, SHIPMENTS, MESSAGES, ACCOUNT };

    public TABS _sel_tab_index;
    public TABS _before_sel_tab_index;

    private FrameLayout _mainFrame;
    private BoardFragment _boardFragment;
    private ShipmentsFragment _shipmentsFragment;
    private MessagesFragment _messagesFragment;
    private AccountFragment _accountFragment;

    @BindView(R.id.nav_tab_board_nav) RelativeLayout _tab_board;
    @BindView(R.id.nav_tab_shipments_nav) RelativeLayout _tab_shipments;
    @BindView(R.id.nav_tab_messages_nav) RelativeLayout _tab_messages;
    @BindView(R.id.nav_tab_account_nav) RelativeLayout _tab_account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs_navigation);

        ButterKnife.bind(this);

        _sel_tab_index = _before_sel_tab_index = TABS.BOARD;

        _mainFrame = findViewById(R.id.main_frame);
        _boardFragment = new BoardFragment();
        _shipmentsFragment = new ShipmentsFragment();
        _messagesFragment = new MessagesFragment();
        _accountFragment = new AccountFragment();

        goToBoard();
        setFragment(_boardFragment);

        _tab_board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBoard();
            }
        });

        _tab_shipments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToShipments();
            }
        });

        _tab_messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMessages();
            }
        });

        _tab_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAccount();
            }
        });
    }

    public void goToBoard() {
        if (_sel_tab_index == TABS.BOARD) return;

        _sel_tab_index = TABS.BOARD;
        setTabStyle(_sel_tab_index, true);
        setTabStyle(_before_sel_tab_index, false);
        _before_sel_tab_index = _sel_tab_index;
    }

    public void goToShipments() {
        if (_sel_tab_index == TABS.SHIPMENTS) return;

        _sel_tab_index = TABS.SHIPMENTS;
        setTabStyle(_sel_tab_index, true);
        setTabStyle(_before_sel_tab_index, false);
        _before_sel_tab_index = _sel_tab_index;
    }

    public void goToMessages() {
        if (_sel_tab_index == TABS.MESSAGES) return;

        _sel_tab_index = TABS.MESSAGES;
        setTabStyle(_sel_tab_index, true);
        setTabStyle(_before_sel_tab_index, false);
        _before_sel_tab_index = _sel_tab_index;
    }

    public void goToAccount() {
        if (_sel_tab_index == TABS.ACCOUNT) return;

        _sel_tab_index = TABS.ACCOUNT;
        setTabStyle(_sel_tab_index, true);
        setTabStyle(_before_sel_tab_index, false);
        _before_sel_tab_index = _sel_tab_index;
    }

    public void setTabStyle(TABS index, boolean checked) {
        if (index == TABS.BOARD) {
            setTabBoard(checked);
            if (checked) setFragment(_boardFragment);
        } else if (index == TABS.SHIPMENTS) {
            setTabShipments(checked);
            if (checked) setFragment(_shipmentsFragment);
        } else if (index == TABS.MESSAGES) {
            setTabMessages(checked);
            if (checked) setFragment(_messagesFragment);
        } else if (index == TABS.ACCOUNT) {
            setTabAccount(checked, true);
            if (checked) setFragment(_accountFragment);
        }
    }

    public void setTabBoard(boolean checked) {
        ImageView icon = (ImageView) findViewById(R.id.nav_tab_board_icon);
        icon.setImageResource(checked == true ? R.drawable.board_white : R.drawable.board_gray);

        TextView text = (TextView) findViewById(R.id.nav_tab_board_text);
        text.setTextColor(getResources().getColor(checked == true ? R.color.colorTabCheckedColor : R.color.colorTabUncheckedColor));
    }

    public void setTabShipments(boolean checked) {
        ImageView icon = (ImageView) findViewById(R.id.nav_tab_shipments_icon);
        icon.setImageResource(checked == true ? R.drawable.shipments_white : R.drawable.shipments_gray);

        TextView text = (TextView) findViewById(R.id.nav_tab_shipments_text);
        text.setTextColor(getResources().getColor(checked == true ? R.color.colorTabCheckedColor : R.color.colorTabUncheckedColor));
    }

    public void setTabMessages(boolean checked) {
        ImageView icon = (ImageView) findViewById(R.id.nav_tab_messages_icon);
        icon.setImageResource(checked == true ? R.drawable.messages_white : R.drawable.messages_gray);

        TextView text = (TextView) findViewById(R.id.nav_tab_messages_text);
        text.setTextColor(getResources().getColor(checked == true ? R.color.colorTabCheckedColor : R.color.colorTabUncheckedColor));
    }

    public void setTabAccount(boolean checked, boolean available) {
        ImageView icon = (ImageView) findViewById(R.id.nav_tab_account_icon);
        if (available) {
            icon.setImageResource(checked == true ? R.drawable.account_white_on : R.drawable.account_gray_on);
        } else {
            icon.setImageResource(checked == true ? R.drawable.account_white_off : R.drawable.account_gray_off);
        }

        TextView text = (TextView) findViewById(R.id.nav_tab_account_text);
        text.setTextColor(getResources().getColor(checked == true ? R.color.colorTabCheckedColor : R.color.colorTabUncheckedColor));
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }
}
