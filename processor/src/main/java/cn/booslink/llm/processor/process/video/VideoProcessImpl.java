package cn.booslink.llm.processor.process.video;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.app.IAppProcess;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class VideoProcessImpl implements IVideoProcess {

    private static final String SLOT_CATEGORY = "category";
    private static final String SLOT_NAME = "name";

    private final String IQIYI_PACKAGE_NAME = "com.gitvvideo.shenzhenweihaocpa";

    private final Context mContext;
    private final IAppProcess mAppProcess;
    private final IVideoAction mIQiYiVideoAction;

    @Inject
    public VideoProcessImpl(@ApplicationContext Context context, IAppProcess appProcess, @Named("iqiyi") IVideoAction iQiYiVideoAction) {
        this.mContext = context;
        this.mAppProcess = appProcess;
        this.mIQiYiVideoAction = iQiYiVideoAction;
    }

    @Override
    public boolean shouldVideoProcess(String foregroundPkgName, Category category, AIUIIntent intent) {
        boolean isAppOpened = IQIYI_PACKAGE_NAME.equals(foregroundPkgName);
        return category == Category.VIDEO || (isAppOpened && category == Category.CONTROL && (
                //intent == AIUIIntent.XXX || // 打开搜索页面
                intent == AIUIIntent.RESUME_PLAY || // 播放
                        intent == AIUIIntent.PAUSE || // 暂停
                        intent == AIUIIntent.REPLAY || // 重新播放
                        intent == AIUIIntent.CHOOSE_NEXT || // 下一集
                        intent == AIUIIntent.CHOOSE_WHICH || intent == AIUIIntent.CHOOSE_LAST || // 选集播放
                        intent == AIUIIntent.FAST_FORWARD || // 快进
                        intent == AIUIIntent.REWIND || // 快退
                        intent == AIUIIntent.PLAYTIME_SET || // 跳转到指定时间
                        intent == AIUIIntent.BRIGHT_UP || intent == AIUIIntent.BRIGHT_DOWN || intent == AIUIIntent.BRIGHT_MAX || intent == AIUIIntent.BRIGHT_MIN ||// 修改亮度
                        intent == AIUIIntent.VOLUME_PLUS || intent == AIUIIntent.VOLUME_MINUS || intent == AIUIIntent.VOLUME_MAX || intent == AIUIIntent.VOLUME_MIN || intent == AIUIIntent.UNMUTE || intent == AIUIIntent.MUTE || // 修改音量
                        intent == AIUIIntent.SKIP_SET // 跳过片头， 跳过片尾
        )) || (isAppOpened && category == Category.PAGE_CONTROL && (
                intent == AIUIIntent.PAGE_OPEN || // 打开登录、收银台、播放历史、收藏、xx频道榜单、首页XX频道
                        intent == AIUIIntent.PAGE_BACK //返回到上一级页面
        )) || (isAppOpened && category == Category.VIDEO_ENHANCE && (
                intent == AIUIIntent.SPEED_DOWN || intent == AIUIIntent.SPEED_UP || intent == AIUIIntent.CHANGE_SPEED || // 切换倍速
                        intent == AIUIIntent.CLARITY_DOWN || intent == AIUIIntent.CLARITY_UP || intent == AIUIIntent.CHANGE_CLARITY || //  切换清晰度
                        intent == AIUIIntent.FAVORITE_REMOVE || intent == AIUIIntent.FAVORITE_ADD || // 收藏/取消收藏
                        intent == AIUIIntent.CLOSE_DANMU || intent == AIUIIntent.OPEN_DANMU // 开启/关闭弹幕
        ));
    }

    @Override
    public boolean handleVideoIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        if (intent == AIUIIntent.QUERY) {
            return populateActionBySlots(slots);
        }
        Intent actionIntent = populateByVideoAction(foregroundPkgName, intent, slots);
        if (actionIntent != null) {
            startIntent(actionIntent);
            return true;
        }
        return false;
    }

    private boolean populateActionBySlots(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return false;
        for (Slot slot : slots) {
            if (SLOT_CATEGORY.equals(slot.getName())) {
                String category = slot.getValue();
                Intent homeChannel = mIQiYiVideoAction.openHomeChannel(category);
                if (homeChannel != null) {
                    mAppProcess.launchAppWithIntent(IQIYI_PACKAGE_NAME, homeChannel);
                    return true;
                } else {
                    return false;
                }
            } else if (SLOT_NAME.equals(slot.getName())) {
                String name = slot.getValue();
                Intent intent = mIQiYiVideoAction.search(name);
                mAppProcess.launchAppWithIntent(IQIYI_PACKAGE_NAME, intent);
                return true;
            }
        }
        return false;
    }

    private Intent populateByVideoAction(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        IVideoAction videoAction = getVideoActionByPkgName(foregroundPkgName);
        switch (intent) {
            case EXIT:
                return videoAction.exitApp();
            case RESUME_PLAY:
                return videoAction.play();
            case PAUSE:
                return videoAction.pause();
            case REPLAY:
                return videoAction.replay();
            case CHOOSE_NEXT:
                return videoAction.next();
            case CHOOSE_WHICH:
                String num = getPlayNumberBySlot(slots);
                if (TextUtils.isEmpty(num)) return null;
                return videoAction.choosePlay(num);
            case CHOOSE_LAST:
                return videoAction.choosePlay("END");
            case FAST_FORWARD:
                String forward = getDurationBySlot(slots);
                return videoAction.fastForward(forward);
            case REWIND:
                String backword = getDurationBySlot(slots);
                return videoAction.fastBackword(backword);
            case PLAYTIME_SET:
                String playTime = getDurationBySlot(slots);
                return videoAction.seekTo(playTime);
            case BRIGHT_UP:
                return videoAction.changeBright("UP");
            case BRIGHT_DOWN:
                return videoAction.changeBright("DOWN");
            case BRIGHT_MAX:
                return videoAction.changeBright("225");
            case BRIGHT_MIN:
                return videoAction.changeBright("0");
            case VOLUME_PLUS:
                return videoAction.changeVolume("UP");
            case VOLUME_MINUS:
                return videoAction.changeVolume("DOWN");
            case VOLUME_MAX:
                return videoAction.changeVolume("500");
            case MUTE:
            case VOLUME_MIN:
                return videoAction.changeVolume("0");
            case UNMUTE:
                return videoAction.changeVolume("250");
            case SKIP_SET:
                int skipValue = getSkipValueBySlot(slots);
                if (skipValue > 0) {
                    return videoAction.skipHead();
                } else if (skipValue < 0) {
                    return videoAction.skipTile();
                } else {
                    return null;
                }
            case SPEED_DOWN:
                return videoAction.changeSpeed("DOWN");
            case SPEED_UP:
                return videoAction.changeSpeed("UP");
            case CHANGE_SPEED:
                // TODO get speed by slots
                return null;
            case CLARITY_DOWN:
                return videoAction.changeRate("DOWN");
            case CLARITY_UP:
                return videoAction.changeRate("UP");
            case CHANGE_CLARITY:
                // TODO get rate by slots
                return null;
            case FAVORITE_REMOVE:
                return videoAction.changeFavorite("false");
            case FAVORITE_ADD:
                return videoAction.changeFavorite("true");
            case OPEN_DANMU:
                return videoAction.changeDanMu("true");
            case CLOSE_DANMU:
                return videoAction.changeDanMu("false");
        }
        return null;
    }

    private int getSkipValueBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return 0;
        for (Slot slot : slots) {
            if ("skipType".equals(slot.getName())) {
                if ("片头".equals(slot.getValue())) {
                    return 1;
                } else if ("片尾".equals(slot.getValue())) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private void startIntent(Intent intent) {
        if (intent == null) return;
        mContext.startActivity(intent);
    }

    private IVideoAction getVideoActionByPkgName(String foregroundPackage) {
        // TODO 根据packageName获取VideoAction
        return mIQiYiVideoAction;
    }

    private String getPlayNumberBySlot(@NotNull List<Slot> slots) {
        for (Slot slot : slots) {
            if ("number".equals(slot.getName())) {
                return slot.getNormValue();
            }
        }
        return null;
    }

    private String getDurationBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return "10000"; // 10s
        int duration = 0;
        for (Slot slot : slots) {
            if ("second".equals(slot.getName())) {
                int seconds = tryParseIntNum(slot.getNormValue());
                duration += seconds;
            } else if ("minute".equals(slot.getName())) {
                int minutes = tryParseIntNum(slot.getNormValue());
                duration += minutes * 60;
            } else if ("hour".equals(slot.getName())) {
                int hours = tryParseIntNum(slot.getNormValue());
                duration += hours * 3600;
            }
        }
        return duration != 0 ? String.valueOf(duration * 1000) : "10000";
    }

    private int tryParseIntNum(String value) {
        int intNum;
        try {
            intNum = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            intNum = 0;
        }
        return intNum;
    }
}
