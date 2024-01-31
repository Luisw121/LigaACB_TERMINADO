import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CsgoController {

	Connection connection;
	Statement st;
	PreparedStatement pr;

	public CsgoController(Connection connection) {
		this.connection = connection;
		try {
			this.st = connection.createStatement();
		} catch (SQLException e) {
			System.out.println("Error al crear el Statement: " + e.getMessage());
		}

	}
	public void crearTablas() {
		try(BufferedReader br = new BufferedReader(new FileReader("src/schema.sql"))) {
			String sqlScrip = br.lines().collect(Collectors.joining(" \n"));

			try (Statement statement = connection.createStatement()){
				statement.execute(sqlScrip);
				System.out.println("Se ha creado");
			}catch (SQLException e) {
				System.out.println("Error al ejecutar el scrip : " + e.getMessage());
			}
		}catch (IOException e) {
			System.out.println("Comprueba el fichero schema.sql" + e.getMessage());
		}catch (Exception e) {
			System.out.println("Error inesperado: " + e.getMessage());
		}
	}
	public void mostrarTablas() {
		try {
			mostrarTabla("Datos_Armas");
			mostrarTabla("Datos_Llaves");
			mostrarTabla("Datos_Skins");
			mostrarTabla("Nombre_Cajas");
		} catch (IOException | SQLException e) {
			System.out.println("Error al mostrar las tablas: " + e.getMessage());
		}
	}
	public void mostrarTabla(String tableName) throws IOException, SQLException {
		String sql = "SELECT * FROM " + tableName;

		try(Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql)) {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			System.out.println("Datos de la tabla " + tableName + ":");
			for (int i = 1; i <= columnCount; i++) {
				System.out.println(metaData.getColumnName(i) + "\t");
			}
			System.out.println();

			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					System.out.println(rs.getString(i) + "\t");
				}
				System.out.println();
			}
		}catch (SQLException e) {
			System.out.println("Error al mostrar la tabla " + tableName + ": " +e.getMessage());
		}
	}
	public void eliminarTablas() {
		try {
			pr = connection.prepareStatement("DROP TABLE datos_armas, datos_llaves, datos_skins, nombre_cajas CASCADE");
			pr.executeUpdate();
			System.out.println("Se han eliminado correctamente");
			Thread.sleep(1000);
		}catch (Exception e) {
			System.out.println("No se puede eliminar todas las tablas, revisa que exsistan: " + e.getMessage());
		}
	}

	//Metodo para rellenar Tablas()
	public void rellenarTablas() {
		rellenarArmas();
		rellenarLlaves();
		rellenarSkins();
		rellenarCajas();
		System.out.println("Se ha rellenado correctamente");
	}
	//Metodo para rellenar las tablas de armas en la base de datos
	public void rellenarArmas() {
		String csvFile = "src/CSV/datos_armas.csv";
		String line = "";
		try(BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			String sql = "INSERT INTO datos_armas (nombre_arma, damage_lmb, damage_rmb, kill_award, running_speed, side) VALUES (?,?,?,?,?,?) ON CONFLICT DO NOTHING";

			pr = connection.prepareStatement(sql);
			br.read();

			while ((line = br.readLine()) != null) {
				//Limpiar los parámetros antes de cada inserción
				pr.clearParameters();
				//Dividir la linea en datos utilizando la coma como delimitador
				String[] data = line.split(",");

				//Establecemos los parámetros en la sentencia SQL
				pr.setString(1, data[0].replace("\"", ""));

				try {
					pr.setInt(2, Integer.parseInt(data[1].replace("\"", "")));
					pr.setInt(3, Integer.parseInt(data[2].replace("\"", "")));
				}catch (NumberFormatException e) {
					pr.setInt(2, 0);
					pr.setInt(3, 0);
				}

				pr.setString(4, data[3].replace("\"", ""));

				String cleanDoubleString = data[4].replaceAll("[^\\d.]", "");
				try {
					pr.setDouble(5, Double.parseDouble(cleanDoubleString));

				}catch (NumberFormatException e) {
					pr.setDouble(5, 0.0);
				}

				pr.setString(6, data[5].replace("\"", ""));

				//Ejecutar la inserción
				pr.executeUpdate();
			}
			System.out.println("Datos de armas insertados correctamente");
		}catch (Exception e) {
			System.out.println("Error al insertar los datos de armas en la tabla armas: " + e.getMessage());
		}
	}

	public void rellenarLlaves() {
		String csvFile = "src/CSV/datos_llaves.csv";

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String sql = "INSERT INTO datos_llaves (Nombre_llave, Precio_llave, Caja_que_abre) VALUES (?, ?, ARRAY[?]::VARCHAR[]) ON CONFLICT DO NOTHING";

			try (PreparedStatement pr = connection.prepareStatement(sql)) {
				br.readLine(); // Leer la primera línea para ignorar los encabezados

				String line;
				while ((line = br.readLine()) != null) {
					String[] data = line.split(","); // Ajusta el delimitador según tu formato CSV

					if (data.length >= 3) {
						pr.setString(1, data[0].trim());

						// Procesar el precio y convertirlo a BigDecimal
						String precioStr = data[1].trim().replace("$", "");
						BigDecimal precio = new BigDecimal(precioStr);
						pr.setBigDecimal(2, precio);

						// Convertir el array de cajas a una lista y luego insertar cada caja
						String[] cajas = data[2].split("\n");
						pr.setArray(3, connection.createArrayOf("VARCHAR", cajas));
						pr.executeUpdate();
					} else {
						System.out.println("Datos insuficientes para insertar en la tabla llaves: " + line);
					}
				}
				System.out.println("Datos de llaves insertados correctamente en la tabla llaves");
			}
		} catch (Exception e) {
			System.out.println("Error al insertar datos de llaves en la tabla llaves: " + e.getMessage());
		}
	}


	// Método para rellenar las skins
	public void rellenarSkins() {
		String csvFile = "src/CSV/datos_skins.csv";

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String sql = "INSERT INTO datos_skins (Nombre_caja, Nombre_skin) VALUES ((SELECT ID_caja FROM Nombre_Cajas WHERE Nombre_caja = ?), ?) ON CONFLICT DO NOTHING";

			try (PreparedStatement pr = connection.prepareStatement(sql)) {
				br.readLine(); // Leer la primera línea para ignorar los encabezados

				String line;
				while ((line = br.readLine()) != null) {
					String[] data = line.split(","); // Ajusta el delimitador según tu formato CSV

					if (data.length >= 2) {
						pr.setString(1, data[0].trim());
						pr.setString(2, data[1].trim());
						pr.executeUpdate();
					} else {
						System.out.println("Datos insuficientes para insertar en la tabla skins: " + line);
					}
				}
				System.out.println("Datos de Skins insertados correctamente en la tabla skin");
			}
		} catch (SQLException e) {
			System.out.println("Error al insertar datos de skins en la tabla skins: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace(); // Imprime la traza completa del error
		}
	}


	// Método para rellenar las cajas
	public void rellenarCajas() {
		String csvFile = "src/CSV/nombre_cajas.csv";

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String sql = "INSERT INTO nombre_cajas (Nombre_caja) VALUES (?) ON CONFLICT DO NOTHING";

			try (PreparedStatement pr = connection.prepareStatement(sql)) {
				br.readLine(); // Leer la primera línea para ignorar los encabezados

				String line;
				while ((line = br.readLine()) != null) {
					String[] data = line.split(","); // Ajusta el delimitador según tu formato CSV

					if (data.length >= 1) {
						pr.setString(1, data[0].trim());

						pr.executeUpdate();
					} else {
						System.out.println("Datos insuficientes para insertar en la tabla cajas: " + line);
					}
				}
				System.out.println("Datos de Cajas insertados correctamente en la tabla caja");
			}
		} catch (Exception e) {
			System.out.println("Error al insertar datos de cajas en la tabla cajas: " + e.getMessage());
		}
	}
	//Metodo para mostrar los datos de una columna especifica de una tabla especifica de la bsd

	public void selectColumna() {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Escribe la tabla que quieres buscar:Datos_Armas,Datos_Llaves,Datos_Skins,Nombre_Cajas");
			String tabla = sc.next();

			// Espacios adicionales agregados en las consultas SQL
			ResultSet rs2 = st.executeQuery("SELECT * FROM " + tabla);
			ResultSetMetaData metaData = rs2.getMetaData();
			int columnCount = metaData.getColumnCount();
			System.out.println();

			// Bucle para imprimir el nombre de cada columna
			for (int i = 1; i <= columnCount; i++) {
				System.out.print(metaData.getColumnName(i) + " ");
			}

			System.out.println();
			System.out.println();
			System.out.println("Por favor, escriba la columna que quiera buscar");

			String columna = sc.next();
			System.out.println();

			ResultSet rs = st.executeQuery("SELECT " + columna + " FROM " + tabla);
			while (rs.next()) {
				System.out.println(rs.getString(columna));
			}

			rs.close();
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("Comprueba que se ha escrito bien: " + e.getMessage());
		}
	}
	//Metodo par aseleccionar una tabla completa de una bsd y también mostrar el contenido en consola
	public void selectTabla() {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Escriba la tabla que quiera busca:Datos_Armas,Datos_Llaves,Datos_Skins,Nombre_Cajas");
			String tabla = sc.next();
			System.out.println();

			ResultSet rs = st.executeQuery("SELECT *" + "FROM " + tabla);
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			System.out.println();
			//Imprimimos el nombre de cada columna
			for (int i = 1; i < columnCount; i++) {
				System.out.println(metaData.getColumnName(i) + " ");
			}
			System.out.println();
			while (rs.next()) {
				//Bucle para recorrer cada columna de la fila actual
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					System.out.println(rs.getString(i) + " ");
				}
				System.out.println();
			}
			Thread.sleep(1000);

			rs.close();

		}catch (Exception e) {
			System.out.println("Comprueba que exsiste la tabla " + e.getMessage());
		}
	}
}



/*





 */