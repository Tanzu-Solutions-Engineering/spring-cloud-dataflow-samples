package io.spring.cloud.dataflow.batch.dbtransform.config;

import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("db-transform")
public class BatchProperty {

    /**
     * Action to perform on the names. Values can be NONE, UPPERCASE,LOWERCASE, BACKWARDS
     *  NONE is the default if no action is specified.
     */
    PersonItemProcessor.Action action = PersonItemProcessor.Action.NONE;


    public String getAction() {
        return action.name();
    }

    public void setAction(String action) {
        this.action = PersonItemProcessor.Action.valueOf(action);
    }


}
