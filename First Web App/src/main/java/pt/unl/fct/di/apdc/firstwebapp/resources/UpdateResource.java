package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;


import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
//import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/update")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UpdateResource {

	private static final Logger LOG = Logger.getLogger(UpdateResource.class.getName());
	
	public UpdateResource() {}
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	@PUT
	@Path("{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUserInfo(String description, @HeaderParam(value = "tokenId") String tokenId,
			@PathParam("username") String username) {
		
		Transaction txn = datastore.newTransaction();
		/*Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(tokenId);
		Entity authToken = txn.get(tokenKey);
		if(authToken == null) {
			LOG.info("Token does not exist");
			return Response.status(Status.FORBIDDEN).build();
		}
		if((long) authToken.getValue("expiration_data").get() < System.currentTimeMillis()) {
			LOG.warning("Your session has timed out");
			return Response.status(Status.FORBIDDEN).build();
		}*/
		Key currentUserKey = datastore.newKeyFactory().setKind("User").newKey(username);
		LOG.fine("Attempt to update info on " + username);
		try {
			Entity user = Entity.newBuilder(txn.get(currentUserKey)).set("user_description", description).build();
			txn.update(user);
			LOG.info(username + " info updated");
			txn.commit();
			return Response.ok("{}").build();
		} finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
