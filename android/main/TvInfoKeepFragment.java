package com.mobitant.yesterdaytv;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mobitant.yesterdaytv.adapter.KeepListAdapter;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.item.GeoItem;
import com.mobitant.yesterdaytv.item.KeepItem;
import com.mobitant.yesterdaytv.lib.GoLib;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TvInfoKeepFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    Context context;
    int memberSeq;

    RecyclerView keepRecyclerView;
    TextView noDataText;

    KeepListAdapter keepListAdapter;

    ArrayList<KeepItem> keepList = new ArrayList<>();


    public static TvInfoKeepFragment newInstance() {
        TvInfoKeepFragment f = new TvInfoKeepFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        setHasOptionsMenu(true);
        memberSeq = ((MyApp)getActivity().getApplication()).getMemberSeq();

        View layout = inflater.inflate(R.layout.fragment_tvinfo_keep, container, false);

        return layout;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_close, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_close:
                GoLib.getInstance().goBackFragment(getFragmentManager());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        MyApp myApp = ((MyApp) getActivity().getApplication());
        TvInfoItem currentInfoItem = myApp.getTvInfoItem();

        if (keepListAdapter != null && currentInfoItem != null) {
            keepListAdapter.setItem(currentInfoItem);
            myApp.setTvInfoItem(null);

            if (keepListAdapter.getItemCount() == 0) {
                noDataText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).changeTitle("즐겨찾기");

        keepRecyclerView = (RecyclerView) view.findViewById(R.id.keep_list);
        noDataText = (TextView) view.findViewById(R.id.no_keep);

        keepListAdapter = new KeepListAdapter(context,
                R.layout.row_tvinfo_keep, keepList, memberSeq);
        StaggeredGridLayoutManager layoutManager
                = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        keepRecyclerView.setLayoutManager(layoutManager);
        keepRecyclerView.setAdapter(keepListAdapter);

        listKeep(memberSeq, GeoItem.getKnownLocation());
    }

    private void listKeep(int memberSeq, LatLng userLatLng) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<KeepItem>> call
                = remoteService.listKeep(memberSeq, userLatLng.latitude, userLatLng.longitude);
        call.enqueue(new Callback<ArrayList<KeepItem>>() {
            @Override
            public void onResponse(Call<ArrayList<KeepItem>> call,
                                   Response<ArrayList<KeepItem>> response) {
                ArrayList<KeepItem> list = response.body();

                if (list == null) {
                    list = new ArrayList<>();
                }

                noDataText.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    MyLog.d(TAG, "list size " + list.size());
                    if (list.size() == 0) {
                        noDataText.setVisibility(View.VISIBLE);
                    } else {
                        keepListAdapter.setItemList(list);
                    }
                } else {
                    MyLog.d(TAG, "not success");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<KeepItem>> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }
}