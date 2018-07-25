package io.nambm.sqlitetesting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.nambm.sqlitetesting.model.UserAdapter;
import io.nambm.sqlitetesting.model.UserModel;
import io.nambm.sqlitetesting.storage.UserRepository;

public class MainActivity extends AppCompatActivity {

    private static final int UPDATE_CODE = 0;
    private static final int ADD_CODE = 1;

    private ListView listView;
    private EditText editSearch;
    private Button btnNew;

    private UserRepository userRepository;
    private List<UserModel> users = new ArrayList<>();
    private List<UserModel> allUsers;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        wireUp();
    }

    private void init() {
        this.listView = findViewById(R.id.userList);
        this.editSearch = findViewById(R.id.editSearch);
        this.btnNew = findViewById(R.id.btnNew);
        this.userRepository = new UserRepository(this);
    }

    private void wireUp() {
        this.btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserDetailActivity.getUserDetailIntent(MainActivity.this, null);
                startActivityForResult(intent, ADD_CODE);
            }
        });

        users = userRepository.queryAll();
        allUsers = new ArrayList<>(users);

        userAdapter = new UserAdapter(users, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = UserAdapter.getUsernameFromConvertView(v);
                UserModel userModel = userRepository.getByUsername(username);

                Intent intent = UserDetailActivity.getUserDetailIntent(MainActivity.this, userModel);
                startActivityForResult(intent, UPDATE_CODE);
            }
        }, this);
        this.listView.setAdapter(userAdapter);

        this.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    userAdapter.getUsers().clear();
                    userAdapter.getUsers().addAll(allUsers);
                    userAdapter.notifyDataSetChanged();
                } else {
                    userAdapter.getUsers().clear();
                    for (UserModel user : allUsers) {
                        if (user.getUsername().toLowerCase().contains(s.toString().toLowerCase())
                                || user.getFullName().toLowerCase().contains(s.toString().toLowerCase())) {
                            userAdapter.getUsers().add(user);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_CODE) {
            if (resultCode == RESULT_OK) {
                allUsers = userRepository.queryAll();
                userAdapter.getUsers().clear();
                userAdapter.getUsers().addAll(allUsers);
                userAdapter.notifyDataSetChanged();

                Toast.makeText(this, "Update successfully", Toast.LENGTH_SHORT).show();
            } else if (resultCode == UserDetailActivity.RESULT_INSERT_ERROR) {
                Toast.makeText(this, "Error while updating", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == ADD_CODE) {
            if (resultCode == RESULT_OK) {
                allUsers = userRepository.queryAll();
                userAdapter.getUsers().clear();
                userAdapter.getUsers().addAll(allUsers);
                userAdapter.notifyDataSetChanged();

                Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
            } else if (resultCode == UserDetailActivity.RESULT_INSERT_ERROR) {
                Toast.makeText(this, "Error while adding", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        String username = UserAdapter.getUsernameFromConvertView(v);
        UserModel selectedUser = new UserModel();
        selectedUser.setUsername(username);
        userAdapter.setSelectedUser(selectedUser);

        menu.setQwertyMode(true);
        menu.setHeaderTitle("Delete user?");
        MenuItem item = menu.add(0, 0, 0, "Delete");
        item.setAlphabeticShortcut('1');
        menu.add(0, 1, 1, "Cancel");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                UserModel selectedUser = userAdapter.getSelectedUser();
                userRepository.delete(selectedUser);
                userAdapter.deleteSelectedUser();
                userAdapter.notifyDataSetChanged();
                Toast.makeText(this, "User is deleted", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRepository.closeRepo();
    }
}
