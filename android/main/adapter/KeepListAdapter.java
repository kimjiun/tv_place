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
import com.mobitant.yesterdaytv.R;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.item.KeepItem;
import com.mobitant.yesterdaytv.lib.DialogLib;
import com.mobitant.yesterdaytv.lib.GoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.StringLib;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * 맛집 정보 즐겨찾기 리스트의 아이템을 처리하는 어댑터
 */
public class KeepListAdapter extends RecyclerView.Adapter<KeepListAdapter.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;
    private ArrayList<KeepItem> itemList;
    private int memberSeq;


    public KeepListAdapter(Context context, int resource, ArrayList<KeepItem> itemList, int memberSeq) {
        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
        this.memberSeq = memberSeq;
    }

    public void setItemList(ArrayList<KeepItem> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public void setItem(TvInfoItem newItem) {
        for (int i=0; i < itemList.size(); i++) {
            KeepItem item = itemList.get(i);

            if (item.seq == newItem.seq && !newItem.isKeep) {
                itemList.remove(i);
                notifyItemChanged(i);
                break;
            }
        }
    }

    private void removeItem(int seq) {
        for (int i=0; i < itemList.size(); i++) {
            if (itemList.get(i).seq == seq) {
                itemList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, getItemCount());
                break;
            }
        }
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        final KeepItem item = itemList.get(position);
        MyLog.d(TAG, "getView " + item);

        if (item.isKeep) {
            holder.keep.setImageResource(R.drawable.ic_keep_on);
        } else {
            holder.keep.setImageResource(R.drawable.ic_keep_off);
        }

        holder.name.setText(item.name);
        holder.keyword.setText(
                StringLib.getInstance().getSubString(context,
                                        item.keyword, Constant.MAX_LENGTH_DESCRIPTION));
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
                DialogLib.getInstance().showKeepDeleteDialog(context, keepHandler, memberSeq, item.seq);
            }
        });
    }

    private void setImage(ImageView imageView, String fileName) {
        MyLog.d(TAG, "setImage fileName " + fileName);

        if (StringLib.getInstance().isBlank(fileName)) {
            Picasso.with(context).load(R.drawable.bg_bestfood_drawer).into(imageView);
        } else {
            Picasso.with(context).load(RemoteService.IMAGE_URL + fileName).into(imageView);
        }
    }

    Handler keepHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            removeItem(msg.what);
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
            address = (TextView) itemView.findViewById(R.id.address);
            image = (ImageView) itemView.findViewById(R.id.image);
            keep = (ImageView) itemView.findViewById(R.id.keep);
            name = (TextView) itemView.findViewById(R.id.name);
            keyword = (TextView) itemView.findViewById(R.id.description);
        }
    }
}
