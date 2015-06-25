package org.elasticsearch.wildfly.log;

import java.util.Date;


/**
 * This is the lightweight version of {@link java.util.logging.LogRecord} entity that is stored in Elasticsearch.
 * @author Radek Koubsky
 */
public class ElasticsearchLogRecord {
    /**
     * Message text
     * */
    private final String message;

    /**
     * Date of this log record
     * */
    private final Date date;

    /**
     * Name of the source Logger.
     */
    private final String loggerName;

    /**
     * The Throwable (if any) associated with log message
     */
    private final Throwable thrown;

    public ElasticsearchLogRecord(final String message, final long millis, final String loggerName,
            final Throwable thrown) {
        this.message = message;
        this.date = new Date(millis);
        this.loggerName = loggerName;
        this.thrown = thrown;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public Throwable getThrown() {
        return thrown;
    }
}
