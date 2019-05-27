package io.spring.cloud.dataflow.batch.dbtransform.config;

import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties("db-transform")
public class BatchProperty {




    /**
     * Action to perform. NONE by default
     */
    PersonItemProcessor.Action action = PersonItemProcessor.Action.NONE;


    public String getAction() {
        return action.name();
    }

    public void setAction(String action) {
        this.action = PersonItemProcessor.Action.valueOf(action);
    }


}
