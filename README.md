# elasticsearch-wildfly-log
==========================
Elasticsearch log handler for Wildfly.

*NOTE:*
Current build was tested with Wildfly 8, Elasticsearch 1.6.0 and Kibana 4.1.1.

This handler sends log records from Wildfly directly to Elasticsearch cluster.
For vizualization of the log events, use Kibana tool.

* download Wildfly 8: http://wildfly.org/
* download Elasticsearch 1.6.0: https://www.elastic.co/products/elasticsearch
* dowlonad Kibana 4.1.1:https://www.elastic.co/products/kibana

Build project with maven: `mvn clean package`

Unzip the archive created in `target/zip` folder to `$WILDFLY_HOME/modules/system/layers/base`.

Directory structure of the unzipped archive:

```
	org
	├── elasticsearch
	│   └── main
	│       ├── elasticsearch-1.6.0.jar
	│       ├── lucene-analyzers-common-4.10.4.jar
	│       ├── lucene-core-4.10.4.jar
	│       ├── lucene-expressions-4.10.4.jar
	│       ├── lucene-grouping-4.10.4.jar
	│       ├── lucene-highlighter-4.10.4.jar
	│       ├── lucene-join-4.10.4.jar
	│       ├── lucene-memory-4.10.4.jar
	│       ├── lucene-misc-4.10.4.jar
	│       ├── lucene-queries-4.10.4.jar
	│       ├── lucene-queryparser-4.10.4.jar
	│       ├── lucene-sandbox-4.10.4.jar
	│       ├── lucene-spatial-4.10.4.jar
	│       ├── lucene-suggest-4.10.4.jar
	│       └── module.xml
	└── wildfly
	    └── elasticsearch
	        └── log
	            └── main
	                ├── elasticsearch-wildfly-log-0.0.1-SNAPSHOT.jar
	                └── module.xml
```
## Configuration

Edit Wildfly configuration file `standalone/configuration/standalone.xml` or its domain version:

```
<subsystem xmlns="urn:jboss:domain:logging:2.0">
	...
	<custom-handler name="ELASTICSEARCH" class="org.elasticsearch.wildfly.log.ElasticsearchLogHandler" module="org.wildfly.elasticsearch.log">
		<level name="DEBUG"/>
		<formatter>
		    <pattern-formatter pattern="%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n"/>
		</formatter>
		<properties>
			<!-- Default configuration for localhost, all values are mandatory -->
		    <property name="clusterName" value="elasticsearch"/>  
		    <property name="transportAddress" value="localhost"/>  
		    <property name="port" value="9300" />
		    <property name="index" value="wildfly"/>  
		    <property name="indexType" value="log"/>
		</properties>  
	</custom-handler>
	...
	<!-- NOTE: To avoid recursive calls inside elasticsearch log handler, it is needed to turn off logs from
		 		org.elasticsearch package which the handler uses. -->
	<logger category="org.elasticsearch">
	    <level name="OFF"/>
	</logger>
	
	<root-logger>
            <level name="DEBUG"/>
            <handlers>
                <handler name="CONSOLE"/>
                <handler name="FILE"/>
	    		<handler name="ELASTICSEARCH"/>
            </handlers>
    </root-logger>
</subsystem>
```

## Run

Simple scenario if you are running on localhost.

1. Run Elasticsearch
2. Run Wildfly
3. Run Kibana
4. Go to localhost:5601