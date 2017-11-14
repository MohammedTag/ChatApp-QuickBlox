package test.chatting.Holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by moshw on 4/17/2017.
 */

public class QBChatMessagesHolder {

    private static QBChatMessagesHolder instance;

    private HashMap<String,ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized QBChatMessagesHolder getInstance()
    {

        QBChatMessagesHolder qbChatMessagesHolder;
        synchronized (QBChatMessagesHolder.class)
        {
            if (instance == null)
                instance = new QBChatMessagesHolder();
            qbChatMessagesHolder = instance;

        }
        return qbChatMessagesHolder;
    }

    private QBChatMessagesHolder()
    {
        this.qbChatMessageArray  = new HashMap<>();
    }

    public void putMessages(String dialogId,ArrayList<QBChatMessage> qbChatMessages)
    {
        this.qbChatMessageArray.put(dialogId,qbChatMessages);
    }

    public void putMessage(String dialogId,QBChatMessage qbChatMessage)
    {
        List<QBChatMessage> lstresult = (List)this.qbChatMessageArray.get(dialogId);
        lstresult.add(qbChatMessage);
        ArrayList<QBChatMessage> lstAdded = new ArrayList(lstresult.size());
        lstAdded.addAll(lstresult);
        putMessages(dialogId,lstAdded);
    }

    public ArrayList<QBChatMessage> getChatMessagesByDialogId(String dialogId)
    {
        return (ArrayList<QBChatMessage>)this.qbChatMessageArray.get(dialogId);
    }

}
