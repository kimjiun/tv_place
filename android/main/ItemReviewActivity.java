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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import com.mobitant.yesterdaytv.adapter.ItemReviewAdapter;
import com.mobitant.yesterdaytv.custom.EndlessRecyclerViewScrollListener;
import com.mobitant.yesterdaytv.item.ReviewItem;
import com.mobitant.yesterdaytv.lib.MyLog;
import com.mobitant.yesterdaytv.lib.MyToast;
import com.mobitant.yesterdaytv.remote.RemoteService;
import com.mobitant.yesterdaytv.remote.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemReviewActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    public static final String INFO_SEQ = "INFO_SEQ";
    public static final String ITEM_NAME = "ITEM_NAME";
    Context context;

    MyApp myApp;
    int memberSeq;
    int TvInfoSeq;
    String ItemName;

    EditText editReview;
    RatingBar reviewRating;
    Button submitReview;

    TextView toolbarTitle;
    RecyclerView reviewList;
    TextView noReviewText;

    ItemReviewAdapter itemReviewAdapter;
    EndlessRecyclerViewScrollListener scrollListener;
    StaggeredGridLayoutManager layoutManager;
    InputMethodManager imm;

    int ratingScore=5;
    String reviewContent="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_review);

        context = this;

        memberSeq = ((MyApp)getApplication()).getMemberSeq();
        TvInfoSeq = getIntent().getIntExtra(INFO_SEQ, 0);
        ItemName = getIntent().getStringExtra(ITEM_NAME);
        setToolbar();
        setView();
    }

    @Override
    protected void onResume() {
        super.onResume();
       // ReviewItem currentInfoItem = myApp.getReviewItem();

       // if (itemReviewAdapter != null && currentInfoItem != null) {
      //      itemReviewAdapter.setItem(currentInfoItem);
      //      myApp.setTvInfoItem(null);
      //  }
    }



    private void setToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        final ActionBar actionBar = getSupportActionBar();


        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            toolbarTitle.setText(ItemName+" 리뷰");
        }
    }

    private void setView() {
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        editReview = findViewById(R.id.edit_review);
        reviewRating = findViewById(R.id.item_rating);
        submitReview = findViewById(R.id.btn_review_submit);
        noReviewText = findViewById(R.id.no_review);
        reviewList = (RecyclerView) findViewById(R.id.item_review);

        reviewRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(rating < 1) {
                    rating=1;
                    ratingBar.setRating(rating);
                }
                ratingScore = (int)rating;
            }
        });

        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                reviewContent = editReview.getText().toString();
                if(reviewContent.equals(""))
                    MyToast.s(context, "리뷰를 작성해 주세요!!");
                else {
                    insertReview();
                    editReview.setText("");
                    setRecyclerView();
                    listReview(TvInfoSeq, 0);
                }
            }
        });

        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        reviewList.setLayoutManager(layoutManager);
        setRecyclerView();
        listReview(TvInfoSeq, 0);
    }

    private void setRecyclerView() {
        itemReviewAdapter = new ItemReviewAdapter(context,
                R.layout.row_review_item, new ArrayList<ReviewItem>());
        reviewList.setAdapter(itemReviewAdapter);
        MyLog.d(TAG, "netRecyclerView 호출 성공!");
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                //통신해서 받아오는 코드 + 어댑터 수정하면 끝
                listReview(TvInfoSeq, page);
            }
        };
        reviewList.addOnScrollListener(scrollListener);
    }

    private void listReview(int itemSeq, final int currentPage) {
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<ArrayList<ReviewItem>> call = remoteService.listReview(itemSeq, currentPage);
        call.enqueue(new Callback<ArrayList<ReviewItem>>() {
            @Override
            public void onResponse(Call<ArrayList<ReviewItem>> call,
                                   Response<ArrayList<ReviewItem>> response) {
                ArrayList<ReviewItem> list = response.body();

                if (response.isSuccessful() && list != null) {
                    itemReviewAdapter.addItemList(list);

                    if (itemReviewAdapter.getItemCount() == 0) {
                        noReviewText.setVisibility(View.VISIBLE);
                    } else {
                        noReviewText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ReviewItem>> call, Throwable t) {
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

    private ReviewItem getReviewItem() {
        ReviewItem item = new ReviewItem();
        item.rate = ratingScore;
        item.review = reviewContent;
        item.reviewMemberSeq = memberSeq;
        item.reviewItemSeq = TvInfoSeq;
        return item;
    }


    private void insertReview(){
        final ReviewItem newItem = getReviewItem();

        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertReview(newItem);
        MyLog.d(TAG, "insert review success?");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    MyLog.d(TAG, "insertReview " + response);
                    onResume();
                    }
                else
                    MyLog.d(TAG, "response error " + response.errorBody());
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
            }
        });
    }
}
