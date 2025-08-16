package com.example.demo.common.config

import com.example.demo.post.entity.Post
import com.example.demo.post.repository.PostRepository
import com.example.demo.user.batch.mapper.UserDeleteItem
import com.example.demo.user.batch.processor.UserDeleteItemProcessor
import com.example.demo.user.batch.reader.UserDeleteItemReader
import com.example.demo.user.batch.writer.UserDeleteItemWriter
import com.example.demo.user.entity.User
import com.example.demo.user.repository.UserRepository
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.core.KafkaTemplate

@TestConfiguration
@Import(QueryDslConfig::class)
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = [UserRepository::class, PostRepository::class])
@EntityScan(basePackageClasses = [User::class, Post::class])
@ComponentScan(basePackageClasses = [UserDeleteItemReader::class, UserDeleteItemProcessor::class, UserDeleteItemWriter::class])
class TestBatchConfig {
	@Bean
	fun userDeleteKafkaTemplate(): KafkaTemplate<String, UserDeleteItem> = mock(KafkaTemplate::class.java) as KafkaTemplate<String, UserDeleteItem>
}
