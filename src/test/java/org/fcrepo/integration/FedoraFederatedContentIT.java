package org.fcrepo.integration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

public class FedoraFederatedContentIT extends AbstractResourceIT{

    @Test
    public void testFileSystemFederation() throws IOException, LoginException, RepositoryException {
    	HttpPost post = new HttpPost(serverAddress + "objects/test:file/federate?from=FileSystem1");
    	HttpResponse response = client.execute(post);
    	int status = response.getStatusLine().getStatusCode();
        assertEquals(204, status);
        HttpGet get = new HttpGet(serverAddress + "objects/test:file/datastreams/testds");
        assertEquals(200, getStatus(get));
    }
    
/*    @Test
    public void testBagitConnectorFederation() throws RepositoryException, IOException {
    	HttpPost post = new HttpPost(serverAddress + "objects/test:bagit/federate?from=BagItFed1");
    	HttpResponse response = client.execute(post);
    	int status = response.getStatusLine().getStatusCode();
        assertEquals(204, status);
        HttpGet get = new HttpGet(serverAddress + "objects/test:bagit/datastreams/testDS");
        assertEquals(200, getStatus(get));
    }*/
}
