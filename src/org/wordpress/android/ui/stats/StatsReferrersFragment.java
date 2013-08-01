package org.wordpress.android.ui.stats;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.datasets.StatsReferrersTable;
import org.wordpress.android.providers.StatsContentProvider;
import org.wordpress.android.ui.HorizontalTabView.TabListener;

public class StatsReferrersFragment extends StatsAbsListViewFragment  implements TabListener {

    @Override
    public FragmentPagerAdapter getAdapter() {
        return new CustomPagerAdapter(getChildFragmentManager());
    }

    private class CustomPagerAdapter extends FragmentPagerAdapter {

        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int entryLabelResId = R.string.stats_entry_referrers;
            int totalsLabelResId = R.string.stats_totals_views;
            StatsCursorFragment fragment = StatsCursorFragment.newInstance(StatsContentProvider.STATS_REFERRERS_URI, entryLabelResId, totalsLabelResId);
            mFragmentMap.put(position, fragment);
            fragment.setListAdapter(new CustomCursorAdapter(getActivity(), null));
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return StatsTimeframe.TODAY.getLabel();
            else if (position == 1)
                return StatsTimeframe.YESTERDAY.getLabel();
            else 
                return ""; 
        }
        
    }
    
    public class CustomCursorAdapter extends CursorAdapter {

        public CustomCursorAdapter(Context context, Cursor c) {
            super(context, c, true);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            
            String entry = cursor.getString(cursor.getColumnIndex(StatsReferrersTable.Columns.TITLE));
            String url = cursor.getString(cursor.getColumnIndex(StatsReferrersTable.Columns.URL));
            int total = cursor.getInt(cursor.getColumnIndex(StatsReferrersTable.Columns.VIEWS));
            String imageUrl = cursor.getString(cursor.getColumnIndex(StatsReferrersTable.Columns.IMAGE_URL));

            // entries
            TextView entryTextView = (TextView) view.findViewById(R.id.stats_list_cell_entry);
            if (url != null && url.length() > 0) {
                Spanned link = Html.fromHtml("<a href=\"" + url + "\">" + entry + "</a>");
                entryTextView.setText(link);
                entryTextView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                entryTextView.setText(entry);
            }
            
            // totals
            TextView totalsTextView = (TextView) view.findViewById(R.id.stats_list_cell_total);
            totalsTextView.setText(total + "");
            
            // image
            NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.stats_list_cell_image);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageUrl(imageUrl, WordPress.imageLoader);
            
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup root) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.stats_list_cell, root, false);
        }

    }
    
    @Override
    public String getTitle() {
        return getString(R.string.stats_view_referrers);
    }

}
