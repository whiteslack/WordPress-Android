package org.wordpress.android.ui.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.wordpress.android.R;
import org.wordpress.android.util.ColorUtilsKt;

public class WPPreference extends Preference implements PreferenceHint {
    private String mHint;

    public WPPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DetailListPreference);

        for (int i = 0; i < array.getIndexCount(); ++i) {
            int index = array.getIndex(i);
            if (index == R.styleable.DetailListPreference_longClickHint) {
                mHint = array.getString(index);
            }
        }

        array.recycle();
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        Resources res = getContext().getResources();
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_sz_large));
            titleView.setTextColor(res.getColor(
                    isEnabled() ? ColorUtilsKt.getThemeColorResId(getContext(), R.attr.colorText)
                            : R.color.neutral_20));
        }
        if (summaryView != null) {
            summaryView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_sz_medium));
            summaryView.setTextColor(res.getColor(isEnabled() ? R.color.neutral : R.color.neutral_20));
        }
    }

    @Override
    public boolean hasHint() {
        return !TextUtils.isEmpty(mHint);
    }

    @Override
    public String getHint() {
        return mHint;
    }

    @Override
    public void setHint(String hint) {
        mHint = hint;
    }
}
