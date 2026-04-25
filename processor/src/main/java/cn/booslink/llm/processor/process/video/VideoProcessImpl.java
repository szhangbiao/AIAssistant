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
import cn.booslink.llm.downloader.utils.PkgUtils;
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
    public boolean shouldVideoProcess(Category category, AIUIIntent intent) {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        return category == Category.VIDEO || (IQIYI_PACKAGE_NAME.equals(foregroundPackage) && category == Category.CONTROL && (
                intent == AIUIIntent.EXIT || // 退出APP
                        //intent == AIUIIntent.XXX || // 返回到上一级页面
                        //intent == AIUIIntent.XXX || // 打开登录页面
                        //intent == AIUIIntent.XXX || // 打开收银台页面
                        //intent == AIUIIntent.XXX || // 打开播放历史页面
                        //intent == AIUIIntent.XXX || // 打开收藏页面
                        //intent == AIUIIntent.XXX || // 打开榜单页面
                        //intent == AIUIIntent.XXX || // 打开首页频道页面
                        //intent == AIUIIntent.XXX || // 打开搜索页面
                        intent == AIUIIntent.RESUME_PLAY || // 播放
                        intent == AIUIIntent.PAUSE || // 暂停
                        intent == AIUIIntent.REPLAY || // 重新播放
                        intent == AIUIIntent.CHOOSE_NEXT || // 下一集
                        intent == AIUIIntent.CHOOSE_WHICH || intent == AIUIIntent.CHOOSE_LAST || // 选集播放
                        intent == AIUIIntent.FAST_FORWARD || // 快进
                        intent == AIUIIntent.REWIND || // 快退
                        intent == AIUIIntent.PLAYTIME_SET || // 跳转到指定时间
                        // intent == AIUIIntent.XXX || // 切换倍速
                        // intent == AIUIIntent.XXX || // 切换清晰度
                        intent == AIUIIntent.BRIGHT_UP || intent == AIUIIntent.BRIGHT_DOWN || intent == AIUIIntent.BRIGHT_MAX || intent == AIUIIntent.BRIGHT_MIN ||// 修改亮度
                        intent == AIUIIntent.VOLUME_PLUS || intent == AIUIIntent.VOLUME_MINUS || intent == AIUIIntent.VOLUME_MAX || intent == AIUIIntent.VOLUME_MIN || intent == AIUIIntent.UNMUTE || intent == AIUIIntent.MUTE || // 修改音量
                        intent == AIUIIntent.SKIP_SET // 跳过片头， 跳过片尾
                //intent == AIUIIntent.XXX || // // 收藏/取消收藏
                //intent == AIUIIntent.XXX || // // 开启/关闭弹幕

        ));
    }

    @Override
    public boolean handleVideoIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        if (intent == AIUIIntent.QUERY) {
            return populateActionBySlots(slots);
        }
        return populateByVideoAction(intent, slots);
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

    private boolean populateByVideoAction(AIUIIntent intent, @NotNull List<Slot> slots) {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        IVideoAction videoAction = getVideoActionByPkgName(foregroundPackage);
        switch (intent) {
            case EXIT:
                return startIntent(videoAction.exitApp());
            case RESUME_PLAY:
                return startIntent(videoAction.play());
            case PAUSE:
                return startIntent(videoAction.pause());
            case REPLAY:
                return startIntent(videoAction.replay());
            case CHOOSE_NEXT:
                return startIntent(videoAction.next());
            case CHOOSE_WHICH:
                String num = getPlayNumberBySlot(slots);
                if (TextUtils.isEmpty(num)) return false;
                return startIntent(videoAction.choosePlay(num));
            case CHOOSE_LAST:
                return startIntent(videoAction.choosePlay("END"));
            case FAST_FORWARD:
                String forward = getDurationBySlot(slots);
                return startIntent(videoAction.fastForward(forward));
            case REWIND:
                String backword = getDurationBySlot(slots);
                return startIntent(videoAction.fastBackword(backword));
            case PLAYTIME_SET:
                String playTime = getDurationBySlot(slots);
                return startIntent(videoAction.seekTo(playTime));
            case BRIGHT_UP:
                return startIntent(videoAction.changeBright("UP"));
            case BRIGHT_DOWN:
                return startIntent(videoAction.changeBright("DOWN"));
            case BRIGHT_MAX:
                return startIntent(videoAction.changeBright("225"));
            case BRIGHT_MIN:
                return startIntent(videoAction.changeBright("0"));
            case VOLUME_PLUS:
                return startIntent(videoAction.changeVolume("UP"));
            case VOLUME_MINUS:
                return startIntent(videoAction.changeVolume("DOWN"));
            case VOLUME_MAX:
                return startIntent(videoAction.changeVolume("500"));
            case MUTE:
            case VOLUME_MIN:
                return startIntent(videoAction.changeVolume("0"));
            case UNMUTE:
                return startIntent(videoAction.changeVolume("250"));
            case SKIP_SET:
                int skipValue = getSkipValueBySlot(slots);
                if (skipValue > 0) {
                    return startIntent(videoAction.skipHead());
                } else if (skipValue < 0) {
                    return startIntent(videoAction.skipTile());
                } else {
                    return false;
                }
            default:
                // TODO operation action
                break;
        }
        return false;
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

    private boolean startIntent(Intent intent) {
        if (intent == null) return false;
        mContext.startActivity(intent);
        return true;
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
