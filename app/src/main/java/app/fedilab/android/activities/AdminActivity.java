/* Copyright 2019 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */
package app.fedilab.android.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import app.fedilab.android.R;
import app.fedilab.android.fragments.DisplayAdminAccountsFragment;
import app.fedilab.android.fragments.DisplayAdminReportsFragment;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 19/06/2019.
 * Admin activity
 */

public class AdminActivity extends BaseActivity  {


    private boolean unresolved;
    private boolean local, remote, active, pending, disabled, silenced, suspended;
    private DisplayAdminReportsFragment displayAdminReportsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

        setTheme(R.style.AppAdminTheme);

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(String.format(getString(R.string.administration)+ " %s", Helper.getLiveInstance(getApplicationContext())));
        }
        setContentView(R.layout.activity_admin);
        unresolved = true;

        ViewPager admin_viewpager = findViewById(R.id.admin_viewpager);

        TabLayout admin_tablayout = findViewById(R.id.admin_tablayout);
        admin_tablayout.addTab(admin_tablayout.newTab().setText(getString(R.string.reports)));
        admin_tablayout.addTab(admin_tablayout.newTab().setText(getString(R.string.accounts)));


        final LinearLayout tabStrip = (LinearLayout) admin_tablayout.getChildAt(0);
        tabStrip.getChildAt(0).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popup = new PopupMenu(AdminActivity.this, tabStrip.getChildAt(0));
                popup.getMenuInflater()
                        .inflate(R.menu.option_filter_admin_reports, popup.getMenu());
                Menu menu = popup.getMenu();
                final MenuItem itemUnresolved = menu.findItem(R.id.action_unresolved_reports);


                itemUnresolved.setChecked(unresolved);

                popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        if( displayAdminReportsFragment != null)
                            displayAdminReportsFragment.refreshFilter();
                    }
                });
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                        item.setActionView(new View(getApplicationContext()));
                        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                            @Override
                            public boolean onMenuItemActionExpand(MenuItem item) {
                                return false;
                            }

                            @Override
                            public boolean onMenuItemActionCollapse(MenuItem item) {
                                return false;
                            }
                        });
                        if (item.getItemId() == R.id.action_unresolved_reports) {
                            unresolved = !unresolved;
                        }
                        if( admin_tablayout.getTabAt(0) != null)
                            //noinspection ConstantConditions
                            admin_tablayout.getTabAt(0).select();
                        PagerAdapter mPagerAdapter = new AdminPagerAdapter(getSupportFragmentManager());
                        admin_viewpager.setAdapter(mPagerAdapter);
                        itemUnresolved.setChecked(unresolved);
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });



        PagerAdapter mPagerAdapter = new AdminPagerAdapter(getSupportFragmentManager());
        admin_viewpager.setAdapter(mPagerAdapter);

        admin_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = admin_tablayout.getTabAt(position);
                if( tab != null)
                    tab.select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        admin_tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                admin_viewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment fragment = null;
                if( admin_viewpager.getAdapter() != null)
                    fragment = (Fragment) admin_viewpager.getAdapter().instantiateItem(admin_viewpager, tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        if( fragment != null) {
                            displayAdminReportsFragment = ((DisplayAdminReportsFragment) fragment);
                            displayAdminReportsFragment.scrollToTop();
                        }
                        break;
                    case 1:
                        if( fragment != null) {
                            DisplayAdminAccountsFragment displayAdminAccountsFragment = ((DisplayAdminAccountsFragment) fragment);
                            displayAdminAccountsFragment.scrollToTop();
                        }
                        break;

                }
            }
        });

    }


    private class AdminPagerAdapter extends FragmentStatePagerAdapter {

        AdminPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            switch (position){
                case 0:
                    DisplayAdminReportsFragment displayAdminReportsFragment = new DisplayAdminReportsFragment();
                    bundle = new Bundle();
                    bundle.putBoolean("unresolved",unresolved);
                    displayAdminReportsFragment.setArguments(bundle);
                    return displayAdminReportsFragment;
                case 1:
                    DisplayAdminAccountsFragment displayAdminAccountsFragment = new DisplayAdminAccountsFragment();
                    return displayAdminAccountsFragment;
            }
            return null;
        }


        @Override
        public int getCount() {
            return 2;
        }
    }

}
