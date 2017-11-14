package test.chatting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpActvity extends AppCompatActivity {

    Button btnSignUp,btnCancel;
    EditText edtUSer,edtPassword,edtFullName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_actvity);

        registerSession();

        btnSignUp = (Button)findViewById(R.id.signup_btnSignUp);
        btnCancel = (Button)findViewById(R.id.signup_btnCancel);


        edtUSer = (EditText) findViewById(R.id.signup_editLogin);
        edtPassword = (EditText) findViewById(R.id.signup_editPassword);
        edtFullName = (EditText) findViewById(R.id.signup_editFullName);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user =  edtUSer.getText().toString();
                String password = edtPassword.getText().toString();

                QBUser qbUser = new QBUser(user,password);

                qbUser.setFullName(edtFullName.getText().toString());

                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(),"Signed Up Scuccessfully",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                        Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    private void registerSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {

                Log.e("ERROR",e.getMessage());
            }
        });
    }
}
