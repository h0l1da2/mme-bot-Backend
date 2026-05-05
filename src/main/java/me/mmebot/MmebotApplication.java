package me.mmebot;

import me.mmebot.chat.config.ChatPersistenceQueueProperties;
import me.mmebot.common.config.ExternalServiceProperties;
import me.mmebot.common.config.JwtProperties;
import me.mmebot.common.logging.MaskingProperties;
import me.mmebot.common.mail.GoogleProperties;
import me.mmebot.common.persistence.ApiProp;
import me.mmebot.core.config.EncryptionKeyProperties;
import me.mmebot.openai.config.OpenAIProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        ApiProp.class,
        ExternalServiceProperties.class,
        JwtProperties.class,
        EncryptionKeyProperties.class,
        GoogleProperties.class,
        ChatPersistenceQueueProperties.class,
        MaskingProperties.class
        , OpenAIProperties.class
})
public class MmebotApplication {

	static void main(String[] args) {
		SpringApplication.run(MmebotApplication.class, args);
	}

}
