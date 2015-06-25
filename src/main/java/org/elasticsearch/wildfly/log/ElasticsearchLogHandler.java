package org.elasticsearch.wildfly.log;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class ElasticsearchLogHandler extends Handler {
    private Settings settings;
    private String clusterName;
    private String transportAddress;
    private Integer port;
    private String index;
    private String indexType;

    public ElasticsearchLogHandler() {
        super();
    }

    void processRecord(final LogRecord record) {
        synchronized(this){
            TransportClient client = null;
            try {
                checkSettings();
                client = new TransportClient(this.settings);
                client.addTransportAddress(new InetSocketTransportAddress(
                        this.transportAddress, this.port));
                client.prepareIndex(this.index, this.indexType).setSource(serializeToJson(record)).execute();
            }catch (final Exception e) {
                e.printStackTrace();
            }finally{
                if(client != null){
                    client.close();
                }
            }
        }
    }

    private void checkSettings() {
        if(this.settings == null){
            if(this.clusterName == null){
                throw new IllegalStateException("Cluster name cannot be null.");
            }

            if(this.transportAddress == null){
                throw new IllegalStateException("Transport address cannot be null.");
            }

            if(this.port == null){
                throw new IllegalStateException("Port cannot be null.");
            }

            if(index == null){
                throw new IllegalStateException("Index cannot be null.");
            }

            if(indexType == null){
                throw new IllegalStateException("Index type cannot be null.");
            }

            this.settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name", this.clusterName).put("name", "Wildfly Log Node").build();
        }
    }

    private XContentBuilder serializeToJson(final LogRecord record) throws IOException {
        final XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("timestamp", new Date(record.getMillis()))
                .field("level", record.getLevel().getName())
                .field("loggerName", record.getLoggerName())
                .field("message", record.getMessage());

        final String name = record.getThrown() == null ? "null" : record.getThrown().getClass().getName();
        final String message = record.getThrown() == null ? "null" : record.getThrown().getMessage();

        builder.startObject("thrown")
                    .field("name", name)
                    .field("message", message)
                .endObject();
        builder.endObject();

        return builder;
    }

    @Override
    public void publish(final LogRecord record) {
        processRecord(record);
    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() throws SecurityException {
        // TODO Auto-generated method stub
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public void setTransportAddress(final String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final String port) {
        try {
            this.port = Integer.parseInt(port);
        } catch (final NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(final String indexType) {
        this.indexType = indexType;
    }
}
