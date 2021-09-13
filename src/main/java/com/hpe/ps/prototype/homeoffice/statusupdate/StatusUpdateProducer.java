package com.hpe.ps.prototype.homeoffice.statusupdate;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to send the message on the status for the process to the Status Update queue. 
 * 
 * @author pmorenas
 *
 */
public class StatusUpdateProducer {
	
	private static Logger log = LoggerFactory.getLogger(StatusUpdateProducer.class);

	// Declare a new producer.
    private Producer<String, StatusUpdateMessage> producer;
	
    private String statusUpdateTopic;

    
    public StatusUpdateProducer(Producer<String, StatusUpdateMessage> producer, String statusUpdateTopic) {
    	
    	this.producer = producer;
    	this.statusUpdateTopic = statusUpdateTopic;
    	
    }
    
    public StatusUpdateProducer(String statusUpdateTopic) {
    	
    	this.statusUpdateTopic = statusUpdateTopic;
    }
    

    public Future<RecordMetadata> send(String queryId, StatusUpdateMessage message) {
     
    	return send(queryId, message, true);
        
    }
    
    public Future<RecordMetadata> send(String queryId, StatusUpdateMessage message, boolean autoComplete) {
        
    	Future<RecordMetadata> recordMetadata = null;
    	
    	try {
    		
    		ProducerRecord<String, StatusUpdateMessage> record = new ProducerRecord<>(statusUpdateTopic, 
    				                                                                  queryId, message);
    		recordMetadata = producer.send(record);
    		
    	}
		catch (Exception e) {
			
			log.error("Error when sending the status update message: {}", e.getMessage());
			
		}
    	finally {
    		
            if (autoComplete) {
            	
            	producer.close();
            
            }
	    }
    	
    	return recordMetadata;
        
    }
    
    public void sendStatusUpdate(StatusUpdateMessage message) {
		
    	try {
			
		    configureProducer();
		    
		    send(message.getQueryId(), message);
			
			log.info("Sent message to Status Update Queue - Query ID: {}, Status: {}", message.getQueryId(), message.getStatus());
			
		}
		catch (Exception e) {
			
			log.error("Error when sending the status update message: {}", e.getMessage());
			
		}
		
	}
	
	
	private void configureProducer() {
    
		Properties props = new Properties();
		
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        		  StatusUpdateSerializer.class.getName());
        
        producer = new KafkaProducer<>(props);

	}
	
}