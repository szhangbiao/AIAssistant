package cn.booslink.llm.speech.di;

import javax.inject.Singleton;

import cn.booslink.llm.speech.ISpeechAgent;
import cn.booslink.llm.speech.SpeechAgentImpl;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public interface SpeechBinds {
    @Binds
    @Singleton
    ISpeechAgent provideSpeechAgent(SpeechAgentImpl speechAgentImpl);
}
