package net.macdidi5.picomfire;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class ConnectActivity extends AppCompatActivity {

    private EditText app_url, email, password;
    // 登入中與登入表單元件
    private View progress, login_form;
    // 執行登入工作的執行緒
    private UserLoginTask userLoginTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        checkNetwork();
        processViews();
    }

    public void clickConnect(View view) {
        processLogin();
    }

    private void checkNetwork() {
        if (!TurtleUtil.checkNetwork(this)) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(R.string.connection_require);
            ab.setTitle(android.R.string.dialog_alert_title);
            ab.setIcon(android.R.drawable.ic_dialog_alert);
            ab.setCancelable(false);
            ab.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            ab.show();
        }
    }

    private void processViews() {
        progress = findViewById(R.id.progress);
        login_form = findViewById(R.id.login_form);

        app_url = (EditText)findViewById(R.id.app_url);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);

        app_url.setText(TurtleUtil.getPref(this, TurtleUtil.KEY_APP_URL, "http://picom.firebaseio.com"));
        email.setText(TurtleUtil.getPref(this, TurtleUtil.KEY_EMAIL, "demo@macdidi5.net"));
        password.setText(TurtleUtil.getPref(this, TurtleUtil.KEY_PASSWORD, "12345678"));
    }

    // 執行登入
    private void processLogin() {
        hideSoftKeyboard(this);

        app_url.setError(null);
        email.setError(null);
        password.setError(null);

        String appUrlValue = app_url.getText().toString();
        String emailValue = email.getText().toString();
        String passwordValue = password.getText().toString();

        View focusView = null;

        // 檢查輸入的內容
        focusView = checkEmpty(app_url, appUrlValue, "This field is required");

        if (focusView == null) {
            focusView = checkEmpty(email, emailValue, "This field is required");
        }

        if (focusView == null) {
            focusView = checkEmpty(password, passwordValue, "This field is required");
        }

        // 如果輸入的內容有錯誤
        if (focusView != null) {
            focusView.requestFocus();
        } else {
            TurtleUtil.savePref(this, TurtleUtil.KEY_APP_URL, appUrlValue);
            TurtleUtil.savePref(this, TurtleUtil.KEY_EMAIL, emailValue);
            TurtleUtil.savePref(this, TurtleUtil.KEY_PASSWORD, passwordValue);

            // 顯示登入中
            showProgress(true);
            // 建立與啟動執行登入工作的執行緒
            userLoginTask = new UserLoginTask(appUrlValue, emailValue, passwordValue);
            userLoginTask.execute();
        }
    }

    private View checkEmpty(EditText et, String value, String message) {
        View result = null;

        if (TextUtils.isEmpty(value)) {
            et.setError(message);
            result = et;
        }

        return result;
    }

    // 顯示登入中與切換登入表單
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            login_form.setVisibility(show ? View.GONE : View.VISIBLE);
            login_form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    login_form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            login_form.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // 執行登入工作的執行緒類別
    public class UserLoginTask extends AsyncTask<Void, Void, Void> {

        private final String appUrlValue;
        private final String emailValue;
        private final String passwordValue;

        UserLoginTask(String appUrlValue, String emailValue, String passwordValue) {
            this.appUrlValue = appUrlValue;
            this.emailValue = emailValue;
            this.passwordValue = passwordValue;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // 建立 Firebase 物件
            Firebase firebaseRef = new Firebase(appUrlValue);
            // 建立登入用的 Firebase 物件
            Firebase child = firebaseRef.getRoot().child(".info/connected");

            // 宣告與建立登入驗證監聽物件
            Firebase.AuthResultHandler arh = new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    // 登入成功
                    progress.setVisibility(View.GONE);
                    Intent intent = getIntent();
                    intent.putExtra("appUrlValue", appUrlValue);
                    intent.putExtra("emailValue", emailValue);
                    intent.putExtra("passwordValue", passwordValue);

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    // 登入失敗
                    showProgress(false);
                    password.setError("This password is incorrect");
                    password.requestFocus();
                }
            };

            // 註冊登入驗證監聽事件
            child.authWithPassword(emailValue, passwordValue, arh);

            return null;
        }

        @Override
        protected void onCancelled() {
            userLoginTask = null;
            showProgress(false);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}
