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

/**
 * Elasticsearch log handler sends logs directly to elasticsearch cluster.
 * @author Radek Koubsky
 * */
public class ElasticsearchLogHandler extends Handler {
    /**
     * Name of a server instance.
     * */
    private String jbossServerName;
    /**
     * Address to which is a server instance bound.
     * */
    private String jbossBindAddress;
    /**
     * Settings used to create a client instance.
     * */
    private Settings settings;
    /**
     * A name of elasticsearch cluster.
     * */
    private String clusterName;
    /**
     * An address of a cluster.
     * */
    private String transportAddress;
    /**
     * A port of an address.
     * */
    private Integer port;
    /**
     * An index to which logs are bound.
     * */
    private String index;
    /**
     * A type of an index.
     * */
    private String indexType;
    /**
     * Client which communicates with elascticsearch cluster.
     * */
    private volatile TransportClient client;

    /**
     * Ctor.
     * */
    public ElasticsearchLogHandler() {
        super();
    }

    /**
     * Processes a log record and sends it to elasticsearch cluster.
     * @param record record to be processed
     * */
    void processRecord(final LogRecord record) {
            initClient();
            try {
                this.client.prepareIndex(this.index, this.indexType).setSource(serializeToJson(record)).execute();
            } catch (final IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Initializes client.
     * */
    private void initClient() {
        if(this.client == null){
            synchronized (this) {
                if(this.client == null){
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

                    this.client = new TransportClient(this.settings)
                            .addTransportAddress(new InetSocketTransportAddress(
                                    this.transportAddress, this.port));
                    try {
                        this.jbossServerName = System.getProperty("jboss.server.name");
                        this.jbossBindAddress = System.getProperty("jboss.bind.address");
                    } catch (final SecurityException e) {
                        this.jbossServerName = "access_denied";
                        this.jbossBindAddress = "access_denied";
                    }
                }
            }
        }

    }

    /**
     * Serializes log record to JSON format using {@link XContentBuilder}.
     * @param record record to be serialized
     * @return serialized log record
     * */
    private XContentBuilder serializeToJson(final LogRecord record) throws IOException {
        final XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("timestamp", new Date(record.getMillis()))
                .field("jboss.bind.address", jbossBindAddress)
                .field("jboss.server.name", jbossServerName)
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
        if(this.client != null){
            this.client.close();
        }
    }

    /**
     * @return the clusterName
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * @param clusterName the clusterName to set
     */
    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * @return the transportAddress
     */
    public String getTransportAddress() {
        return transportAddress;
    }

    /**
     * @param transportAddress the transportAddress to set
     */
    public void setTransportAddress(final String transportAddress) {
        this.transportAddress = transportAddress;
    }

    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Parses port as an integer.
     * @param port the port to set
     */
    public void setPort(final String port) {
        try {
            this.port = Integer.parseInt(port);
        } catch (final NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(final String index) {
        this.index = index;
    }

    /**
     * @return the indexType
     */
    public String getIndexType() {
        return indexType;
    }

    /**
     * @param indexType the indexType to set
     */
    public void setIndexType(final String indexType) {
        this.indexType = indexType;
    }
}
