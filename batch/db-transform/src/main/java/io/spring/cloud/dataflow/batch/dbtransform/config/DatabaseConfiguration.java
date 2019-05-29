/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.cloud.dataflow.batch.dbtransform.config;


import com.google.common.base.Strings;
import io.spring.cloud.dataflow.batch.domain.Person;
import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;


/**
 * Class used to configure the batch job related beans.
 *
 * @author Chris Schaefer
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties({BatchProperty.class})
@EnableBatchProcessing
public class DatabaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private final DataSource dataSource;

    private final ResourceLoader resourceLoader;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    private BatchProperty batchProperty;

    @Autowired
    public DatabaseConfiguration(final DataSource dataSource, final JobBuilderFactory jobBuilderFactory,
                                 final StepBuilderFactory stepBuilderFactory,
                                 final ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }




    @Bean(name="dbPersonProcessor")
    @StepScope
    public ItemProcessor<Person, Person> processor() {

        PersonItemProcessor processor =  new PersonItemProcessor();
        if(batchProperty!=null && !Strings.isNullOrEmpty(batchProperty.getAction()))
        {
            processor.setStringAction(batchProperty.getAction());
        }
        return processor;
    }

    @Bean
    public ItemStreamReader<Person> databaseReader() {
        return new JdbcCursorItemReaderBuilder<Person>()
                .name("databaseReader")
                .beanRowMapper(Person.class)
                .dataSource(this.dataSource)
                .sql("SELECT person_id as id, first_name, last_name FROM people")
                .build();
    }

    @Bean
    public ItemWriter<Person> databaseUpdaterWriter() {
        return new JdbcBatchItemWriterBuilder<Person>()
                .beanMapped()
                .dataSource(this.dataSource)
                .sql("UPDATE people SET first_name = :firstName,  last_name = :lastName WHERE person_id = :id")
                .build();
    }


    @Bean
    public Job transformNamesJob() {
        return jobBuilderFactory.get("transformNamesJob")
                .incrementer(new RunIdIncrementer())
                .flow(stepDatabase())
                .end()
                .build();

    }

    @Bean
    public Step stepDatabase() {
        return stepBuilderFactory.get("transformNames")
                .<Person, Person>chunk(10)
                .reader(databaseReader())
                .processor(processor())
                .writer(databaseUpdaterWriter())
                .build();
    }
}
