package test.chatting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;

import test.chatting.Adapter.ChatMessageAdapter;
import test.chatting.Common.Common;
import test.chatting.Holder.QBChatMessagesHolder;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButton;
    EditText edtContent;

    ChatMessageAdapter adapter;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

       /* edtContent.addTextChangedListener(new TextWatcher() {

       @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length()==0){
                    submitButton.setEnabled(false);
                } else {
                    submitButton.setEnabled(true);
                }


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });*/



        initViews();
        initChatDialogs();
        retrieveMessage();

        //send messages feature
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(edtContent.getText().toString());
                chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                chatMessage.setSaveToHistory(true);

                try {
                    qbChatDialog.sendMessage(chatMessage);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

              /*  //put message to cache and refresh list view
                QBChatMessagesHolder.getInstance().putMessage(qbChatDialog.getDialogId(),chatMessage);
                ArrayList<QBChatMessage> messages =QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId());
                adapter = new ChatMessageAdapter(getBaseContext(),messages);
                lstChatMessages.setAdapter(adapter);
                adapter.notifyDataSetChanged();*/

              //fix private chat dont show a message
                if(qbChatDialog.getType() == QBDialogType.PRIVATE)
                {
                    QBChatMessagesHolder.getInstance().putMessage(qbChatDialog.getDialogId(),chatMessage);
                    ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(chatMessage.getDialogId());

                    adapter = new ChatMessageAdapter(getBaseContext(),messages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                //remove the text from edit text after being sent
                edtContent.setText("");
                edtContent.setFocusable(true);
            }
        });
    }

    private void retrieveMessage() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500);//get last 500 messages
        if (qbChatDialog != null)
        {
            QBRestChatService.getDialogMessages(qbChatDialog,messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //put messages to cache
                    QBChatMessagesHolder.getInstance().putMessages(qbChatDialog.getDialogId(),qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(),qbChatMessages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    private void initChatDialogs() {
        qbChatDialog =(QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());
        //register listener incoming

        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //joining a group chat
        if(qbChatDialog.getType()== QBDialogType.PUBLIC_GROUP || qbChatDialog.getType()==QBDialogType.GROUP)
        {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d("ERROR",""+e.getMessage());
                }
            });
        }

        qbChatDialog.addMessageListener(this);
    }

    private void initViews() {
        lstChatMessages = (ListView)findViewById(R.id.list_of_message);
        submitButton = (ImageButton)findViewById(R.id.send_button);
        edtContent = (EditText) findViewById(R.id.edt_content);
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //cache Message
        QBChatMessagesHolder.getInstance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(),messages);
        lstChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("ERROR",""+e.getMessage());
    }
}
