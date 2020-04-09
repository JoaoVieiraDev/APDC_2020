package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public LogoutResource() {}
	
	@POST
	//@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(AuthToken auth) {
		LOG.fine("Logout attempt by user " + auth.username);
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(auth.username);
		Transaction txn = datastore.newTransaction();
		Entity token = txn.get(tokenKey);
		if(token == null) {
			LOG.warning("You do not have permission to do this");
			return Response.status(Status.FORBIDDEN).build();
		}
		txn.delete(tokenKey);
		txn.commit();
		LOG.info("User " + auth.username + " logged out sucessfully");
		return Response.ok().build();
	}
}
