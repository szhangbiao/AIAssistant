package cn.booslink.llm.speech.di;

import javax.inject.Singleton;

import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.speech.SpeechAgentImpl;
import cn.booslink.llm.speech.repository.ConfigRepositoryImpl;
import cn.booslink.llm.speech.repository.IConfigRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public interface SpeechBinds {
    @Binds
    @Singleton
    ISpeechAgent bindSpeechAgent(SpeechAgentImpl speechAgentImpl);

    @Binds
    IConfigRepository bindConfigRepository(ConfigRepositoryImpl configRepositoryImpl);
}
