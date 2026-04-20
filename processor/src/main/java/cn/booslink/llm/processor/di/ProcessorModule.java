package cn.booslink.llm.processor.di;

import javax.inject.Singleton;

import cn.booslink.llm.processor.EventProcessorImpl;
import cn.booslink.llm.processor.IEventProcessor;
import cn.booslink.llm.processor.process.IIntentProcess;
import cn.booslink.llm.processor.process.IntentProcessProxy;
import cn.booslink.llm.processor.process.app.AppProcessImpl;
import cn.booslink.llm.processor.process.app.IAppProcess;
import cn.booslink.llm.processor.process.control.ControlProcessImpl;
import cn.booslink.llm.processor.process.control.IControlProcess;
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
}
