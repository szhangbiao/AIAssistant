package cn.booslink.llm.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.R;
import cn.booslink.llm.common.utils.RxUtil;
import cn.booslink.llm.common.utils.ScreenUtils;
import cn.booslink.llm.common.model.GuideItem;
import cn.booslink.llm.repository.IGuideRepository;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class GuideActivity extends AppCompatActivity {

    @Inject
    IGuideRepository mGuideRepository;

    private RecyclerView mRvMenu;
    private ScrollView mSvDetail;
    private TextView mTvDetailInstruction;

    private final List<GuideItem> mGuideItems = new ArrayList<>();
    private int mSelectedPosition = 0;
    private final CompositeDisposable mDisposables = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ScreenUtils.setupFullScreen(getWindow());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initViews();
        loadGuideData();
    }

    private void initViews() {
        TextView mTvBack = findViewById(R.id.tv_back);
        mRvMenu = findViewById(R.id.rv_menu);
        mSvDetail = findViewById(R.id.sv_detail);
        mTvDetailInstruction = findViewById(R.id.tv_detail_instruction);
        mTvBack.setOnClickListener(v -> finish());

        // Handle D-pad Down from back button to focus the last focused/selected menu item
        mTvBack.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    focusRecyclerViewItem(mSelectedPosition);
                    return true;
                }
            }
            return false;
        });

        // Handle D-pad Up/Down to scroll and Left to return to menu when ScrollView is focused
        mSvDetail.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    mSvDetail.arrowScroll(View.FOCUS_DOWN);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    mSvDetail.arrowScroll(View.FOCUS_UP);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    focusRecyclerViewItem(mSelectedPosition);
                    return true;
                }
            }
            return false;
        });
    }

    private void focusRecyclerViewItem(int position) {
        RecyclerView.LayoutManager lm = mRvMenu.getLayoutManager();
        if (lm != null) {
            View view = lm.findViewByPosition(position);
            if (view != null) {
                view.requestFocus();
            } else {
                mRvMenu.scrollToPosition(position);
                mRvMenu.post(() -> {
                    View v = lm.findViewByPosition(position);
                    if (v != null) {
                        v.requestFocus();
                    }
                });
            }
        }
    }

    private void loadGuideData() {
        mDisposables.add(mGuideRepository.getGuideItems()
                .compose(RxUtil.singleOnMain())
                .subscribe(
                        items -> {
                            mGuideItems.clear();
                            mGuideItems.addAll(items);
                            setupMenu();
                        },
                        Throwable::printStackTrace
                )
        );
    }

    @Override
    protected void onDestroy() {
        mDisposables.clear();
        super.onDestroy();
    }

    private void setupMenu() {
        mRvMenu.setLayoutManager(new LinearLayoutManager(this));
        MenuAdapter mAdapter = new MenuAdapter(mGuideItems, (item, position) -> {
            updateSelectedPosition(position);
            mTvDetailInstruction.setText(item.getInstruction());
            mSvDetail.scrollTo(0, 0);
        });
        mRvMenu.setAdapter(mAdapter);

        // Preload first item instruction
        if (!mGuideItems.isEmpty()) {
            mTvDetailInstruction.setText(mGuideItems.get(0).getInstruction());
        }

        // Post requestFocus on the first item to select it visually
        mRvMenu.post(() -> {
            RecyclerView.LayoutManager lm = mRvMenu.getLayoutManager();
            if (lm != null) {
                View firstItem = lm.findViewByPosition(0);
                if (firstItem != null) {
                    firstItem.requestFocus();
                }
            }
        });
    }

    private void updateSelectedPosition(int newPosition) {
        int oldPosition = mSelectedPosition;
        mSelectedPosition = newPosition;

        RecyclerView.ViewHolder oldHolder = mRvMenu.findViewHolderForAdapterPosition(oldPosition);
        if (oldHolder != null) {
            oldHolder.itemView.setSelected(false);
        }
        RecyclerView.ViewHolder newHolder = mRvMenu.findViewHolderForAdapterPosition(newPosition);
        if (newHolder != null) {
            newHolder.itemView.setSelected(true);
        }
    }


    // Interface for item updates
    public interface OnItemSelectListener {
        void onItemSelect(GuideItem item, int position);
    }

    // RecyclerView Adapter (non-static inner class to access mSelectedPosition)
    private class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {

        private final List<GuideItem> items;
        private final OnItemSelectListener selectListener;

        public MenuAdapter(List<GuideItem> items, OnItemSelectListener selectListener) {
            this.items = items;
            this.selectListener = selectListener;
        }

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guide_menu, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            GuideItem item = items.get(position);
            holder.tvTitle.setText(item.getTitle());

            // Bind current selection state (visual highlight retention)
            holder.itemView.setSelected(position == mSelectedPosition);

            // Handle focus changes (crucial for TV D-pad navigation)
            holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (selectListener != null) {
                        selectListener.onItemSelect(item, holder.getAdapterPosition());
                    }
                }
            });

            // Handle touch clicks (for tablets or touch screens)
            holder.itemView.setOnClickListener(v -> {
                v.requestFocus();
                if (selectListener != null) {
                    selectListener.onItemSelect(item, holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    private static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_menu_title);
        }
    }
}
