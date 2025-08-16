package com.example.demo.infrastructure.kafka.config

import com.example.demo.infrastructure.kafka.provider.KafkaConsumerFactoryProvider
import com.example.demo.infrastructure.mail.MailPayload
import com.example.demo.user.batch.mapper.UserDeleteItem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory

@Configuration
class KafkaListenerConfig(
	private val factoryProvider: KafkaConsumerFactoryProvider
) {
	@Bean
	fun mailKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, MailPayload> = factoryProvider.createFactory(MailPayload::class.java)

	@Bean
	fun userDeleteKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserDeleteItem> =
		factoryProvider.createFactory(UserDeleteItem::class.java)
}
