package io.nambm.sqlitetesting.model;

import java.io.Serializable;

import io.nambm.sqlitetesting.repository.SqliteGenericObject;
import io.nambm.sqlitetesting.repository.annotation.SqliteNotNull;
import io.nambm.sqlitetesting.repository.annotation.SqlitePrimaryKey;
import io.nambm.sqlitetesting.repository.annotation.SqliteTableName;

@SqliteTableName("user")
public class UserModel implements SqliteGenericObject, Serializable {

    @SqlitePrimaryKey
    @SqliteNotNull
    private String username;

    @SqliteNotNull
    private String fullName;

    @SqliteNotNull
    private String password;

    @SqliteNotNull
    private String email;

    @SqliteNotNull
    private boolean isActive;

    public UserModel() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String getId() {
        return username;
    }
}
