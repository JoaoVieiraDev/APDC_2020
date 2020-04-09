package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/consult")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ConsultResource {
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	
	public ConsultResource() {}
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	@POST
	@Path("{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doConsult(@PathParam("username") String username, AuthToken auth) {
		LOG.fine("Attempt to consult user " + username);
		Transaction txn = datastore.newTransaction();
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
		Entity user = txn.get(userKey);
		Key currentUserKey = datastore.newKeyFactory().setKind("User").newKey(auth.username);
		Entity currentUser = txn.get(currentUserKey);
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(auth.username);
		Entity tokenOriginal = txn.get(tokenKey);
		
		if(user == null) {
			LOG.warning("You do not have permission to do this");
			return Response.status(Status.FORBIDDEN).build();
		}
		if(currentUser == null) {
			LOG.warning("You do not have permission to do this");
			return Response.status(Status.FORBIDDEN).build();
		}
		if(tokenOriginal == null) {
			LOG.warning("You do not have permission to do this");
			return Response.status(Status.FORBIDDEN).build();
		}
		if(auth.expirationData < System.currentTimeMillis()) {
			LOG.warning("Your session has timed out");
			return Response.status(Status.FORBIDDEN).build();
		} else if(!user.getValue("account_status").get().equals("active") || user.getValue("account_role").get().equals("GBO")) {
			LOG.warning("You do not have permission to do this");
			return Response.status(Status.FORBIDDEN).build();
		} else {
			LOG.info("User " + username + " logged in sucessfully");
			return Response.ok(g.toJson(user.getValue("user_description").get())).build();
		}
	}
}
