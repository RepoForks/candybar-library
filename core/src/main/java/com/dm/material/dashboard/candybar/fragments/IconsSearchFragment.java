package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.adapters.IconsAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.SoftKeyboardHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.AlphanumComparator;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class IconsSearchFragment extends Fragment {

    private RecyclerView mIconsGrid;
    private RecyclerFastScroller mFastScroll;
    private TextView mSearchResult;
    private SearchView mSearchView;

    private IconsAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mGetIcons;

    public static final String TAG = "icons_search";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_search, container, false);
        mIconsGrid = (RecyclerView) view.findViewById(R.id.icons_grid);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mSearchResult = (TextView) view.findViewById(R.id.search_result);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        ViewCompat.setNestedScrollingEnabled(mIconsGrid, false);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mIconsGrid,
                getActivity().getResources().getConfiguration().orientation);

        mIconsGrid.setHasFixedSize(true);
        mIconsGrid.setItemAnimator(new DefaultItemAnimator());
        mIconsGrid.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.icons_column_count)));
        mFastScroll.attachRecyclerView(mIconsGrid);

        getIcons();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_icons_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(search);
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setQueryHint(getActivity().getResources().getString(R.string.search_icon));
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        MenuItemCompat.expandActionView(search);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.clearFocus();

        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        ViewHelper.changeSearchViewTextColor(mSearchView,
                color, ColorHelper.setColorAlpha(color, 0.6f));
        View view = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (view != null) view.setBackgroundColor(Color.TRANSPARENT);

        ImageView closeIcon = (ImageView) mSearchView.findViewById(
                android.support.v7.appcompat.R.id.search_close_btn);
        if (closeIcon != null) closeIcon.setImageResource(R.drawable.ic_toolbar_close);

        ImageView searchIcon = (ImageView) mSearchView.findViewById(
                android.support.v7.appcompat.R.id.search_mag_icon);
        ViewHelper.removeSearchViewSearchIcon(searchIcon);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String string) {
                filterSearch(string);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String string) {
                mSearchView.clearFocus();
                return true;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomPadding(
                getActivity(), mIconsGrid, newConfig.orientation);
        ViewHelper.resetSpanCount(getActivity(), mIconsGrid, R.integer.icons_column_count);
    }

    @Override
    public void onDestroy() {
        if (mGetIcons != null) mGetIcons.cancel(true);
        super.onDestroy();
    }

    private void filterSearch(String query) {
        try {
            mAdapter.search(query);
            if (mAdapter.getItemCount()==0) {
                String text = String.format(getActivity().getResources().getString(
                        R.string.search_noresult), query);
                mSearchResult.setText(text);
                mSearchResult.setVisibility(View.VISIBLE);
            }
            else mSearchResult.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    private void getIcons() {
        mGetIcons = new AsyncTask<Void, Void, Boolean>() {

            List<Icon> icons;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                icons = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (CandyBarMainActivity.sSections == null) {
                            CandyBarMainActivity.sSections = IconsHelper.getIconsList(getActivity());
                            CandyBarMainActivity.sIconsCount = 0;

                            for (Icon section : CandyBarMainActivity.sSections) {
                                CandyBarMainActivity.sIconsCount += section.getIcons().size();

                                if (getActivity().getResources().getBoolean(R.bool.show_icon_name)) {
                                    for (Icon icon : section.getIcons()) {
                                        String name = IconsHelper.replaceName(getActivity(),
                                                getActivity().getResources().getBoolean(R.bool.enable_icon_name_replacer),
                                                icon.getTitle());
                                        icon.setTitle(name);
                                    }
                                }
                            }
                        }

                        for (Icon icon : CandyBarMainActivity.sSections)
                            icons.addAll(icon.getIcons());

                        Collections.sort(icons, new AlphanumComparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                String s1 = ((Icon) o1).getTitle();
                                String s2 = ((Icon) o2).getTitle();
                                return super.compare(s1, s2);
                            }
                        });
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                    }
                    return true;
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    mAdapter = new IconsAdapter(getActivity(), icons, true);
                    mIconsGrid.setAdapter(mAdapter);
                    mSearchView.requestFocus();
                    SoftKeyboardHelper.openKeyboard(getActivity());
                } else {
                    //Unable to load all icons
                    Toast.makeText(getActivity(), R.string.icons_load_failed,
                            Toast.LENGTH_LONG).show();
                }
                mGetIcons = null;
            }
        }.execute();
    }

}
