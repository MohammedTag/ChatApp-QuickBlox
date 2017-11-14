package test.chatting;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

import test.chatting.Adapter.ListUsersAdapter;
import test.chatting.Common.Common;
import test.chatting.Holder.QBUsersHolder;

public class ListUsersActivity extends AppCompatActivity {

    ListView lstUsers;
    Button btnCreateChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        retrieveAllUsers();
        lstUsers = (ListView)findViewById(R.id.lstUsers);
        lstUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        btnCreateChat = (Button)findViewById(R.id.btn_create_chat);
        btnCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int countChoice = lstUsers.getCount();
                //set condition if user select one friend then its private chat if more then its group chat
                if(lstUsers.getCheckedItemPositions().size()==1)
                
                    createPrivateChat(lstUsers.getCheckedItemPositions());
                
                else if (lstUsers.getCheckedItemPositions().size()>1)
                    
                    createGroupChat(lstUsers.getCheckedItemPositions());
                else
                    Toast.makeText(ListUsersActivity.this,"please select a friend or a group of friends to chat with",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog mDialog = new ProgressDialog(ListUsersActivity.this);
        mDialog.setMessage("please wait..");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countChoice = lstUsers.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for (int i=0; i<countChoice;i++ )
        {
            if (checkedItemPositions.get(i))
            {
                QBUser user = (QBUser) lstUsers.getItemAtPosition(i);
                occupantIdsList.add(user.getId());
            }
        }
        //create chat dialog
        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.createChatDialogName(occupantIdsList));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

               mDialog.dismiss();
                Toast.makeText(getBaseContext(),"Created group chat dialog successfully",Toast.LENGTH_SHORT).show();

                //send system message to recepient
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for (int i=0 ; i < qbChatDialog.getOccupants().size();i++)
                {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));

                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
                //qbChatMessage.setBody(qbChatDialog.getDialogId());
               /* try {
                    qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }*/


                finish();

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());
            }
        });

    }

    //buildinga private chat
    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog mDialog = new ProgressDialog(ListUsersActivity.this);
        mDialog.setMessage("please wait..");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countChoice = lstUsers.getCount();
        for (int i=0; i<countChoice;i++ )
        {
            if (checkedItemPositions.get(i))
            {
                final QBUser user = (QBUser) lstUsers.getItemAtPosition(i);
                QBChatDialog dialog = DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                        mDialog.dismiss();
                        Toast.makeText(getBaseContext(),"Created private chat dialog successfully",Toast.LENGTH_SHORT).show();

                        //send system message to recepient

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR",e.getMessage());
                    }
                });

            }
        }

    }


    private void retrieveAllUsers() {

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                //adding cache holder because @ common.creatdialogname method we're using data from cache
                //if not the data will always be set to null therefore app crash will occur

                QBUsersHolder.getInstance().putUsers(qbUsers);

                //creating new array list to add all users from web servecies execluding current user
                ArrayList<QBUser> qbUserWithoutCurrent = new ArrayList<QBUser>();
                for(QBUser user : qbUsers)
                {
                    if(!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                        qbUserWithoutCurrent.add(user);
                }
                //creatinga adapter to show at list view
                ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),qbUserWithoutCurrent);
                lstUsers.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

                Log.e("Error",e.getMessage());
            }
        });
    }


}
