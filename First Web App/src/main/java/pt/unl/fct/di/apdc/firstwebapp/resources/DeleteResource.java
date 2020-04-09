package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	public DeleteResource() {}
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	@DELETE
	@Path("/{username}")
	public Response deleteUser(@PathParam("username") String username, AuthToken auth) {
		LOG.fine("Attempt to delete user " + username);
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
		/*
		if(auth.expirationData != (long) tokenOriginal.getValue("expiration_data").get()) {
			LOG.warning("This is not your token! " + auth.username + " " + tokenOriginal.getValue("user_name").get());
			txn.delete(currentUserKey);
			LOG.info("User " + auth.username + " was deleted.");
			txn.commit();
			return Response.status(Status.FORBIDDEN).build();
		}
		*/
		if(auth.expirationData < System.currentTimeMillis()) {
			LOG.warning("Your session has timed out");
			return Response.status(Status.FORBIDDEN).build();
		} else if(!auth.username.equals(username) && !currentUser.getValue("account_role").get().equals("GBO")) {
			LOG.warning("You do not have permission to do this, or you are not GBO");
			return Response.status(Status.FORBIDDEN).build();
		} else {
			try {
				if(txn.get(userKey) == null) {
					LOG.warning("Your account no longer exists");
					return Response.status(Status.FORBIDDEN).build();
				}
				txn.delete(userKey);
				LOG.info("User " + username + " was deleted.");
				txn.commit();
				return Response.ok("{}").build();	
			} finally {
				if(txn.isActive()) {
					txn.rollback();
				}
			}
		}
	}
}
