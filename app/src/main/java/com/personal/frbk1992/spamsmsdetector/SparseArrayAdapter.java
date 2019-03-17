package com.personal.frbk1992.spamsmsdetector;


import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 *
 * @param <E>
 *
 * TODO update the class using this URL as reference
 * https://gist.github.com/luckycreationsindia/61055849d67f6a572987
 */
public class SparseArrayAdapter<E> extends BaseAdapter {
    private final String TAG = this.getClass().getSimpleName();

    private SparseArray<E> mData; // list of objects that represent the data in the array adapter
    private Context mContext;

    private final LayoutInflater mInflater;

    /*
     The resource indicating what views to inflate to display the content of this
     array adapter.
     */
    private int mResource;


    /*
     This field must contain the identifier that matches the one defined in the resource file.
     */
    private int mFieldId = 0;



    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public SparseArrayAdapter(Context context, @LayoutRes int resource, @IdRes int textViewResourceId
            , @NonNull SparseArray<E> objects) {
        this.mContext = context;
        this.mData = objects;
        this.mInflater = LayoutInflater.from(context);
        this.mResource = resource;
        this.mFieldId = textViewResourceId;

    }

    public void setData(SparseArray<E> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Conversation getItem(int position) {
        return (Conversation) mData.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.keyAt(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        TextView titleView;
        TextView contentView;
        SMSClass smsClassContent;
        if(convertView == null) {
            view = mInflater.inflate(mResource, parent, false);

            //get all the views ids and save them in the ViewHolder class
            ViewHolder holder = new ViewHolder();
            titleView = holder.mTitleView = view.findViewById(R.id.text_view_list_title);
            contentView = holder.mContentView = view.findViewById(R.id.text_view_list_detail);
            smsClassContent = holder.smsContent = ((Conversation) mData.valueAt(position)).getSmsList().get(0); // TODO change, this is test
            view.setTag(holder);

        }else{
            view = convertView;
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            titleView = viewHolder.mTitleView;
            contentView = viewHolder.mContentView;
            smsClassContent = viewHolder.smsContent;
        }

        titleView.setText(smsClassContent.getTitle());
        contentView.setText(smsClassContent.getContent());

        Log.v(TAG, "Conversation "+position+" "+((Conversation) mData.valueAt(position)).getThreadId());

        return view;
    }

    public void updateConversations(SparseArray<E> conversations) {
        ThreadPreconditions.checkOnMainThread();
        this.mData = conversations;
        notifyDataSetChanged();
    }

    /*
   Holder class
    */
    private static class ViewHolder {
        TextView mTitleView = null;
        TextView mContentView = null;
        SMSClass smsContent = null; // last SMS in the conversation list
    }
}
