package org.fcrepo.integration;

import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.fcrepo.integration.AbstractResourceIT;
import org.junit.Test;
import org.modeshape.jcr.api.JcrTools;

public class FedoraFederatedContentIT extends AbstractResourceIT{

    @Test
    public void testFileSystemFederation() throws IOException, LoginException, RepositoryException {
      // When I POST to /objects/pid/federate?from=BagItFed1
    	// *do a POST*
    	// then I should be able to GET /objects/pid/datastreams/testds
    	Session session = repo.login();
    	Node node = session.getNode("/federated/FileSystem1");
    	JcrTools tools = new JcrTools();
    	//tools.printSubgraph(node);
    	HttpPost post1 = new HttpPost(serverAddress + "objects/test");
    	HttpResponse response1 = client.execute(post1);
    	HttpPost post = new HttpPost(serverAddress + "objects/test/federate?from=FileSystem1");
    	HttpResponse response = client.execute(post);
    	int status = response.getStatusLine().getStatusCode();
        assertEquals(204, status);
        HttpGet get = new HttpGet(serverAddress + "objects/test/datastreams/testds");
        assertEquals(200, getStatus(get));
  }
}
