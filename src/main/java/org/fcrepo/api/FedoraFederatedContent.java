package org.fcrepo.api;

import static javax.ws.rs.core.Response.noContent;
import static org.fcrepo.services.PathService.getDatastreamJcrNodePath;
import static org.slf4j.LoggerFactory.getLogger;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.fcrepo.AbstractResource;
import org.fcrepo.services.DatastreamService;
import org.fcrepo.services.PathService;
import org.modeshape.jcr.JcrSession;
import org.modeshape.jcr.api.Workspace;
import org.slf4j.Logger;

@Path("/objects/{pid}/federate")
public class FedoraFederatedContent extends AbstractResource {

	private final Logger logger = getLogger(FedoraDatastreams.class);

	@Inject
	private Repository repo;

	@Inject
	DatastreamService datastreamService;

	@POST
	public Response addFederatedDatastream(@PathParam("pid") final String pid,
			@QueryParam("from") final String from) throws RepositoryException {

		Session session = getAuthenticatedSession();		
		String nodePath = federatePath(from);

		try {
			Workspace ws = (Workspace) session.getWorkspace();
			Node federatedNode = session.getNode(nodePath);
			
			NodeIterator it = federatedNode.getNodes();
			while (it.hasNext()) {
				Node node = it.nextNode();
				versionFederatedNode(node);

				String federatedSrc = node.getPath();
				String cloneTo = getDatastreamJcrNodePath(pid, node.getName());
				logger.debug("Cloning '" + federatedSrc + "' to '" + cloneTo + "'");
				
				try {
					ws.clone("fedora", federatedSrc, cloneTo, false);
					//Getting the shared node and letting modeshape know it's a fedora datastream
					Node sharedNode = session.getNode(cloneTo);
					sharedNode.addMixin("fedora:datastream");
				} catch (RepositoryException ex){
					logger.error("Could not clone node " + 
							node.getName() + " - " + 
							ex.getMessage());
				} 			
			}

			session.save();
			
		} finally {
			session.logout();
		}

		return noContent().build();
	}
	
	private VersionHistory versionFederatedNode(Node node) throws RepositoryException {
		JcrSession session = (JcrSession)node.getSession();
		Workspace workspace = session.getWorkspace();
		VersionManager versionMgr = workspace.getVersionManager();
		VersionHistory result = versionMgr.getVersionHistory(node.getPath());
		node.setProperty("jcr:isCheckedOut", false);
		session.save();
		versionMgr.checkout(node.getPath());
		node.addMixin("mix:shareable");
		session.save();
		versionMgr.checkin(node.getPath());
		return result;
	}

		
	/**
	 * Returns path to federated node
	 * @param path
	 * @return
	 */
	private static String federatePath(String path) {
		String str = path;
		if(path.indexOf("/") != 0)
			str = "/" + path;
		
		if(str.indexOf(PathService.FEDERATED_PATH) >= 0) {
			return str;
		} else {
			return PathService.FEDERATED_PATH + str;
		}
	}

}