package io.nambm.sqlitetesting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.nambm.sqlitetesting.model.UserModel;
import io.nambm.sqlitetesting.storage.UserRepository;

public class UserDetailActivity extends AppCompatActivity {

    public static final int RESULT_INSERT_ERROR = 2;
    private static final String USER_MODEL = "userModel";
    private static final String UPDATE = "update";
    private static final String ADD = "add";

    private EditText editUsername;
    private EditText editFullName;
    private EditText editEmail;
    private TextView txtStatus;

    private Button btnAdd;
    private Button btnUpdate;
    private Button btnBack;

    private TextView labelPassword;
    private EditText editPassword;

    private UserModel userModel = null;

    public static Intent getUserDetailIntent(Context context, UserModel userModel) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(USER_MODEL, userModel);
        return intent;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail);

        init();
        wireUp();

        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra(USER_MODEL) != null) {
            userModel = (UserModel) intent.getSerializableExtra(USER_MODEL);

            editUsername.setText(userModel.getUsername());
            editFullName.setText(userModel.getFullName());
            editEmail.setText(userModel.getEmail());
            txtStatus.setText("Status: " + (userModel.isActive() ? "Active" : "Inactive"));

            switchMode(UPDATE);
        } else {
            switchMode(ADD);
        }
    }

    private void init() {
        editUsername = findViewById(R.id.editUsername);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        txtStatus = findViewById(R.id.txtStatus);
        btnBack = findViewById(R.id.btnBackEdit);
        btnUpdate = findViewById(R.id.btnUpdateEdit);
        btnAdd = findViewById(R.id.btnAdd);

        labelPassword = findViewById(R.id.labelPassword);
        editPassword = findViewById(R.id.editPassword);
    }

    private void wireUp() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean rs = false;
                if (userModel != null) {
                    userModel.setFullName(editFullName.getText().toString());
                    userModel.setEmail(editEmail.getText().toString());

                    UserRepository userRepository = new UserRepository(UserDetailActivity.this);
                    rs = userRepository.update(userModel, "fullName", "email");
                    userRepository.closeRepo();
                }

                setResult((rs ? RESULT_OK : RESULT_INSERT_ERROR));
                UserDetailActivity.this.finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean rs;
                userModel = new UserModel();
                userModel.setUsername(editUsername.getText().toString());
                userModel.setPassword(editPassword.getText().toString());
                userModel.setFullName(editFullName.getText().toString());
                userModel.setEmail(editEmail.getText().toString());
                userModel.setActive(true);

                UserRepository userRepository = new UserRepository(UserDetailActivity.this);
                rs = userRepository.insert(userModel);
                userRepository.closeRepo();

                setResult((rs ? RESULT_OK : RESULT_INSERT_ERROR));
                UserDetailActivity.this.finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult((RESULT_CANCELED));
                UserDetailActivity.this.finish();
            }
        });
    }

    private void switchMode(String mode) {
        switch (mode) {
            case UPDATE:
                labelPassword.setVisibility(View.GONE);
                editPassword.setVisibility(View.GONE);

                btnAdd.setVisibility(View.GONE);
                btnUpdate.setVisibility(View.VISIBLE);
                editUsername.setInputType(InputType.TYPE_NULL);
                break;
            case ADD:
                labelPassword.setVisibility(View.VISIBLE);
                editPassword.setVisibility(View.VISIBLE);

                btnAdd.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.GONE);
                editUsername.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
    }
}
