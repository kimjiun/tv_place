package com.mobitant.yesterdaytv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mobitant.yesterdaytv.lib.GoLib;

public class TvInfoMenuFragment extends Fragment {
    Context context;
    ListView listview;
    static final String[] list_menu = {"프로필 설정", "즐겨찾기", "내 리뷰"};

    public static TvInfoMenuFragment newInstance() {
        TvInfoMenuFragment f = new TvInfoMenuFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        View layout = inflater.inflate(R.layout.fragment_tvinfo_menu, container, false);

        listview = (ListView)layout.findViewById(R.id.listView);
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(
                this.getActivity(),
                android.R.layout.simple_list_item_1,
                list_menu
        );
        listview.setAdapter(listViewAdapter);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).changeTitle("기타설정");

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        GoLib.getInstance().goProfileActivity(getActivity());
                        break;
                    case 1:
                       GoLib.getInstance().goFragmentBack(getFragmentManager(), R.id.content_main, TvInfoKeepFragment.newInstance());
                        break;
                    case 2:
                        GoLib.getInstance().goMyReviewActivity(getActivity());
                        break;
                }
            }
        });
    }
}
