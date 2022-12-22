package com.mobitant.yesterdaytv.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
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

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder>  {
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;
    private ArrayList<TvInfoItem> itemList;
    private MemberInfoItem memberInfoItem;

    public RecommendAdapter(Context context, int resource, ArrayList<TvInfoItem> itemList) {
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

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    @Override
    public RecommendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

        return new RecommendAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecommendAdapter.ViewHolder holder, int position) {
        final TvInfoItem item = itemList.get(position);
        MyLog.d(TAG, "getView " + item);

        if (item.isKeep) {
            holder.keep.setImageResource(R.drawable.ic_keep_on);
        } else {
            holder.keep.setImageResource(R.drawable.ic_keep_off);
        }

        holder.name.setText(item.name);
        holder.keyword.setText(StringLib.getInstance().getSubString(context,
                item.keyword, Constant.MAX_LENGTH_DESCRIPTION));
        holder.address.setText(StringLib.getInstance().getSubString(context,
                item.address, Constant.MAX_LENGTH_DESCRIPTION));

        int meter = (int) item.userDistanceMeter;
        if (meter == 0)
            holder.distance.setText("");
        else if (meter < 1000)
            holder.distance.setText(meter + context.getResources().getString(R.string.unit_m));
        else
            holder.distance.setText( (meter / 1000) + context.getResources().getString(R.string.unit_km));

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
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            keep = (ImageView) itemView.findViewById(R.id.keep);
            name = (TextView) itemView.findViewById(R.id.name);
            keyword = (TextView) itemView.findViewById(R.id.keyword);
            distance = itemView.findViewById(R.id.distance);
            address = (TextView) itemView.findViewById(R.id.address);
        }
    }

}
