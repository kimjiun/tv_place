package com.mobitant.yesterdaytv;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mobitant.yesterdaytv.adapter.InfoListAdapter;
import com.mobitant.yesterdaytv.custom.EndlessRecyclerViewScrollListener;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.item.GeoItem;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TvInfoListFragment extends Fragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    int memberSeq;

    RecyclerView tvInfoList;
    TextView noDataText;

    TextView orderMeter;
    TextView orderFavorite;
    TextView orderRecent;

    ImageView listType;

    InfoListAdapter infoListAdapter;
    StaggeredGridLayoutManager layoutManager;
    EndlessRecyclerViewScrollListener scrollListener;

    int listTypeValue = 2;
    String orderType;


    public static TvInfoListFragment newInstance() {
        TvInfoListFragment f = new TvInfoListFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();

        memberSeq = ((MyApp)this.getActivity().getApplication()).getMemberSeq();

        View layout = inflater.inflate(R.layout.fragment_tvinfo_list, container, false);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        MyApp myApp = ((MyApp) getActivity().getApplication());
        TvInfoItem currentInfoItem = myApp.getTvInfoItem();

        if (infoListAdapter != null && currentInfoItem != null) {
            infoListAdapter.setItem(currentInfoItem);
            myApp.setTvInfoItem(null);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).changeTitle("가게리스트");

        orderType = Constant.ORDER_TYPE_METER;

        tvInfoList = (RecyclerView) view.findViewById(R.id.list);
        noDataText = (TextView) view.findViewById(R.id.no_data);
        listType = (ImageView) view.findViewById(R.id.list_type);

        orderMeter = (TextView) view.findViewById(R.id.order_meter);
        orderFavorite = (TextView) view.findViewById(R.id.order_favorite);
        orderRecent = (TextView) view.findViewById(R.id.order_recent);

        orderMeter.setOnClickListener(this);
        orderFavorite.setOnClickListener(this);
        orderRecent.setOnClickListener(this);
        listType.setOnClickListener(this);

        setRecyclerView();
        setTouch(false, true, true);
        listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, 0);
    }

    private void setLayoutManager(int row) {
        layoutManager = new StaggeredGridLayoutManager(row, StaggeredGridLayoutManager.VERTICAL);
        layoutManager
                .setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        tvInfoList.setLayoutManager(layoutManager);
    }

    private void setRecyclerView() {
        setLayoutManager(listTypeValue);

        infoListAdapter = new InfoListAdapter(context,
                R.layout.row_tvinfo_list, new ArrayList<TvInfoItem>());
        tvInfoList.setAdapter(infoListAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, page);
            }
        };
        tvInfoList.addOnScrollListener(scrollListener);
    }

    private void listInfo(int memberSeq, LatLng userLatLng, String orderType, final int currentPage) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<TvInfoItem>> call = remoteService.listTvInfo(memberSeq, userLatLng.latitude,
                                                userLatLng.longitude, orderType, currentPage);
        call.enqueue(new Callback<ArrayList<TvInfoItem>>() {
            @Override
            public void onResponse(Call<ArrayList<TvInfoItem>> call,
                                   Response<ArrayList<TvInfoItem>> response) {
                ArrayList<TvInfoItem> list = response.body();

                if (response.isSuccessful() && list != null) {
                    infoListAdapter.addItemList(list);
                    if (infoListAdapter.getItemCount() == 0) {
                        noDataText.setVisibility(View.VISIBLE);
                    } else {
                        noDataText.setVisibility(View.GONE);
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
        if (v.getId() == R.id.list_type) {
            changeListType();

        } else {
            if (v.getId() == R.id.order_meter) {
                orderType = Constant.ORDER_TYPE_METER;
                setOrderTextColor(R.color.text_color_green, R.color.text_color_black, R.color.text_color_black);
                setTouch(false, true, true);

            } else if (v.getId() == R.id.order_favorite) {
                orderType = Constant.ORDER_TYPE_FAVORITE;
                setOrderTextColor(R.color.text_color_black, R.color.text_color_green, R.color.text_color_black);
                setTouch(true, false, true);

            } else if (v.getId() == R.id.order_recent) {
                orderType = Constant.ORDER_TYPE_RECENT;
                setOrderTextColor(R.color.text_color_black, R.color.text_color_black, R.color.text_color_green);
                setTouch(true, true, false);
            }

            setRecyclerView();
            listInfo(memberSeq, GeoItem.getKnownLocation(), orderType, 0);
        }
    }

    private void setOrderTextColor(int color1, int color2, int color3) {
        orderMeter.setTextColor(ContextCompat.getColor(context, color1));
        orderFavorite.setTextColor(ContextCompat.getColor(context, color2));
        orderRecent.setTextColor(ContextCompat.getColor(context, color3));
    }

    private void setTouch(boolean nameT, boolean addT, boolean keyT){
        orderMeter.setEnabled(nameT);
        orderFavorite.setEnabled(addT);
        orderRecent.setEnabled(keyT);
    }

    private void changeListType() {
        if (listTypeValue == 1) {
            listTypeValue = 2;
            listType.setImageResource(R.drawable.ic_list2);
        } else {
            listTypeValue = 1;
            listType.setImageResource(R.drawable.ic_list);

        }
        setLayoutManager(listTypeValue);
    }
}
