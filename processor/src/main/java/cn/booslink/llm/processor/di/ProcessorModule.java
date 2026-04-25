package cn.booslink.llm.processor.di;

import javax.inject.Named;
import javax.inject.Singleton;

import cn.booslink.llm.processor.EventProcessorImpl;
import cn.booslink.llm.processor.IEventProcessor;
import cn.booslink.llm.processor.process.IIntentProcess;
import cn.booslink.llm.processor.process.IntentProcessProxy;
import cn.booslink.llm.processor.process.app.AppProcessImpl;
import cn.booslink.llm.processor.process.app.IAppProcess;
import cn.booslink.llm.processor.process.control.ControlProcessImpl;
import cn.booslink.llm.processor.process.control.IControlProcess;
import cn.booslink.llm.processor.process.ksong.BslQmKSongAction;
import cn.booslink.llm.processor.process.ksong.DuoChangKSongAction;
import cn.booslink.llm.processor.process.ksong.IKSongAction;
import cn.booslink.llm.processor.process.ksong.IKSongProcess;
import cn.booslink.llm.processor.process.ksong.KSongProcessImpl;
import cn.booslink.llm.processor.process.ksong.QuanMinKSongAction;
import cn.booslink.llm.processor.process.ksong.SmartKSongAction;
import cn.booslink.llm.processor.process.music.IMusicProcess;
import cn.booslink.llm.processor.process.music.NetEaseMusicProcessImpl;
import cn.booslink.llm.processor.process.video.IQiYiVideoAction;
import cn.booslink.llm.processor.process.video.IVideoAction;
import cn.booslink.llm.processor.process.video.IVideoProcess;
import cn.booslink.llm.processor.process.video.ManGoTVVideoAction;
import cn.booslink.llm.processor.process.video.TencentVideoAction;
import cn.booslink.llm.processor.process.video.VideoProcessImpl;
import cn.booslink.llm.processor.process.video.YouKuVideoAction;
import cn.booslink.llm.processor.process.volume.IVolumeProcess;
import cn.booslink.llm.processor.process.volume.VolumeProcessImpl;
import cn.booslink.llm.processor.repository.AppRepositoryImpl;
import cn.booslink.llm.processor.repository.IAppRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public interface ProcessorModule {

    @Binds
    @Singleton
    IEventProcessor bindEventProcessor(EventProcessorImpl eventProcessorImpl);

    @Binds
    IIntentProcess bindIntentProcess(IntentProcessProxy intentProcessProxy);

    @Binds
    IControlProcess bindControlProcess(ControlProcessImpl controlProcessImpl);

    @Binds
    IVolumeProcess bindVolumeProcess(VolumeProcessImpl volumeProcessImpl);

    @Binds
    IAppProcess bindAppProcess(AppProcessImpl appProcessImpl);

    @Binds
    IAppRepository bindAppRepository(AppRepositoryImpl appRepositoryImpl);

    @Binds
    IMusicProcess bindMusicProcess(NetEaseMusicProcessImpl netEaseMusicProcessImpl);

    @Binds
    IVideoProcess bindVideoProcess(VideoProcessImpl videoProcessImpl);

    @Binds
    IKSongProcess bindKSongProcess(KSongProcessImpl kSongProcessImpl);

    @Binds
    @Named("quanmin")
    IKSongAction bindQuanMinAction(QuanMinKSongAction quanMinKSongAction);

    @Binds
    @Named("duochang")
    IKSongAction bindDuoChangeAction(DuoChangKSongAction duoChangeKSongAction);

    @Binds
    @Named("smart")
    IKSongAction bindSmartAction(SmartKSongAction smartKSongAction);

    @Binds
    @Named("bslqm")
    IKSongAction bindBslQmAction(BslQmKSongAction bslQmKSongAction);

    @Binds
    @Named("iqiyi")
    IVideoAction bindIQiYiAction(IQiYiVideoAction iqiyiVideoAction);

    @Binds
    @Named("youku")
    IVideoAction bindYouKuAction(YouKuVideoAction youkuVideoAction);

    @Binds
    @Named("mango")
    IVideoAction bindManGoAction(ManGoTVVideoAction manGoVideoAction);

    @Binds
    @Named("tencent")
    IVideoAction bindTencentAction(TencentVideoAction tencentVideoAction);
}
