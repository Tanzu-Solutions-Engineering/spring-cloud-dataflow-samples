package io.spring.cloud.dataflow.batch.ingest.config;

import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties("file-ingest")
public class BatchProperty {


    /**
     * FilePath to process. classpath:data.csv by default
     */
     String filePath = "classpath:data.csv";

    /**
     * Action to perform. NONE by default
     */
    PersonItemProcessor.Action action = PersonItemProcessor.Action.NONE;


    public String getFilepath() {
        Assert.hasText(filePath, "format must not be empty nor null");
        return filePath;
    }

    public void setFilepath(String filePath) {
        this.filePath = filePath;
    }


    public String getAction() {
        return action.name();
    }

    public void setAction(String action) {
        this.action = PersonItemProcessor.Action.valueOf(action);
    }

}
