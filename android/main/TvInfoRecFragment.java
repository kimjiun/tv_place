package com.mobitant.yesterdaytv;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mobitant.yesterdaytv.adapter.InfoListAdapter;
import com.mobitant.yesterdaytv.adapter.RecommendAdapter;
import com.mobitant.yesterdaytv.custom.EndlessRecyclerViewScrollListener;
import com.mobitant.yesterdaytv.item.GeoItem;
import com.mobitant.yesterdaytv.item.KeepItem;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TvInfoRecFragment extends Fragment  implements View.OnClickListener  {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    int memberSeq;

    RecyclerView recommendList;
    TextView loadingText;

    TextView itemBased;
    TextView userBased;

    RecommendAdapter recommendAdapter;
    StaggeredGridLayoutManager layoutManager;
    EndlessRecyclerViewScrollListener scrollListener;

    String cftype = "item_based";

    public static TvInfoRecFragment newInstance() {
        TvInfoRecFragment f = new TvInfoRecFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();

        memberSeq = ((MyApp)this.getActivity().getApplication()).getMemberSeq();

        View layout = inflater.inflate(R.layout.fragment_tvinfo_recomend, container, false);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        MyApp myApp = ((MyApp) getActivity().getApplication());
        TvInfoItem currentInfoItem = myApp.getTvInfoItem();

        if (recommendAdapter != null && currentInfoItem != null) {
            recommendAdapter.setItem(currentInfoItem);
            myApp.setTvInfoItem(null);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).changeTitle("추천시스템");

        recommendList = (RecyclerView) view.findViewById(R.id.rec_list);
        loadingText = view.findViewById(R.id.loading_text);

        itemBased = (TextView) view.findViewById(R.id.item_based);
        userBased = (TextView) view.findViewById(R.id.user_based);

        itemBased.setOnClickListener(this);
        userBased.setOnClickListener(this);

        setRecyclerView();
        listRecommend(memberSeq, GeoItem.getKnownLocation(), 0, cftype);
    }

    private void setLayoutManager() {
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recommendList.setLayoutManager(layoutManager);
    }

    private void setRecyclerView() {
        setLayoutManager();

        recommendAdapter = new RecommendAdapter(context,
                R.layout.row_recommend_list, new ArrayList<TvInfoItem>());
        recommendList.setAdapter(recommendAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                //listRecommend(memberSeq, GeoItem.getKnownLocation(), page, cftype);
            }
        };
        recommendList.addOnScrollListener(scrollListener);
    }

    private void listRecommend(int memberSeq, LatLng userLatLng, final int currentPage, String cftype) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<TvInfoItem>> call;
            call = remoteService.recommendList(userLatLng.latitude, userLatLng.longitude, memberSeq, currentPage, cftype);

        call.enqueue(new Callback<ArrayList<TvInfoItem>>() {
            @Override
            public void onResponse(Call<ArrayList<TvInfoItem>> call,
                                   Response<ArrayList<TvInfoItem>> response) {
                ArrayList<TvInfoItem> list = response.body();

                if (response.isSuccessful() && list != null) {
                    recommendAdapter.addItemList(list);
                    if (recommendAdapter.getItemCount() == 0) {
                        loadingText.setVisibility(View.VISIBLE);
                    } else {
                        loadingText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<TvInfoItem>> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.item_based) {
            cftype = "item_based";
            setOrderTextColor(R.color.text_color_green, R.color.text_color_black);
            setTouch(false, true);
        }
        else if(v.getId() == R.id.user_based){
            cftype = "user_based";
            setOrderTextColor(R.color.text_color_black, R.color.text_color_green);
            setTouch(true, false);
        }
        loadingText.setVisibility(View.VISIBLE);
        setRecyclerView();
        listRecommend(memberSeq, GeoItem.getKnownLocation(), 0, cftype);
    }

    private void setOrderTextColor(int color1, int color2) {
        itemBased.setTextColor(ContextCompat.getColor(context, color1));
        userBased.setTextColor(ContextCompat.getColor(context, color2));
    }

    private void setTouch(boolean itemT, boolean userT){
        itemBased.setEnabled(itemT);
        userBased.setEnabled(userT);
    }
}
