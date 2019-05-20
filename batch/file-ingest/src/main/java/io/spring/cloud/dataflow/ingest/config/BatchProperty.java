package io.spring.cloud.dataflow.ingest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties("file-ingest")
public class BatchProperty {


    public enum Action
    {
        NONE,
        UPPERCASE,
        BACKWARDS

    }

    public enum Source
    {
        FILE,
        DATABASE
    }


    /**
     * Source of data. FILE by default. filePath is ignored if source is DATABASE.
     */
    Source source = Source.FILE;

    /**
     * FilePath to process. classpath:data.csv by default
     */
     String filePath = "classpath:data.csv";

    /**
     * Action to perform. NONE by default
     */
     Action action = Action.NONE;


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
        this.action = Action.valueOf(action);
    }

    public String getSource() {
        return source.name();
    }

    public void setSource(String action) {
        this.source = Source.valueOf(action);
    }
}
