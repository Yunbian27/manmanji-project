//package com.yunbian27.ai;
//
//
//import com.yunbian27.ai.config.LlmProviderProperties;
//import io.micrometer.observation.ObservationRegistry;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.api.Advisor;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.model.tool.ToolCallingManager;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.openai.OpenAiChatOptions;
//import org.springframework.ai.openai.api.OpenAiApi;
//import org.springframework.ai.retry.RetryUtils;
//import org.springframework.ai.tool.ToolCallback;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 动态路由选择大语言模型
// */
//@Slf4j
//@Component
//public class LlmProviderRegistry {
//
//    private final LlmProviderProperties properties;
//    // 线程安全的缓存Map：key是唯一标识，value是ChatClient客户端
//    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();
//    private final Map<String, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();
//    private final LlmProviderRepository providerRepository;
//    private final LlmGlobalSettingRepository globalSettingRepository;
//    private final ApiKeyEncryptionService encryptionService;
//
//    private final ToolCallingManager toolCallingManager;
//    private final ObservationRegistry observationRegistry;
//    private final ToolCallback interviewSkillsToolCallback;
//    private static final Map<String, String> RECOMMENDED_EMBEDDING_MODELS = Map.of(
//            "dashscope", "text-embedding-v3",
//            "glm", "embedding-3",
//            "zhipu", "embedding-3",
//            "baidu", "Embedding-V1",
//            "minimax", "embo-01"
//    );
//
//    @Autowired
//    public LlmProviderRegistry(
//            LlmProviderProperties properties,
//            LlmProviderRepository providerRepository,
//            LlmGlobalSettingRepository globalSettingRepository,
//            ApiKeyEncryptionService encryptionService,
//            @Autowired(required = false) ToolCallingManager toolCallingManager,
//            @Autowired(required = false) ObservationRegistry observationRegistry,
//            @Autowired(required = false) @Qualifier("interviewSkillsToolCallback") ToolCallback interviewSkillsToolCallback) {
//        this.properties = properties;
//        this.providerRepository = providerRepository;
//        this.globalSettingRepository = globalSettingRepository;
//        this.encryptionService = encryptionService;
//        this.toolCallingManager = toolCallingManager;
//        this.observationRegistry = observationRegistry;
//        this.interviewSkillsToolCallback = interviewSkillsToolCallback;
//    }
//
//    public ChatClient getChatClient(String providerId) {
//        return clientCache.computeIfAbsent(providerId, id -> {
//            log.info("[LlmProviderRegistry] Creating new client for provider: {}", id);
//            return createChatClient(id);
//        });
//    }
//
//    private ChatClient createChatClient(String providerId) {
//        OpenAiChatModel chatModel = buildChatModel(providerId);
//
//        ChatClient.Builder builder = ChatClient.builder(chatModel);
//        if (interviewSkillsToolCallback != null) {
//            builder.defaultToolCallbacks(interviewSkillsToolCallback);
//        }
//        List<Advisor> advisors = buildDefaultAdvisors(providerId);
//        if (!advisors.isEmpty()) {
//            builder.defaultAdvisors(advisors.toArray(new Advisor[0]));
//            log.info("[LlmProviderRegistry] Applied {} advisors for provider {}", advisors.size(), providerId);
//        }
//
//        return builder.build();
//    }
//
//    private OpenAiChatModel buildChatModel(String providerId) {
//        ProviderSnapshot config = loadProviderOrThrow(providerId);
//        log.info("[LlmProviderRegistry] Building ChatModel - Provider: {}, BaseUrl: {}, Model: {}",
//                providerId, config.baseUrl(), config.model());
//
//        OpenAiApi openAiApi = ApiPathResolver.buildOpenAiApi(config.baseUrl(), config.apiKey());
//
//        OpenAiChatOptions options = OpenAiChatOptions.builder()
//                .model(config.model())
//                .temperature(config.temperature() != null ? config.temperature() : 0.2)
//                .build();
//
//        return new OpenAiChatModel(
//                openAiApi,
//                options,
//                toolCallingManager,
//                RetryUtils.DEFAULT_RETRY_TEMPLATE,
//                observationRegistry != null ? observationRegistry : ObservationRegistry.NOOP
//        );
//    }
//}
