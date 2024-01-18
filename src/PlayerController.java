import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class PlayerController {

	private Connection connection;
	
	public PlayerController(Connection c) {
		this.connection = connection;
	}

}
