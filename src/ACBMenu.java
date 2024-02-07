import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ACBMenu {
	private int option;

	public ACBMenu() {
		super();
	}

	public int mainMenu() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		do {

			System.out.println(" \nMENU PRINCIPAL \n");

			System.out.println("1. Crear Tablas");
			System.out.println("2. Mostrar Tablas");
			System.out.println("3. Eliminar Tablas");
			System.out.println("4. Poblar las tablas ");
			System.out.println("5. Seleccionar una columna");
			System.out.println("6. Seleccionar una Tabla");
			System.out.println("7. Modificar un registro");
			System.out.println("8. Eliminar un registro");
			System.out.println("9. Eliminar un conjunto");
			System.out.println("10. Modificar registros");
			System.out.println("Esculli opció: ");
			try {
				option = Integer.parseInt(br.readLine());
			} catch (NumberFormatException | IOException e) {
				System.out.println("valor no vàlid");
				e.printStackTrace();
			}

		} while (option != 1 && option != 2 && option != 3 && option != 4 && option != 5 && option != 6 && option != 7
				&& option != 8 && option != 9 && option != 10);

		return option;
	}

	public Identity authenticate(int tries) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("============================ACB=============================");
		System.out.println("============================================================");
		System.out.println("Avís: tens " + (3 - tries) + " intents per loginarte");
		System.out.println("============================================================");
		System.out.println("Inserta nom del usuari: ");
		String user = br.readLine();
		System.out.println("Inserta contrasenya: ");
		String password = br.readLine();

		Identity identity = new Identity(user, password);
		return identity;

	}

}
