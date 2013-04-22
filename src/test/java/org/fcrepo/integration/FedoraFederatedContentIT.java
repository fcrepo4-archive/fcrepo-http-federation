package org.fcrepo.integration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;

public class FedoraFederatedContentIT extends AbstractResourceIT{
    
    private void deleteQuietly(String pid) {
        HttpDelete del = new HttpDelete(serverAddress + "objects/" + pid);
        try {
            getStatus(del);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createQuietly(String pid) {
        HttpPost post = new HttpPost(serverAddress + "objects/" + pid);
        try {
            getStatus(post);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Before
    public void setUp() {
        deleteQuietly("test:file");
        createQuietly("test:file");
        deleteQuietly("test:bag");
        createQuietly("test:bag");
    }
    
    @Test
    public void testFileSystemFederation() throws IOException, LoginException, RepositoryException {
    	HttpPost post = new HttpPost(serverAddress + "objects/test:file/federate?from=/files/FileSystem1");
    	HttpResponse response = client.execute(post);
    	int status = response.getStatusLine().getStatusCode();
        assertEquals(204, status);
        HttpGet get = new HttpGet(serverAddress + "objects/test:file/datastreams/testds");
        assertEquals(200, getStatus(get));
    }
    
    @Test
    public void testBagitConnectorFederation() throws RepositoryException, IOException {
    	HttpPost post = new HttpPost(serverAddress + "objects/test:bag/federate?from=/bags/BagItFed1");
    	HttpResponse response = client.execute(post);
    	int status = response.getStatusLine().getStatusCode();
        assertEquals(204, status);
        HttpGet get = new HttpGet(serverAddress + "objects/test:bag/datastreams/testDS");
        assertEquals(200, getStatus(get));
    }
}
