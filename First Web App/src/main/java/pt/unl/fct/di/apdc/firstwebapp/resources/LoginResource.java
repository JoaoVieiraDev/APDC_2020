package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Gson g = new Gson();
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public LoginResource() {}
	
	@POST
	//@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);
		if(user != null) {
			String hashedPWD = user.getString("user_pwd");
			//if(hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
			if(hashedPWD.equals(data.password)) {
				Transaction txn = datastore.newTransaction();
				AuthToken token = new AuthToken(data.username);
				Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);
				if(txn.get(tokenKey) != null) {
					LOG.warning("You are already logged in " + data.username);
					return Response.status(Status.FORBIDDEN).build();
				}
				Entity user_token = Entity.newBuilder(tokenKey)
						.set("user_name", data.username)
						.set("creation_data", token.creationData)
						.set("expiration_data", token.expirationData)
						.build();
				txn.add(user_token);
				txn.commit();
				LOG.info("User " + data.username + " logged in sucessfully");
				return Response.ok(g.toJson(token)).build();
			} else {
				LOG.warning("Wrong password for username " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} else {
			LOG.warning("Failed login attempt for username " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}
	}
	
	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if(!username.equals("Satan")) {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}
}
