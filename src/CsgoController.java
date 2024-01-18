import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CsgoController {

	Connection connection;
	Statement st;
	PreparedStatement pr;

	public CsgoController(Connection connection) {
		this.connection = connection;

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
		//rellenarSkins();
		//rellenarCajas();
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
	//Metodo para rellenar llaves
	public void rellenarLlaves() {
		String csvFile = "src/CSV/datos_llaves.csv";
		String line = "";
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			String sql = "INSERT INTO datos_llaves VALUES (?,?,?) ON CONFLICT DO NOTHING";

			pr = connection.prepareStatement(sql);
			br.readLine(); //Leer la primera línea para ignorar los encabezados

			while ((line = br.readLine()) != null) {
				//Limpiar los parámetros antes de cada inserción
				pr.clearParameters();
				//Dividir la línea en datos utilizando la coma como delimitador
				String[] data = line.split(",");

				//Verificar la longitud del arreglo antes de acceder a los índices
				if (data.length >= 3) {
					// Establecer los parámetros en la sentencia SQL
					pr.setString(1, data[0].replace("\"", ""));
					pr.setFloat(2, parsePrecio(data[1].replace("\"", "")));

					//Obtener la lista de cajas y almacenarlas una por una
					String[] cajas = data[2].split("\n");
					for (String caja : cajas) {
						//Establecer el parámetro en la sentencia SQL y ejecutar la inserción
						pr.setString(3, caja.trim().replace("\"", ""));
						try {
							pr.executeUpdate();
						} catch (SQLException e) {
							//Manejar la excepción de SQL específicamente para la columna "Caja_que_abre"
							if (e.getMessage().contains("ERROR: column \"caja_que_abre\" is of type integer")) {
								System.out.println("Error al insertar los datos de llaves en la tabla llaves: Valor no válido para 'caja_que_abre'");
							} else {
								// Manejar otras excepciones de SQL
								System.out.println("Error al insertar los datos de llaves en la tabla llaves: " + e.getMessage());
							}
						}
					}
				} else {
					//Manejar el caso en el que no haya suficientes elementos en el arreglo
					System.out.println("Datos insuficientes para insertar en la tabla llaves: " + line);
					continue; // Pasar a la siguiente iteración del bucle
				}
			}
			System.out.println("Datos de llaves insertados correctamente");
		} catch (Exception e) {
			System.out.println("Error al insertar los datos de llaves en la tabla llaves: " + e.getMessage());
		}
	}



	private float parsePrecio(String precio) {
		if (precio.trim().equals("-")) {
			return 0.0f; // Manejar el caso en que el precio sea "-"
		}

		// Eliminar símbolo de dólar, espacios y comas antes de la conversión
		precio = precio.replace("$", "").replace(",", "").trim();

		try {
			return Float.parseFloat(precio);
		} catch (NumberFormatException e) {
			System.out.println("Error al convertir el precio a un número: " + e.getMessage());
			return 0.0f; // En caso de error, devolver un valor predeterminado
		}
	}



	//Metodo para rellear las skins
	public void rellenarSkins(){
		String csvFile = "src/CSV/datos_skins.csv";
		String line = "";

		try(BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String sql = "INSERT INTO datos_skins VALUES(?,?) ON CONFLICT DO NOTHING";

			pr = connection.prepareStatement(sql);

			br.readLine();

			while ((line = br.readLine())!=null) {
				pr.clearParameters();

				String[] data = line.split("\t");

				if (data.length >= 2) {
					pr.setString(1, data[0].trim());
					pr.setString(2, data[1].trim());
				}else {
					System.out.println("Datos insuficientes para insertar en la tabla skins: " + line);
					continue;
				}
				pr.executeUpdate();
			}
			System.out.println("Datos de Skins insertados correctamente en la tabla skin");
		}catch (Exception e) {
			System.out.println("Error al insertar datos de skins en la tablas skins: " + e.getMessage());
		}
	}
	//Metodo para rellenar las cajas
	public void rellenarCajas(){
		String csvFile = "src/CSV/nombre_cajas.csv";
		String line = "";

		try(BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String sql = "INSERT INTO nombre_cajas VALUES(?) ON CONFLICT DO NOTHING";

			pr = connection.prepareStatement(sql);

			br.readLine();
			//Aqui vamos a leer cada línea del archivo CSV
			while ((line = br.readLine()) != null) {

				pr.clearParameters();

				String[] data = line.split("\t");

				pr.setInt(1, Integer.parseInt(data[2].replace("\"", "")));

				//Ejecutamos la inserción
				pr.executeUpdate();
			}
			System.out.println("Datos de Cajas insertados correctamente en la tabla skin");
		}catch (Exception e) {
			System.out.println("Error al insertar datos de skins en la tabla skins: " +e.getMessage());
		}
	}

}



/*



	//Metodo para mostrar los datos de una columna especifica de una tabla especifica de la bsd
	public void selectColumna() {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("ESribre la tabla que quieres buscar: armas, llaves, skins, cajas");
			String tabla = sc.next();
			ResultSet rs2 = st.executeQuery("SELECT *" + "FROM" + tabla);
			ResultSetMetaData metaData = rs2.getMetaData();
			int columnCount = metaData.getColumnCount();
			System.out.println();

			//Bucle para imprimir el nombre de cada columna
			for (int i = 1; i <=columnCount ; i++) {
				System.out.println(metaData.getColumnName(i) + " ");
			}
			System.out.println();
			System.out.println();
			System.out.println("Por favor, escriba la columna que quiera buscar");

			String columna = sc.next();
			System.out.println();

			ResultSet rs = st.executeQuery("SELECT" + columna + "FROM" + tabla);
			while (rs.next()) {
				System.out.println(rs.getString(columna));
			}
			rs.close();
			Thread.sleep(1000);
		}catch (Exception e) {
			System.out.println("Comprueba que se ha escrito bien: " +e.getMessage());
		}
	}
	//Metodo par aseleccionar una tabla completa de una bsd y también mostrar el contenido en consola
	public void selectTabla() {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Escriba la tabla que quiera busca: armas, llaves, skins, cajas");
			String tabla = sc.next();
			System.out.println();

			ResultSet rs = st.executeQuery("SELECT *" + "FROM" + tabla);
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
 */