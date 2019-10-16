package com.example.gb28181_videoplatform.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 *  TabLayout title
 */

public class TabPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> tabFragments;

    private List<String> tabIndicators;
    public void setTabFragments(List<Fragment> tabFragments) {
        this.tabFragments = tabFragments;
    }

    public void setTabIndicators(List<String> tabIndicators) {
        this.tabIndicators = tabIndicators;
    }

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return tabFragments.get(position);
    }

    @Override
    public int getCount() {
        return tabFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabIndicators.get(position);
    }
}
