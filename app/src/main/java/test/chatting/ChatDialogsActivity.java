package test.chatting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import test.chatting.Adapter.ChatDialogsAdapters;
import test.chatting.Common.Common;
import test.chatting.Holder.QBChatDialogHolder;
import test.chatting.Holder.QBUnreadMessageHolder;
import test.chatting.Holder.QBUsersHolder;

public class ChatDialogsActivity extends AppCompatActivity implements QBSystemMessageListener,QBChatDialogMessageListener{

    FloatingActionButton floatingActionButton;
    ListView lstchatdialogs;

    @Override
    protected void onResume() {
        super.onResume();
        loadAllChatDialogs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialogs);

        createSessionForChat();

        lstchatdialogs = (ListView)findViewById(R.id.lstChatDialogs);
        lstchatdialogs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog =(QBChatDialog)lstchatdialogs.getAdapter().getItem(position);
                Intent intent = new Intent(ChatDialogsActivity.this,ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA,qbChatDialog);
                startActivity(intent);
            }
        });

        loadAllChatDialogs();


        floatingActionButton = (FloatingActionButton)findViewById(R.id.chatdialog_adduser);
        //directing user to select a friend to chat with
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChatDialogsActivity.this,ListUsersActivity.class);
                startActivity(intent);
            }
        });




    }

    private void loadAllChatDialogs() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);//get only the newest 100 dialogs

      QBRestChatService.getChatDialogs(null,requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
          @Override
          public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
            //creating adaper and setting it to list view

             /* //put all dialogs to cache
              QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);
              ChatDialogsAdapters adapter = new ChatDialogsAdapters(getBaseContext(),qbChatDialogs);
              lstchatdialogs.setAdapter(adapter);
              adapter.notifyDataSetChanged();*/

              //put all dialogs to cache
              QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);

              //unread settings
              Set<String> setIds = new HashSet<>();
              for (QBChatDialog chatDialog:qbChatDialogs)
                  setIds.add(chatDialog.getDialogId());

              //get messages unread
              QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle()).performAsync(new QBEntityCallback<Integer>() {
                  @Override
                  public void onSuccess(Integer integer, Bundle bundle) {

                      //save to cache
                      QBUnreadMessageHolder.getInstance().setBundle(bundle);

                      //Rfresh List Dialogs
                      ChatDialogsAdapters adapter = new ChatDialogsAdapters(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                      lstchatdialogs.setAdapter(adapter);
                      adapter.notifyDataSetChanged();

                  }

                  @Override
                  public void onError(QBResponseException e) {

                  }
              });




          }

          @Override
          public void onError(QBResponseException e) {

              Log.e("Error",e.getMessage());

          }
      });

    }

    private void createSessionForChat() {
        final ProgressDialog mDialog = new ProgressDialog(ChatDialogsActivity.this);
        mDialog.setMessage("please waiting....");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String user,password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        //load all user and save cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);

            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        final QBUser qbUser = new QBUser(user,password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {

                        mDialog.dismiss();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogsActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogsActivity.this);

                    }

                    @Override
                    public void onError(QBResponseException e) {

                        Log.e("ERROR",""+e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {

        //put dialog to cache because we send the system message with content DialogId so we can get
        //dialog by dialog Id

        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //put to cache
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource =QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogsAdapters adapters = new ChatDialogsAdapters(getBaseContext(),adapterSource);
                lstchatdialogs.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR",""+e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadAllChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
