package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	
	public String username;
	public String password;
	public String email;
	public String role;
	
	public RegisterData() {}
	
	public RegisterData(String username, String password, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
	}
	
	public boolean validRegistration() {
		if(this.username != null && this.username != "" && this.password != null && this.password != ""
				&& this.email != null && this.email != "" && this.role != null && this.role != ""
				&& (this.role.equals("USER") || this.role.equals("GBO"))) return true;
		else return false;
	}

}
