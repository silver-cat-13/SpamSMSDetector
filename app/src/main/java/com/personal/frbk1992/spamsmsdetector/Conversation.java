package com.personal.frbk1992.spamsmsdetector;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Conversation class used to store the conversationList messages between two users
 *
 * This file is a template to be used for other classes
 *
 * TODO add a parameter to differentiate the sender over the receiver here or in SMSClass
 * a second ArrayList could be imeplemented for sender and receiver or a parameter
 * in SMSClass could be use, the second option looks the ideal option
 * TODO create toString, equals hash functions
 */
public class Conversation implements Parcelable {

    private final ArrayList<SMSClass> smsList; // store the conversationList of both sender and receiver
    private final int threadID;                     // id of the conversaation
    private String date;                            // date of the newest SMS in the conversationList list
                                                    // this value is used to order the conversations
    private static final String EMPTY_DATE = "";

    public Conversation(ArrayList<SMSClass> smsList,
                        String date,
                        int threadID) {
        this.smsList = smsList;
        this.date = date;
        this.threadID = threadID;
    }

    private Conversation(Parcel in) {
        smsList = in.createTypedArrayList(SMSClass.CREATOR);
        date = in.readString();
        threadID = in.readInt();
    }

    /**
     * Create a conversationList with an empty array list of SMS
     * @param threadID the initial thread
     * @return the conversationList with empty array
     */
    private static Conversation createEmptyConversation(int threadID){
        return new Conversation(new ArrayList<SMSClass>(), EMPTY_DATE, threadID);
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
        dest.writeTypedList(smsList);
        dest.writeString(date);
        dest.writeInt(threadID);
    }


    public ArrayList<SMSClass> getSmsList() {
        return smsList;
    }

    public int getThreadId() {
        return threadID;
    }

    public String getDate() {
        return date;
    }

    /**
     * Given an sparse array of conversations and a threadID the function will return the conversationList
     * related with the threadID. If it does not exist it will create a new convresation object using
     * the threadID
     * TODO study the performance impact of sparse array over a hashmap
     * @param conversations the SparseArray instance with all the current conversations
     * @param threadID the threadID we care about
     * @return the instance of the Conversation in the SparseArray
     */
    private static Conversation getConversationByThreadID(SparseArray<Conversation> conversations,
                                                         int threadID){
        return conversations.get(threadID, createEmptyConversation(threadID));
    }

    /**
     * Adds an SMSClass into an SparseArray Conversations instance using the ThreadID. If the ThreadID
     * Does not exist in the SparseArray it creates a new conversation instance
     * @param conversations the conversations SparseArray
     * @param sms the SMSClass to be added in one of the conversation instance in the SparseArray
     * @param threadID the ThreadID of the conversation
     */
    public static void addSMSToConversation(SparseArray<Conversation> conversations,
                                            SMSClass sms,
                                            int threadID){
        Conversation c = getConversationByThreadID(conversations, threadID);
        c.getSmsList().add(sms);
    }
}
