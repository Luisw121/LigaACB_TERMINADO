import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

public class ACBMain {

	public static void main(String[] args) throws IOException, SQLException, ParseException, InterruptedException {
		ACBMenu menu = new ACBMenu();

		ConnectionFactory connectionFactory = ConnectionFactory.getInstance();
		Connection c = connectionFactory.connect();

		CsgoController csgoController = new CsgoController(c);

		int option = menu.mainMenu();
		while (option > 0 && option < 12) {
			switch (option) {
			case 1:
				csgoController.crearTablas();
				// dbaccessor.mostraAutors();
				break;

			case 2:

				csgoController.mostrarTablas();
				break;

			case 3:

				csgoController.eliminarTablas();
				break;

			case 4:

				csgoController.rellenarTablas();
				break;

			case 5:

				// dbaccessor.altaRevista();
				break;

			case 6:

				break;

			case 7:
				// dbaccessor.actualitzarTitolRevistes(conn);
				break;

			case 8:
				// dbaccessor.afegeixArticleARevista(conn);
				break;

			case 9:
				// dbaccessor.desassignaArticleARevista(conn);
				break;

			case 10:
				// dbaccessor.carregaAutors(conn);
				break;

			case 11:
				// dbaccessor.sortir();
				break;

			default:
				System.out.println("Introdueixi una de les opcions anteriors");
				break;

			}
			option = menu.mainMenu();
		}

	}

}
