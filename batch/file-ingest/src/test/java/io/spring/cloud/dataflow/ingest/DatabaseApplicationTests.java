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

package io.spring.cloud.dataflow.ingest;

import io.spring.cloud.dataflow.ingest.config.BatchConfiguration;
import io.spring.cloud.dataflow.ingest.config.BatchProperty;
import io.spring.cloud.dataflow.ingest.config.DatabaseConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * BatchConfiguration test cases
 *
 * @author Chris Schaefer
 * @author David Turanski
 */
@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { DatabaseConfiguration.class })
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DatabaseApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Before
	public void clearMetadata() {
		jobRepositoryTestUtils.removeJobExecutions();

		jdbcTemplate.execute("INSERT INTO people (first_name, last_name) VALUES " +
				"('John', 'Doe')," +
				"('John', 'Doe')," +
				"('John', 'Doe')," +
				"('John', 'Doe')," +
				"('John', 'Doe')");

	}

	@After
	public void cleanUp()
	{
		jdbcTemplate.execute("DELETE FROM PEOPLE");
	}

	@Test
	public void testDatabaseTransform() throws Exception {


		JobExecution jobExecution = jobLauncherTestUtils.launchJob(
				new JobParametersBuilder()
						.addString("source", "DATABASE")
						.addString("action", "NONE")
						.toJobParameters());

		CheckJobResult(jobExecution, BatchProperty.Action.NONE);
	}

	@Test
	public void testUppercaseDatabaseTransform() throws Exception {

		JobExecution jobExecution2 = jobLauncherTestUtils.launchJob(
				new JobParametersBuilder()
						.addString("source", "DATABASE")
						.addString("action", "UPPERCASE")
						.toJobParameters());
		CheckJobResult(jobExecution2, BatchProperty.Action.UPPERCASE);
	}

	@Test
	public void testBackwardsTransform() throws Exception {

		JobExecution jobExecution3 = jobLauncherTestUtils.launchJob(
				new JobParametersBuilder()
						.addString("source", "DATABASE")
						.addString("action","BACKWARDS")
						.toJobParameters());
		CheckJobResult(jobExecution3, BatchProperty.Action.BACKWARDS);

	}


	private void CheckJobResult(JobExecution jobExecution, BatchProperty.Action action) {
		assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());

		assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());

		List<Map<String, Object>> peopleList = jdbcTemplate.queryForList(
			"select first_name, last_name from people");

		assertEquals("Incorrect number of results", 5, peopleList.size());

		for (Map<String, Object> person : peopleList) {
			assertNotNull("Received null person", person);

			String firstName = (String) person.get("first_name");
			String lastName = (String) person.get("last_name");
			String expectedFirsName = "John";
			String expectedLastName = "Doe";
			switch (action)
			{
				case NONE:
						break;

				case UPPERCASE:
					expectedFirsName = expectedFirsName.toUpperCase();
					expectedLastName = expectedLastName.toUpperCase();
						break;

				case BACKWARDS:
					expectedFirsName = new StringBuilder(expectedFirsName).reverse().toString();
					expectedLastName = new StringBuilder(expectedLastName).reverse().toString();
						break;
			}
			assertEquals("Invalid first name: " + firstName, expectedFirsName, firstName);

			assertEquals("Invalid last name: " + lastName, expectedLastName, lastName);
		}

	}

}
