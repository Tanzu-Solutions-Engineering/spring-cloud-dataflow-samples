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

package io.spring.cloud.dataflow.ingest.processor;

import com.google.common.base.Strings;
import io.spring.cloud.dataflow.ingest.config.BatchProperty;
import io.spring.cloud.dataflow.ingest.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Processes the providing record, transforming the data into
 * uppercase characters.
 *
 * @author Chris Schaefer
 */

public class PersonItemProcessor implements ItemProcessor<Person, Person> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonItemProcessor.class);


	@Value("#{jobParameters['action']}")
	private String stringAction = "";


	@Autowired
	private BatchProperty batchProperty;

	public void setStringAction(String action)
	{
		this.stringAction = action;
	}

	@Override
	public Person process(Person person) throws Exception {

		String firstName = person.getFirstName();
		String lastName = person.getLastName();

		if(Strings.isNullOrEmpty(this.stringAction))
		{
			stringAction = batchProperty.getAction();
		}

		LOGGER.info("Action is: " + this.stringAction);

		BatchProperty.Action action = BatchProperty.Action.valueOf(stringAction);
		switch (action)
		{

			case NONE:
				  break;
			case UPPERCASE:
				firstName = firstName.toUpperCase();
				lastName = lastName.toUpperCase();
						break;

			case BACKWARDS:
				firstName =  new StringBuilder(firstName).reverse().toString();
				lastName = new StringBuilder(lastName).reverse().toString();
						break;
		}

		Person processedPerson = new Person(person.getId(), firstName, lastName);

		LOGGER.info("Processed: " + person + " into: " + processedPerson);

		return processedPerson;
	}
}
