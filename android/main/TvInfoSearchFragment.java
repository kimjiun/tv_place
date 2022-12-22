package com.mobitant.yesterdaytv;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mobitant.yesterdaytv.adapter.SearchListAdapter;
import com.mobitant.yesterdaytv.custom.EndlessRecyclerViewScrollListener;
import com.mobitant.yesterdaytv.item.GeoItem;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class TvInfoSearchFragment extends Fragment implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    Context context;

    int memberSeq;
    String searchContent ="";


    RecyclerView tvInfoSearchList;
    TextView noDataText;

    EditText searchEdit;
    TextView searchSubmit;

    TextView searchName;
    TextView searchLocation;
    TextView searchKeyword;

    SearchListAdapter searchListAdapter;
    StaggeredGridLayoutManager layoutManager;
    EndlessRecyclerViewScrollListener scrollListener;

    InputMethodManager imm;
    String searchType;

    public static TvInfoSearchFragment newInstance() {
        TvInfoSearchFragment f = new TvInfoSearchFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        searchType = Constant.SEARCH_TYPE_NAME;
        memberSeq = ((MyApp)this.getActivity().getApplication()).getMemberSeq();
        imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        View layout = inflater.inflate(R.layout.fragment_tvinfo_search, container, false);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        MyApp myApp = ((MyApp) getActivity().getApplication());
        TvInfoItem currentInfoItem = myApp.getTvInfoItem();

        if (searchListAdapter != null && currentInfoItem != null) {
            searchListAdapter.setItem(currentInfoItem);
            myApp.setTvInfoItem(null);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).changeTitle("검색");

        tvInfoSearchList = (RecyclerView) view.findViewById(R.id.search_result);
        noDataText = (TextView) view.findViewById(R.id.search_no_data);

        searchEdit = (EditText) view.findViewById(R.id.search_edit);
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchAction(v);
                }
                return false;
            }
        });

        searchSubmit = (TextView)view.findViewById(R.id.search_submit);

        searchName = (TextView) view.findViewById(R.id.search_name);
        searchLocation = (TextView) view.findViewById(R.id.search_location);
        searchKeyword = (TextView) view.findViewById(R.id.search_keyword);

        searchName.setOnClickListener(this);
        searchLocation.setOnClickListener(this);
        searchKeyword.setOnClickListener(this);
        searchSubmit.setOnClickListener(this);


        setTouch(false, true, true);
        setRecyclerView();
    }

    private void setRecyclerView() {
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        tvInfoSearchList.setLayoutManager(layoutManager);

        searchListAdapter = new SearchListAdapter(context,
                R.layout.row_tvinfo_search, new ArrayList<TvInfoItem>());
        tvInfoSearchList.setAdapter(searchListAdapter);

        searchListAdapter.setTextColor(searchContent, searchType);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                searchListInfo(memberSeq, GeoItem.getKnownLocation(), searchType, page);
            }
        };
        tvInfoSearchList.addOnScrollListener(scrollListener);
    }

    private void searchListInfo(int memberSeq, LatLng userLatLng, String searchType, final int currentPage) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<TvInfoItem>> call = remoteService.searchTvInfo(memberSeq, searchContent, searchType, currentPage,
                userLatLng.latitude, userLatLng.longitude);
        call.enqueue(new Callback<ArrayList<TvInfoItem>>() {
            @Override
            public void onResponse(Call<ArrayList<TvInfoItem>> call,
                                   Response<ArrayList<TvInfoItem>> response) {
                ArrayList<TvInfoItem> list = response.body();

                if (response.isSuccessful() && list != null) {
                    searchListAdapter.addItemList(list);

                    if (searchListAdapter.getItemCount() == 0) {
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
            if (v.getId() == R.id.search_name) {
                searchType = Constant.SEARCH_TYPE_NAME;
                setOrderTextColor(R.color.text_color_green,
                        R.color.text_color_black, R.color.text_color_black);
                setTouch(false, true, true);
                searchAction(v);
            } else if (v.getId() == R.id.search_location) {
                searchType = Constant.SEARCH_TYPE_LOCATION;
                setOrderTextColor(R.color.text_color_black,
                        R.color.text_color_green, R.color.text_color_black);
                setTouch(true, false, true);
                searchAction(v);
            } else if (v.getId() == R.id.search_keyword) {
                searchType = Constant.SEARCH_TYPE_KEYWORD;
                setOrderTextColor(R.color.text_color_black,
                        R.color.text_color_black, R.color.text_color_green);
                setTouch(true, true, false);
                searchAction(v);
            } else if(v.getId() == R.id.search_submit) {
                searchAction(v);
            }
    }
    private  void searchAction(View v){
        searchContent = searchEdit.getText().toString();
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if(!searchContent.isEmpty()) {
            setRecyclerView();
            searchListInfo(memberSeq, GeoItem.getKnownLocation(), searchType, 0);
        }
    }

    private void setTouch(boolean nameT, boolean addT, boolean keyT){
        searchName.setEnabled(nameT);
        searchLocation.setEnabled(addT);
        searchKeyword.setEnabled(keyT);
    }

    private void setOrderTextColor(int color1, int color2, int color3) {
        searchName.setTextColor(ContextCompat.getColor(context, color1));
        searchLocation.setTextColor(ContextCompat.getColor(context, color2));
        searchKeyword.setTextColor(ContextCompat.getColor(context, color3));
    }
}
