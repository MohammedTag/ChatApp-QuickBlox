package test.chatting.Common;

import android.content.Intent;

import com.quickblox.users.model.QBUser;

import java.util.List;

import test.chatting.Holder.QBUsersHolder;

/**
 * Created by moshw on 4/16/2017.
 */

public class Common {
    public static  final String DIALOG_EXTRA = "Dialogs";

    public  static String createChatDialogName(List<Integer> qbUsers)
    {
       List<QBUser> qbUsers1 = QBUsersHolder.getInstance().getUsersByIds(qbUsers);
        StringBuilder name = new StringBuilder();
        //name of chat of all useres and if its over 30 "..." will be put at end
        for (QBUser user:qbUsers1)

            name.append(user.getFullName()).append("...");
            if (name.length()>30)

                name = name.replace(30,name.length()-1,"...");
                return name.toString();


    }
            //creating name of chat dialog
}
