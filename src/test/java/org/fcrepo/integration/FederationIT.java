package org.fcrepo.integration;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/repo.xml", "/spring/eventing.xml", "/spring/jms.xml", "/spring/generator.xml", "/spring-test/rest.xml"})
public class FederationIT {

    protected static Logger logger = LoggerFactory.getLogger(FederationIT.class);

    protected static final int SERVER_PORT = Integer.parseInt(System
            .getProperty("test.port", "8080"));

    protected static final String HOSTNAME = "localhost";

    protected static final String serverAddress = "http://" + HOSTNAME + ":" +
            SERVER_PORT + "/rest/";

    protected static final PoolingClientConnectionManager connectionManager =
            new PoolingClientConnectionManager();

    protected static HttpClient client;

    static {
        connectionManager.setMaxTotal(Integer.MAX_VALUE);
        connectionManager.setDefaultMaxPerRoute(5);
        connectionManager.closeIdleConnections(3, TimeUnit.SECONDS);
        client = new DefaultHttpClient(connectionManager);
    }
    
    @Before
    public void setUp() throws ClientProtocolException, IOException {
    	int status = 0;
    	HttpGet get = new HttpGet(serverAddress + "objects/test:object");
    	try {
    		status = getStatus(get);
    	} catch (Throwable t) {
    		logger.warn(t.getMessage(), t);
    		status = -1; // because we don't actually return 404 for unknown PIDs
    	}
    	if ( status == 404 || status == 500) {
    		logger.info("PUT to test:object because status of GET was " + status);
    		HttpPost post = new HttpPost(serverAddress + "objects/test:object");
    		try {
    			client.execute(post);
    		} catch (Throwable t) {
    			logger.error(t.getMessage(), t);
    		}
    	} else {
    		logger.debug("Not creating test:object because status of GET was " + status);
    	}
    }

    @Test
    public void testBagItFederation() throws IOException {
    	// When I POST to /objects/pid/federate?from=BagItFed1
    	// *do a POST*
    	// then I should be able to GET /objects/pid/datastreams/testds
    	HttpEntity.class.getCanonicalName();
    	HttpPost post = new HttpPost(serverAddress + "objects/test:object/federate?from=BagItFed1");
    	HttpResponse response = client.execute(post);
    	int status = response.getStatusLine().getStatusCode();
        assertEquals(204, status);
        HttpGet get = new HttpGet(serverAddress + "objects/test:object/datastreams/testDS");
        assertEquals(200, getStatus(get));
        //HttpPut put = new HttpPut(serverAddress + "objects/test:object/datastreams/newds");
        //// just need some test bytes
    	//put.setEntity(new InputStreamEntity(FederationIT.class.getResourceAsStream("/spring-test/rest.xml"), 2832, ContentType.TEXT_XML));
    	//assertEquals(204, getStatus(put));
    	//File check = new File("target/test-classes/test-bagit/BagItFed1/data/newds");
    	//assertTrue(check.exists());
    }

    @Test
    public void testFileSystemFederation() throws IOException {
    	// When I POST to /objects/test:object/federate?from=FileSystem1
    	// *do a POST*
    	// then I should be able to GET /objects/test:object/datastreams/testds
    	//HttpPost post = new HttpPost(serverAddress + "objects/test:object/federate?from=FileSystem1");
    	//HttpResponse response = client.execute(post);
    	//int status = response.getStatusLine().getStatusCode();
        //assertEquals(204, status);
        //HttpGet get = new HttpGet(serverAddress + "objects/test:object/datastreams/testds");
        //assertEquals(200, getStatus(get));
        //HttpPut put = new HttpPut(serverAddress + "objects/test:object/datastreams/newds");
        //// just need some test bytes
    	//put.setEntity(new InputStreamEntity(FederationIT.class.getResourceAsStream("/spring-test/rest.xml"), 2832, ContentType.TEXT_XML));
    	//assertEquals(204, getStatus(put));
    	//File check = new File("target/test-classes/test-objects/FileSystem1/newds");
    	//assertTrue(check.exists());
    }


    protected int getStatus(HttpUriRequest method)
            throws ClientProtocolException, IOException {
        logger.debug("Executing: " + method.getMethod() + " to " +
                method.getURI());
        return client.execute(method).getStatusLine().getStatusCode();
    }
}
