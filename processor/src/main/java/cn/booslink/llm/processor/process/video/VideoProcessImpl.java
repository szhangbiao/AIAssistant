package cn.booslink.llm.processor.process.video;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.app.IAppProcess;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class VideoProcessImpl implements IVideoProcess {

    private static final String TAG = "VideoProcess";

    private static final String SLOT_CATEGORY = "category";
    private static final String SLOT_NAME = "name";
    private static final String SLOT_ACTOR = "actor";
    private static final String KEY_PAGE = "page";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_SKIP_TYPE = "skipType";
    private static final String KEY_SECOND = "second";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_CLARITY = "clarity";

    private static final String PAGE_FAVORITE = "收藏";
    private static final String PAGE_LOGIN = "登录";
    private static final String PAGE_VIP_BUY = "收银台";
    private static final String PAGE_PLAY_HISTORY = "播放历史";
    private static final String PAGE_HISTORY = "历史";
    private static final String PAGE_SEARCH = "搜索";
    private static final String PAGE_RANKING = "榜单";

    private static final String SKIP_HEAD = "片头";
    private static final String SKIP_TAIL = "片尾";

    private static final String IQIYI_APP_NAME = "爱奇艺";
    private final String IQIYI_PACKAGE_NAME = "com.qiyi.video.iv";

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
        return category == Category.VIDEO || intent == AIUIIntent.RANDOM_VIDEO || (isAppOpened && category == Category.CONTROL && (
                intent == AIUIIntent.EXIT || // 退出应用
                        intent == AIUIIntent.RESUME_PLAY || // 播放
                        intent == AIUIIntent.PAUSE || // 暂停
                        intent == AIUIIntent.REPLAY || // 重新播放
                        intent == AIUIIntent.CHOOSE_NEXT || // 下一集
                        intent == AIUIIntent.CHOOSE_WHICH || intent == AIUIIntent.CHOOSE_LAST || // 选集播放
                        intent == AIUIIntent.FAST_FORWARD || // 快进
                        intent == AIUIIntent.REWIND || // 快退
                        intent == AIUIIntent.PLAYTIME_SET || // 跳转到指定时间
                        intent == AIUIIntent.BRIGHT_UP || intent == AIUIIntent.BRIGHT_DOWN || intent == AIUIIntent.BRIGHT_MAX || intent == AIUIIntent.BRIGHT_SET || intent == AIUIIntent.BRIGHT_MIN ||// 修改亮度
                        intent == AIUIIntent.VOLUME_PLUS || intent == AIUIIntent.VOLUME_MINUS || intent == AIUIIntent.VOLUME_MAX || intent == AIUIIntent.VOLUME_MIN || intent == AIUIIntent.VOLUME_SET || intent == AIUIIntent.UNMUTE || intent == AIUIIntent.MUTE || // 修改音量
                        intent == AIUIIntent.SKIP_SET // 跳过片头， 跳过片尾
        )) || (isAppOpened && category == Category.PAGE_CONTROL && (
                intent == AIUIIntent.PAGE_OPEN || // 打开登录、收银台、播放历史、收藏、榜单、搜索
                        intent == AIUIIntent.OPEN_RANK || // 打开xx频道榜单页面
                        intent == AIUIIntent.OPEN_CHANNEL || // 打开首页XX频道
                        intent == AIUIIntent.PAGE_BACK // 返回到上一级页面
        )) || (isAppOpened && category == Category.VIDEO_ENHANCE && (
                intent == AIUIIntent.SPEED_DOWN || intent == AIUIIntent.SPEED_UP || intent == AIUIIntent.CHANGE_SPEED || // 切换倍速
                        intent == AIUIIntent.CLARITY_DOWN || intent == AIUIIntent.CLARITY_UP || intent == AIUIIntent.CHANGE_CLARITY || //  切换清晰度
                        intent == AIUIIntent.FAVORITE_REMOVE || intent == AIUIIntent.FAVORITE_ADD || // 收藏/取消收藏
                        intent == AIUIIntent.CLOSE_DANMU || intent == AIUIIntent.OPEN_DANMU // 开启/关闭弹幕
        )) || (isAppOpened && category == Category.APP_PLUS && intent == AIUIIntent.EXIT_APP)
                || (isAppOpened && category == Category.APP && intent == AIUIIntent.EXIT);
    }

    @Override
    public VoiceResult handleVideoIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        boolean isVideoStartup = IQIYI_PACKAGE_NAME.equals(foregroundPkgName);
        if ((intent == AIUIIntent.QUERY || intent == AIUIIntent.RECOMMEND || intent == AIUIIntent.RANDOM_VIDEO) && !isVideoStartup) {
            return populateActionBySlots(intent, slots);
        }
        Intent actionIntent = populateByVideoAction(foregroundPkgName, intent, slots);
        if (actionIntent != null) {
            startIntent(actionIntent);
            return VoiceResult.Companion.success("好的");
        }
        if (AIUIIntent.VOLUME_SET == intent) {
            return VoiceResult.Companion.success("请设置在1到500之间有效的音量值");
        }
        if(AIUIIntent.BRIGHT_SET == intent) {
            return VoiceResult.Companion.success("请设置在0到255之间有效的亮度值");
        }
        return VoiceResult.Companion.failure();
    }

    private VoiceResult populateActionBySlots(AIUIIntent aiuiIntent, @NotNull List<Slot> slots) {
        if (slots.isEmpty()) return VoiceResult.Companion.failure();
        String name = null;
        String category = null;
        String actor = null;
        for (Slot slot : slots) {
            if (SLOT_CATEGORY.equals(slot.getName())) {
                category = slot.getValue();
            } else if (SLOT_NAME.equals(slot.getName())) {
                name = slot.getValue();
            } else if (SLOT_ACTOR.equals(slot.getName())) {
                actor = slot.getValue();
            }
        }
        switch (aiuiIntent) {
            case QUERY:
            case RECOMMEND:
            case RANDOM_VIDEO:
                if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(actor)) {
                    Intent intent = mIQiYiVideoAction.openSearch(!TextUtils.isEmpty(name) ? name : actor);
                    mAppProcess.launchAppWithIntent(IQIYI_PACKAGE_NAME, intent);
                    return VoiceResult.Companion.progress();
                } else if (!TextUtils.isEmpty(category)) {
                    Intent homeChannel = mIQiYiVideoAction.openHomeChannel(category);
                    if (homeChannel != null) {
                        mAppProcess.launchAppWithIntent(IQIYI_PACKAGE_NAME, homeChannel);
                        return VoiceResult.Companion.progress();
                    } else {
                        return VoiceResult.Companion.failure();
                    }
                }
                break;
        }
        return VoiceResult.Companion.failure();
    }

    private Intent populateByVideoAction(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        IVideoAction videoAction = getVideoActionByPkgName(foregroundPkgName);
        switch (intent) {
            case EXIT:
            case EXIT_APP:
                boolean isMatchApp = parseSlotName(slots);
                if (isMatchApp) {
                    Intent exitIntent = videoAction.exitApp();
                    if (isEmptyIntent(exitIntent)) {
                        simulateHomePress();
                    }
                    return exitIntent;
                }
                break;
            case QUERY:
            case RECOMMEND:
            case RANDOM_VIDEO:
                return populateActionInApp(slots);
            case PAGE_BACK:
                return videoAction.pageBack();
            case PAGE_OPEN:
                return getPageIntentBySlot(videoAction, slots);
            case OPEN_RANK:
                String rankChannel = getChannelBySlot(slots);
                return videoAction.openRanking(rankChannel);
            case OPEN_CHANNEL:
                String homeChannel = getChannelBySlot(slots);
                return videoAction.openHomeChannel(homeChannel);
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
                return videoAction.changeBright("255");
            case BRIGHT_SET:
                int bright = !slots.isEmpty() ? parseSlotValue(slots) : 1;
                if (bright < 0 || bright > 255) return null;
                return videoAction.changeBright(String.valueOf(bright));
            case BRIGHT_MIN:
                return videoAction.changeBright("0");
            case VOLUME_PLUS:
                return videoAction.changeVolume("UP");
            case VOLUME_SET:
                int volume = !slots.isEmpty() ? parseSlotValue(slots) : 1;
                if (volume >= 0 && volume <= 500) {
                    return videoAction.changeVolume(String.valueOf(volume));
                }
                return null;
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
                float speed = getSpeedBySlot(slots);
                if (speed <= 0) return null;
                int intSpeed = (int) (speed * 100);
                return videoAction.changeSpeed(String.valueOf(intSpeed));
            case CLARITY_DOWN:
                return videoAction.changeRate("DOWN");
            case CLARITY_UP:
                return videoAction.changeRate("UP");
            case CHANGE_CLARITY:
                int clarity = getClarityBySlot(slots);
                if (clarity <= 0) return null;
                return videoAction.changeRate(clarity == 4 ? "4k" : String.valueOf(clarity));
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

    private boolean parseSlotName(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return true;
        String name = null;
        for (Slot slot : slots) {
            if (SLOT_NAME.equals(slot.getName())) {
                name = slot.getValue();
                break;
            }
        }
        if (TextUtils.isEmpty(name)) return true;
        return IQIYI_APP_NAME.equalsIgnoreCase(name) || IQIYI_APP_NAME.contains(name);
    }

    private float getSpeedBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return 0;
        for (Slot slot : slots) {
            if (KEY_SPEED.equals(slot.getName())) {
                String speedStr = slot.getNormValue();
                return adjustSpeed(speedStr);
            }
        }
        return -1;
    }

    private int getClarityBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return 0;
        for (Slot slot : slots) {
            if (KEY_CLARITY.equals(slot.getName())) {
                String clarity = slot.getNormValue();
                return adjustClarity(clarity);
            }
        }
        return -1;
    }

    private Intent populateActionInApp(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return null;
        String name = null;
        String category = null;
        String actor = null;
        for (Slot slot : slots) {
            if (SLOT_NAME.equals(slot.getName())) {
                name = slot.getValue();
            } else if (SLOT_CATEGORY.equals(slot.getName())) {
                category = slot.getValue();
            } else if (SLOT_ACTOR.equals(slot.getName())) {
                actor = slot.getValue();
            }
        }
        if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(actor)) {
            return mIQiYiVideoAction.search(!TextUtils.isEmpty(name) ? name : actor);
        } else if (!TextUtils.isEmpty(category)) {
            return mIQiYiVideoAction.openHomeChannel(category);
        }
        return null;
    }

    private Intent getPageIntentBySlot(IVideoAction videoAction, @NotNull List<Slot> slots) {
        for (Slot slot : slots) {
            if (KEY_PAGE.equals(slot.getName())) {
                if (TextUtils.isEmpty(slot.getValue())) return null;
                switch (slot.getValue()) {
                    case PAGE_FAVORITE:
                        return videoAction.openFavorite();
                    case PAGE_LOGIN:
                        return videoAction.openLogin();
                    case PAGE_VIP_BUY:
                        return videoAction.openBuyVip();
                    case PAGE_HISTORY:
                    case PAGE_PLAY_HISTORY:
                        return videoAction.openHistory();
                    case PAGE_SEARCH:
                        return videoAction.openSearch("");
                    case PAGE_RANKING:
                        return videoAction.openRanking("总榜");
                }
            }
        }
        return null;
    }

    private String getChannelBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return "总榜";
        for (Slot slot : slots) {
            if (KEY_CHANNEL.equals(slot.getName())) {
                return slot.getValue();
            }
        }
        return "总榜";
    }

    private int getSkipValueBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return 0;
        for (Slot slot : slots) {
            if (KEY_SKIP_TYPE.equals(slot.getName())) {
                if (SKIP_HEAD.equals(slot.getValue())) {
                    return 1;
                } else if (SKIP_TAIL.equals(slot.getValue())) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private void startIntent(Intent intent) {
        if (intent == null || isEmptyIntent(intent)) return;
        mContext.startActivity(intent);
    }

    private IVideoAction getVideoActionByPkgName(String foregroundPackage) {
        // TODO 根据packageName获取VideoAction
        return mIQiYiVideoAction;
    }

    private String getPlayNumberBySlot(@NotNull List<Slot> slots) {
        for (Slot slot : slots) {
            if (KEY_NUMBER.equals(slot.getName())) {
                return slot.getNormValue();
            }
        }
        return null;
    }

    private String getDurationBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return "10000"; // 10s
        int duration = 0;
        for (Slot slot : slots) {
            if (KEY_SECOND.equals(slot.getName())) {
                int seconds = tryParseIntNum(slot.getNormValue());
                duration += seconds;
            } else if (KEY_MINUTE.equals(slot.getName())) {
                int minutes = tryParseIntNum(slot.getNormValue());
                duration += minutes * 60;
            } else if (KEY_HOUR.equals(slot.getName())) {
                int hours = tryParseIntNum(slot.getNormValue());
                duration += hours * 3600;
            }
        }
        return duration != 0 ? String.valueOf(duration * 1000) : "10000";
    }

    private float adjustSpeed(String speedStr) {
        float[] speeds = new float[]{0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f};
        float speed = tryParseFloatNum(speedStr);
        if (speed <= 0) return 1.0f;
        float closestSpeed = speeds[0];
        float minDiff = Math.abs(speed - speeds[0]);
        for (int i = 1; i < speeds.length; i++) {
            float diff = Math.abs(speed - speeds[i]);
            if (diff < minDiff) {
                minDiff = diff;
                closestSpeed = speeds[i];
            }
        }
        return closestSpeed;
    }

    private int parseSlotValue(List<Slot> slots) {
        for (Slot slot : slots) {
            String value = slot.getValue();
            if (!TextUtils.isEmpty(value)) {
                return tryParseIntNum(value);
            }
        }
        return 1;
    }

    private int adjustClarity(String clarity) {
        int[] clarities = new int[]{480, 720, 1080, 4};
        int num = tryParseIntNum(clarity);
        for (int c : clarities) {
            if (num == c) return c;
        }
        return 0;
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

    private float tryParseFloatNum(String value) {
        float num;
        try {
            num = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            num = 0;
        }
        return num;
    }

    private void simulateHomePress() {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
        } catch (Exception e) {
            // 记录错误日志
            Timber.tag(TAG).e(e, "Failed to simulate home press");
        }
    }

    private boolean isEmptyIntent(Intent intent) {
        if (intent == null) return true;
        // 如果没有action、component或package，基本上就是空Intent
        return TextUtils.isEmpty(intent.getAction()) &&
                intent.getComponent() == null &&
                TextUtils.isEmpty(intent.getPackage());
    }
}
