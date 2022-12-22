package com.mobitant.yesterdaytv.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mobitant.yesterdaytv.Constant;
import com.mobitant.yesterdaytv.R;
import com.mobitant.yesterdaytv.item.MemberInfoItem;
import com.mobitant.yesterdaytv.item.ReviewItem;
import com.mobitant.yesterdaytv.lib.DialogLib;
import com.mobitant.yesterdaytv.lib.GoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.StringLib;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ItemReviewAdapter extends RecyclerView.Adapter<ItemReviewAdapter.ViewHolder> {
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int resource;
    private String reviewDate;
    private ArrayList<ReviewItem> reviewList;

    public ItemReviewAdapter(Context context, int resource, ArrayList<ReviewItem> reviewList) {
        this.context = context;
        this.resource = resource;
        this.reviewList = reviewList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemReviewAdapter.ViewHolder holder, int position) {
        final ReviewItem item = reviewList.get(position);
        MyLog.d(TAG, "getView " + item);

        reviewDate  = item.reviewDate.substring(0, item.reviewDate.indexOf('T'));

        holder.name.setText(item.name);
        holder.review.setText(StringLib.getInstance().getSubString(context,
                item.review, Constant.MAX_LENGTH_REVIEW));
        holder.regDate.setText(StringLib.getInstance().getSubString(context,
                reviewDate, Constant.MAX_LENGTH_DESCRIPTION));
        setImage(holder.profileIcon, item.memberIconFilename);

        holder.rating.setRating(item.rate);
    }


    public void setItem(ReviewItem newItem) {
        for (int i=0; i < reviewList.size(); i++) {
            ReviewItem item = reviewList.get(i);

            if (item.reviewSeq == newItem.reviewSeq) {
                reviewList.set(i, newItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addItemList(ArrayList<ReviewItem> itemList) {
        this.reviewList.addAll(itemList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.reviewList.size();
    }

    private void setImage(ImageView imageView, String fileName) {
        if (StringLib.getInstance().isBlank(fileName)) {
            Picasso.with(context).load(R.drawable.ic_person).into(imageView);
        } else {
            Picasso.with(context).load(RemoteService.MEMBER_ICON_URL + fileName).into(imageView);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIcon;
        TextView name;
        TextView review;
        TextView regDate;
        RatingBar rating;

        public ViewHolder(View itemView) {
            super(itemView);
            profileIcon = (ImageView) itemView.findViewById(R.id.review_profile_icon);
            name = (TextView) itemView.findViewById(R.id.review_name);
            review = (TextView) itemView.findViewById(R.id.review_content);
            regDate = (TextView) itemView.findViewById(R.id.review_regDate);
            rating = (RatingBar)itemView.findViewById(R.id.review_rating);
        }
    }
}
