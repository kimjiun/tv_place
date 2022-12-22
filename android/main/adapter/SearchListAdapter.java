package com.mobitant.yesterdaytv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobitant.yesterdaytv.Constant;
import com.mobitant.yesterdaytv.MyApp;
import com.mobitant.yesterdaytv.R;
import com.mobitant.yesterdaytv.item.MemberInfoItem;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.lib.DialogLib;
import com.mobitant.yesterdaytv.lib.GoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.StringLib;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchListAdapter  extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;
    private ArrayList<TvInfoItem> itemList;
    private MemberInfoItem memberInfoItem;

    private String searchType;
    private String searchWord;


    public SearchListAdapter(Context context, int resource, ArrayList<TvInfoItem> itemList) {
        this.context = context;
        this.resource = resource;
        this.itemList = itemList;

        memberInfoItem = ((MyApp) context.getApplicationContext()).getMemberInfoItem();
    }

    public void setItem(TvInfoItem newItem) {
        for (int i=0; i < itemList.size(); i++) {
            TvInfoItem item = itemList.get(i);

            if (item.seq == newItem.seq) {
                itemList.set(i, newItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addItemList(ArrayList<TvInfoItem> itemList) {
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
    }

    private void changeItemKeep(int seq, boolean keep) {
        for (int i=0; i < itemList.size(); i++) {
            if (itemList.get(i).seq == seq) {
                itemList.get(i).isKeep = keep;
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void setTextColor(String searchWord, String searchType){
        MyLog.d(TAG, "Func searchWord : " + searchWord);
        MyLog.d(TAG, "Func searchType : " + searchType);
        this.searchType = searchType;
        this.searchWord = searchWord;
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchListAdapter.ViewHolder holder, int position) {
        final TvInfoItem item = itemList.get(position);
        MyLog.d(TAG, "getView " + item);

        String searchContent = "";

        if (searchType == "name")
            searchContent = item.name;
        else if(searchType == "keyword")
            searchContent = item.keyword;
        else
            searchContent = item.address;

        SpannableString ssb = new SpannableString(searchContent);
        MyLog.d(TAG, "ssb : " + ssb);

        int start = searchContent.indexOf(searchWord);
        int end = start + searchWord.length();
        MyLog.d(TAG, "searchWord : " + searchWord);
        MyLog.d(TAG, "searchType : " + searchType);

        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (item.isKeep)
            holder.keep.setImageResource(R.drawable.ic_keep_on);
        else
            holder.keep.setImageResource(R.drawable.ic_keep_off);

        if (searchType == "name")
            holder.name.setText(ssb);
        else
             holder.name.setText(item.name);

        if (searchType == "keyword")
            holder.keyword.setText(ssb);
        else
            holder.keyword.setText(StringLib.getInstance().getSubString(context,
                    item.keyword, Constant.MAX_LENGTH_DESCRIPTION));

        if(searchType == "address")
            holder.address.setText(ssb);
        else
            holder.address.setText(StringLib.getInstance().getSubString(context,
                   item.address, Constant.MAX_LENGTH_DESCRIPTION));

        setImage(holder.image, item.imageFilename);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoLib.getInstance().goTvInfoActivity(context, item.seq);
            }
        });

        holder.keep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.isKeep) {
                    DialogLib.getInstance().showKeepDeleteDialog(context,
                            keepDeleteHandler, memberInfoItem.seq, item.seq);
                } else {
                    DialogLib.getInstance().showKeepInsertDialog(context,
                            keepInsertHandler, memberInfoItem.seq, item.seq);
                }
            }
        });
    }

    private void setImage(ImageView imageView, String fileName) {
        if (StringLib.getInstance().isBlank(fileName)) {
            Picasso.with(context).load(R.drawable.bg_bestfood_drawer).into(imageView);
        } else {
            Picasso.with(context).load(RemoteService.IMAGE_URL + fileName).into(imageView);
        }
    }

    Handler keepInsertHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            changeItemKeep(msg.what, true);
        }
    };

    Handler keepDeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            changeItemKeep(msg.what, false);
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView keep;
        TextView name;
        TextView keyword;
        TextView address;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            keep = (ImageView) itemView.findViewById(R.id.keep);
            name = (TextView) itemView.findViewById(R.id.name);
            keyword = (TextView) itemView.findViewById(R.id.keyword);
            address = (TextView) itemView.findViewById(R.id.address);
        }
    }


}
