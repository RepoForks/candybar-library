package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.utils.Animator;
import com.dm.material.dashboard.candybar.utils.Tag;

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

public class ViewHelper {

    private static final float PERCENTAGE_TO_SHOW_TOOLBAR_TITLE  = 0.85f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_CONTAINER = 0.7f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    public static void resetNavigationBarTranslucent(@NonNull Context context, boolean translucent, int orientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!translucent) {
                int color = ColorHelper.getAttributeColor(context, R.attr.colorPrimaryDark);
                ColorHelper.setNavigationBarColor(context, color);
                return;
            }

            boolean tabletMode = context.getResources().getBoolean(R.bool.tablet_mode);

            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                ((AppCompatActivity) context).getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((AppCompatActivity) context).getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                ColorHelper.setNavigationBarColor(context, Color.BLACK);
            }
        }
    }

    public static int getStatusBarHeight(@NonNull Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getNavigationBarHeight(@NonNull Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        if (appUsableSize.x < realScreenSize.x) {
            Point point = new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
            return point.x;
        }

        if (appUsableSize.y < realScreenSize.y) {
            Point point = new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
            return point.y;
        }
        return 0;
    }

    private static Point getAppUsableScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
                size.x = display.getWidth();
                size.y = display.getHeight();
            }
        }
        return size;
    }

    public static void resetNavigationBarBottomPadding(@NonNull Context context, @Nullable View view,
                                                       int orientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!context.getResources().getBoolean(R.bool.use_translucent_navigation_bar))
                return;

            if (view != null) {
                int left = view.getPaddingLeft();
                int right = view.getPaddingRight();
                int bottom = view.getPaddingBottom();
                int top = view.getPaddingTop();
                int navBar = getNavigationBarHeight(context);

                if (bottom > navBar) bottom -= navBar;
                boolean tabletMode = context.getResources().getBoolean(R.bool.tablet_mode);

                if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                    view.setPadding(left, top, right, (bottom + navBar));
                    return;
                }

                view.setPadding(left, top, right, bottom);
            }
        }
    }

    public static boolean handleToolbarTitleVisibility(@NonNull Context context, float percentage,
                                                       boolean isToolbarTitleVisible,
                                                       @NonNull View toolbarTitle) {
        if (percentage >= PERCENTAGE_TO_SHOW_TOOLBAR_TITLE) {
            if (!isToolbarTitleVisible) {
                Animator.startAlphaAnimation(
                        toolbarTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isToolbarTitleVisible = true;
                ColorHelper.setTransparentStatusBar(context, Color.TRANSPARENT);
                ColorHelper.setStatusBarIconColor(context);
            }
        } else {
            if (isToolbarTitleVisible) {
                Animator.startAlphaAnimation(
                        toolbarTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                isToolbarTitleVisible = false;
                ColorHelper.setTransparentStatusBar(context, Color.parseColor("#22000000"));
                ColorHelper.setStatusBarIconColor(context);
            }
        }
        return isToolbarTitleVisible;
    }

    public static boolean handleTitleContainerVisibility(float percentage, boolean isTitleContainerVisible,
                                                         @NonNull View titleContainer, @NonNull FloatingActionButton fab) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_CONTAINER) {
            if (isTitleContainerVisible) {
                Animator.startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                Animator.hideFab(fab);
                isTitleContainerVisible = false;
            }
        } else {
            if (!isTitleContainerVisible) {
                Animator.startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                Animator.showFab(fab);
                isTitleContainerVisible = true;
            }
        }
        return isTitleContainerVisible;
    }

    public static void disableAppBarDrag(@Nullable AppBarLayout appBar) {
        if (appBar != null) {
            if (ViewCompat.isLaidOut(appBar)) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                        appBar.getLayoutParams();
                AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                if (behavior != null) {
                    behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                        @Override
                        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                            return false;
                        }
                    });
                }
            } else {
                Log.d(Tag.LOG_TAG, "ViewCompat.isLaidOut(appBar) = false");
            }
        }
    }

    public static void changeSearchViewTextColor(@Nullable View view, int text, int hint) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(text);
                ((TextView) view).setHintTextColor(hint);
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i), text, hint);
                }
            }
        }
    }

    public static void removeSearchViewSearchIcon(@Nullable View view) {
        if (view != null) {
            ImageView searchIcon = (ImageView) view;
            ViewGroup linearLayoutSearchView = (ViewGroup) view.getParent();
            if (linearLayoutSearchView != null) {
                linearLayoutSearchView.removeView(searchIcon);
                linearLayoutSearchView.addView(searchIcon);

                searchIcon.setAdjustViewBounds(true);
                searchIcon.setMaxWidth(0);
                searchIcon.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                searchIcon.setImageDrawable(null);
            }
        }
    }

    public static void resetSpanCount(@NonNull Context context, @NonNull RecyclerView recyclerView, @IntegerRes int id) {
        try {
            GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
            manager.setSpanCount(context.getResources().getInteger(id));
            manager.requestLayout();
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }
}
