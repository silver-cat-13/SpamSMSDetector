package com.personal.frbk1992.spamsmsdetector;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Conversation class used to store the conversation messages between two users
 *
 * This file is a template to be used for other classes
 *
 * TODO add a parameter to differentiate the sender over the receiver here or in SMSClass
 * a second ArrayList could be imeplemented for sender and receiver or a parameter
 * in SMSClass could be use, the second option looks the ideal option
 */
public class Conversation implements Parcelable {

    private final ArrayList<SMSClass> conversation; // store the conversation of both sender and receiver

    public Conversation(ArrayList<SMSClass> conversation) {
        this.conversation = conversation;
    }

    private Conversation(Parcel in) {
        conversation = in.createTypedArrayList(SMSClass.CREATOR);
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(conversation);
    }


    public ArrayList<SMSClass> getConversation() {
        return conversation;
    }
}
