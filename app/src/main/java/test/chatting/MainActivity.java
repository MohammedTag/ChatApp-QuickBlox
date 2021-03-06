package test.chatting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {

    static final String APP_ID ="56206";
    static final String AUTH_KEY ="sz36XHJBTKLBjjG";
    static final String AUTH_SECRET ="SPjBtxrFzp2eX6X";
    static final String ACCOUNT_KEY ="NF33tj9gxW5upTf9d8sM";

    Button btnLogin,btnSignUp;
    EditText edtUser,edtPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //APP VERIFICATION
        initializeFrameWork();

        btnLogin = (Button)findViewById(R.id.main_btnLogin);
        btnSignUp = (Button)findViewById(R.id.main_btnSignup);

        edtPassword =(EditText)findViewById(R.id.main_editPassword);
        edtUser = (EditText) findViewById(R.id.main_editLogin);

        //starting new activity
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SignUpActvity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = edtUser.getText().toString();
                final String password = edtPassword.getText().toString();

                QBUser qbUser = new QBUser(user,password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(),"login successfully",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this,ChatDialogsActivity.class);
                        intent.putExtra("user",user);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();//close login activity after logging in successfully
                    }


                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(),""+e.getErrors(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
    //APP VERIFICATION

    private void initializeFrameWork() {
        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
