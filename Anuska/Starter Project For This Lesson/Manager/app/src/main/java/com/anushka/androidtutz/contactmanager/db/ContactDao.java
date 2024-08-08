package com.anushka.androidtutz.contactmanager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.anushka.androidtutz.contactmanager.db.entity.Contact;

import java.util.List;

@Dao
public interface ContactDao {

    @Insert
    public long addContact(Contact contact);

    @Update
    public void updateContact(Contact contact);

    @Delete
    public void deleContact(Contact contact);

    @Query("select * from contacts")
    public List<Contact> getContacts();

    @Query("select * from contacts where contact_id ==:contactId")
    public  Contact getAllContacts(long contactId);
}
