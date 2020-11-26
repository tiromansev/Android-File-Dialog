package com.tiromansev.filedialog;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BreadCrumbs {

    private HashMap<View, Integer> items = new HashMap<>();
    private Toolbar toolbar = null;
    private HorizontalScrollView parent = null;
    private Context context;
    private final int breadCrumbsColor = ColorUtils.getAttrColor(R.attr.bread_crumbs_color, this.context);
    private SelectItemListener itemClickListener;
    private LinearLayout.LayoutParams layoutParams;
    private static final String ITEMS = "ITEMS";
    public static final int UNDEFINED_VALUE = -1;

    public BreadCrumbs(Context context) {
        this.context = context;
        layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = (int) context.getResources().getDimension(R.dimen.breadcrumb_button_margin);
        int topMargin = (int) context.getResources().getDimension(R.dimen.breadcrumb_top_margin);
        layoutParams.setMargins(0, 0, margin, topMargin);
        toolbar = new Toolbar(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.breadcrumb_toolbar_height));
        layoutParams.gravity = Gravity.LEFT;
        this.toolbar.setLayoutParams(layoutParams);
        this.toolbar.setPopupTheme(R.style.AppTheme_PopupMenu);
        this.toolbar.setTitleTextAppearance(context, R.style.ToolbarTitle);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setItemClickListener(SelectItemListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setToolbarVisible(boolean visible) {
        if (getToolbar() != null) {
            getToolbar().setVisibility(visible ? View.VISIBLE : View.GONE);
            if (parent != null) {
                parent.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    public int getItemCount() {
        return items.size();
    }

    public void attachTo(HorizontalScrollView parent) {
        this.parent = parent;
        removeViews();
        this.parent.setBackgroundColor(breadCrumbsColor);
        this.parent.addView(toolbar);
    }

    private void removeViews() {
        if (toolbar.getParent() != null) {
            ((HorizontalScrollView) toolbar.getParent()).removeView(toolbar);
        }
    }

    public void detachFrom() {
        removeViews();
        this.parent = null;
        itemClickListener = null;
    }

    public void addHomeItem(String itemTag) {
        ImageButton button = new ImageButton(context);
        button.setTag(itemTag);
        button.setLayoutParams(layoutParams);
        button.setImageResource(R.mipmap.ic_home);
        button.setBackgroundColor(breadCrumbsColor);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeClick();
            }
        });
        toolbar.addView(button);
        items.put(button, items.size() + 1);
    }

    public void addDelimiter() {
        ImageButton button = new ImageButton(context);
        button.setLayoutParams(layoutParams);
        button.setImageResource(R.mipmap.ic_delimiter);
        button.setBackgroundColor(breadCrumbsColor);
        toolbar.addView(button);
        items.put(button, items.size() + 1);
    }

    private void homeClick() {
        clearItems();
        if (itemClickListener != null) {
            itemClickListener.onItemSelect(String.valueOf(UNDEFINED_VALUE));
        }
    }

    private void itemClick(View view) {
        boolean removeItem = true;
        if (itemClickListener != null) {
            removeItem = itemClickListener.onItemSelect((String) view.getTag());
        }
        if (items.get(view) != null && removeItem) {
            removeRightItems(view);
        }
    }

    private void removeRightItems(View view) {
        if (!items.isEmpty()) {
            int itemId = items.get(view);
            if (itemId > 0) {
                for (Iterator<Map.Entry<View, Integer>> it = items.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<View, Integer> entry = it.next();
                    if (entry.getValue() > itemId) {
                        toolbar.removeView(entry.getKey());
                        it.remove();
                    }
                }
            }
        }
    }
    private View getLastView() {
        View lastView = null;
        int lastIndex = 0;
        for (Map.Entry<View, Integer> entry : items.entrySet()) {
            if (lastView == null) {
                lastView = entry.getKey();
                lastIndex = entry.getValue();
            }
            else if (lastIndex < entry.getValue()) {
                lastView = entry.getKey();
                lastIndex = entry.getValue();
            }
        }
        return lastView;
    }

    private void removeLastItem() {
        if (!items.isEmpty()) {
            View lastView = getLastView();
            if (lastView != null) {
                toolbar.removeView(lastView);
                items.remove(lastView);
            }
        }
    }

    public void lastItemClick() {
        if (itemClickListener == null) {
            return;
        }
        if (getToolbar() != null && getToolbar().getVisibility() == View.VISIBLE) {
            View lastView = getLastView();
            if (lastView != null) {
                int groupId = Integer.parseInt((String) lastView.getTag());
                if (groupId == UNDEFINED_VALUE) {
                    clearItems();
                }
                itemClickListener.onItemSelect(String.valueOf(groupId));
                toolbar.removeView(lastView);
                items.remove(lastView);
                removeLastItem();
                lastView = getLastView();
                if (lastView != null) {
                    groupId = Integer.parseInt((String) lastView.getTag());
                    if (groupId == UNDEFINED_VALUE) {
                        clearItems();
                    }
                    itemClickListener.onItemSelect(String.valueOf(groupId));
                }
            }
        }
    }

    public void addItem(String itemName, String itemTag) {
        TextView button = new TextView(context);
        button.setTag(itemTag);
        button.setText(itemName);
        button.setLayoutParams(layoutParams);
        button.setBackgroundColor(breadCrumbsColor);
        button.setTextColor(context.getResources().getColor(R.color.color_white));
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(false);
        button.setBackgroundResource(R.drawable.breadcrumb_button);
        int buttonPadding = (int) context.getResources().getDimension(R.dimen.breadcrumb_button_padding);
        button.setPadding(buttonPadding, 0, 0, 0);
        button.setTypeface(null, Typeface.BOLD);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick(view);
            }
        });
        addDelimiter();
        toolbar.addView(button);
        items.put(button, items.size() + 1);
        if (toolbar != null) {
            button.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (parent != null) {
                        parent.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }
            }, 100L);
        }
    }

    public HashMap<String, Integer> getItems() {
        HashMap<String, Integer> saveItems = new HashMap<>();
        if (!items.isEmpty()) {
            for (Map.Entry<View, Integer> entry: items.entrySet()) {
                if (entry.getKey() instanceof TextView) {
                    String name = ((TextView) entry.getKey()).getText().toString();
                    saveItems.put(name, Integer.parseInt((String.valueOf(entry.getKey().getTag()))));
                }
            }
        }
        return saveItems;
    }

    public void saveState(Bundle outState, String tag) {
        outState.putSerializable(tag, getItems());
    }

    public void saveState(Bundle outState) {
        saveState(outState, ITEMS);
    }

    public void setItems(HashMap<String, Integer> restoreItems) {
        if (!restoreItems.isEmpty()) {
            Map<String, Integer> sortedItems = new TreeMap<>(restoreItems);
            Set<Map.Entry<String, Integer>> set = sortedItems.entrySet();
            Iterator<Map.Entry<String, Integer>> iterator = set.iterator();
            addHomeItem(String.valueOf(UNDEFINED_VALUE));
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = iterator.next();
                addItem(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    public void restoreState(Bundle inState, String tag) {
        if (inState != null) {
            HashMap<String, Integer> restoreItems = (HashMap<String, Integer>) inState.getSerializable(tag);
            setItems(restoreItems);
        }
    }

    public void restoreState(Bundle inState) {
        restoreState(inState, ITEMS);
    }

    public void clearItems() {
        items.clear();
        if (toolbar != null) {
            toolbar.removeAllViews();
        }
    }

    public interface SelectItemListener {
        boolean onItemSelect(String itemTag);
    }

}
