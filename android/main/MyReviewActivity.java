package com.mobitant.yesterdaytv;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.mobitant.yesterdaytv.adapter.MyReviewAdapter;
import com.mobitant.yesterdaytv.custom.EndlessRecyclerViewScrollListener;
import com.mobitant.yesterdaytv.item.MyReviewItem;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReviewActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    int memberSeq;

    TextView toolbarTitle;
    RecyclerView reviewList;
    TextView noReviewText;

    MyReviewAdapter myReviewAdapter;
    StaggeredGridLayoutManager layoutManager;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_review);

        context = this;

        memberSeq = ((MyApp)getApplication()).getMemberSeq();

        setToolbar();
        setView();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        final ActionBar actionBar = getSupportActionBar();


        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            toolbarTitle.setText("나의 리뷰");
        }
    }

    private void setView() {
        noReviewText = findViewById(R.id.my_no_review);
        reviewList = (RecyclerView) findViewById(R.id.my_review);

        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        reviewList.setLayoutManager(layoutManager);

        setRecyclerView();
        listReview(memberSeq, 0);
    }

    private void setRecyclerView() {
        myReviewAdapter = new MyReviewAdapter(context,
                R.layout.row_review_my, new ArrayList<MyReviewItem>());
        reviewList.setAdapter(myReviewAdapter);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                listReview(memberSeq, page);
            }
        };
        reviewList.addOnScrollListener(scrollListener);
    }

    private void listReview(int memberSeq, final int currentPage) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<MyReviewItem>> call = remoteService.myReview(memberSeq, currentPage);
        call.enqueue(new Callback<ArrayList<MyReviewItem>>() {
            @Override
            public void onResponse(Call<ArrayList<MyReviewItem>> call,
                                   Response<ArrayList<MyReviewItem>> response) {
                ArrayList<MyReviewItem> list = response.body();

                if (response.isSuccessful() && list != null) {
                    myReviewAdapter.addItemList(list);

                    if (myReviewAdapter.getItemCount() == 0) {
                        noReviewText.setVisibility(View.VISIBLE);
                    } else {
                        noReviewText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MyReviewItem>> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

}
