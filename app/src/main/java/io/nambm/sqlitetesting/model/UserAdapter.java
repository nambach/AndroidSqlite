package io.nambm.sqlitetesting.model;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.nambm.sqlitetesting.R;

public class UserAdapter extends BaseAdapter {

    private List<UserModel> users;
    private UserModel selectedUser;
    private View.OnClickListener listener;
    private View.OnCreateContextMenuListener contextMenuListener;

    public UserAdapter(List<UserModel> users, View.OnClickListener listener, View.OnCreateContextMenuListener contextMenuListener) {
        this.users = users;
        this.listener = listener;
        this.contextMenuListener = contextMenuListener;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.user_list_entry, parent, false);
        }

        UserModel userModel = users.get(position);

        ((TextView)convertView.findViewById(R.id.entry_title)).setText(userModel.getUsername());
        ((TextView)convertView.findViewById(R.id.entry_subtitle)).setText(userModel.getFullName());
        convertView.setOnClickListener(listener);
        convertView.setOnCreateContextMenuListener(contextMenuListener);

        return convertView;
    }

    public List<UserModel> getUsers() {
        return users;
    }

    public UserModel getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(UserModel selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void deleteSelectedUser() {
        if (selectedUser != null) {
            UserModel userModel = null;
            for (UserModel user : users) {
                if (user.getUsername().equals(selectedUser.getUsername())) {
                    userModel = user;
                    break;
                }
            }
            if (userModel != null) {
                users.remove(userModel);
            }
        }
    }

    public static String getUsernameFromConvertView(View view) {
        return ((TextView)view.findViewById(R.id.entry_title)).getText().toString();
    }
}
