package com.yunbian27;

import org.springframework.ai.model.openai.autoconfigure.*;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 */
@SpringBootApplication(exclude = {
        OpenAiAudioSpeechAutoConfiguration.class,
        OpenAiAudioTranscriptionAutoConfiguration.class,
        OpenAiChatAutoConfiguration.class,
        OpenAiEmbeddingAutoConfiguration.class,
        OpenAiImageAutoConfiguration.class,
        OpenAiModerationAutoConfiguration.class,
        //TODO 完善ai模块后去除
        PgVectorStoreAutoConfiguration.class
})
public class ManmanjiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManmanjiApplication.class, args);
    }
}
