package io.nambm.sqlitetesting.storage;

import android.content.Context;

import io.nambm.sqlitetesting.model.UserModel;
import io.nambm.sqlitetesting.repository.impl.Repository;

public class UserRepository extends Repository<UserModel> {

    public UserRepository(Context context) {
        super(context);
    }

    public UserModel getByUsername(String username) {
        UserModel wrapper = new UserModel();
        wrapper.setUsername(username);

        return queryById(wrapper);
    }
}
