import java.io.IOException;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.net.smtp.*;

public class ClientSMTP2 {
	
	public static void main (String[] args) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, InvalidKeySpecException {
		
		Scanner teclado = new Scanner(System.in);
		
		//Es crea el client SMTP segur
		AuthenticatingSMTPClient client = new AuthenticatingSMTPClient();
		
		//Dades d'usuari i del servidor
		String server = "smtp.gmail.com";
		String username = "correopruebasizan@gmail.com";
		String contrasenya = "xxxxxxxxxxxxx";
		int port = 587;
		
		try {
			
			int resposta;
			//Creació de la clau per establir el canal segur
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(null, null);
			KeyManager km = kmf.getKeyManagers() [0];
			
			//Ens connectem al servidor SMTP
			client.connect(server, port);
			System.out.println("1 - "+client.getReplyString());
			
			//S'estableix la clau per a la comunicació segura
			client.setKeyManager(km);
			
			resposta = client.getReplyCode();
			if (!SMTPReply.isPositiveCompletion(resposta)) {
				
				client.disconnect();
				System.err.println("CONNEXIÓ REBUTJADA");
				System.exit(1);
				
			}
			
			//S'envia l'ordre EHLO
			client.ehlo(server);//Cal fer-ho
			System.out.println("2 - "+client.getReplyString());
			
			//S'executa l'ordre STARTTLS i es comprova si és true
			if (client.execTLS()) {
				
				System.out.println("3 -"+client.getReplyString());
				
				//Es fa l'autenticació amb el servidor
				if (client.auth(AuthenticatingSMTPClient.AUTH_METHOD.PLAIN, username, contrasenya)) {
					
					System.out.println("4 -"+client.getReplyString());
					
					System.out.println("Introduce una direccion de destino: Exemple -> izandueso@gmail.com");
					String desti1 = teclado.nextLine();
					while (desti1.length() > 100){
						System.out.println("Error, el correu de desti es invalid, torna-ho a introduir-lo: ");
						desti1 = teclado.nextLine();
					}
					System.out.println("Introduce un asunto para el correo: ");
					String asumpte = teclado.nextLine();
					while(asumpte.length() > 100){
						System.out.println("Error, el assumpte es massa llarg, torna-ho a introduir:");
						asumpte = teclado.nextLine();
						
					}
					
					System.out.println("Introduce el mensaje que deseas enviar: ");
					String missatge = teclado.nextLine();
					while(missatge.length() > 2000){
						System.out.println("Error, el missatge es massa gran, torna-ho a introduir: ");
						missatge = teclado.nextLine();
					}
					
					//Es crea la capçalera
					SimpleSMTPHeader capcalera = new SimpleSMTPHeader(username, desti1, asumpte);
					
					//El nom d'usuari i el email d'origen coincideixen
					client.setSender(username);
					client.addRecipient(desti1);
					System.out.println("5 -"+client.getReplyString());
					
					//S'envia DATA
					Writer writer = client.sendMessageData();
					
					if (writer == null) {
						System.out.println("ERRADA al enviar DATA");
						System.exit(1);
					}
					
					writer.write(capcalera.toString());//Capçalera
					writer.write(missatge);//El missatge
					writer.close();
					System.out.println("6 -"+client.getReplyString());
					
					boolean exit = client.completePendingCommand();
					System.out.println("7 -"+client.getReplyString());
					
					if (!exit) {
						System.out.println("ERRADA al finalitzar la TRANSACCIÓ");
						System.exit(1);
					}
					
				} else {
					
					System.out.println("USUARI NO AUTENTICAT");
					
				}
				
			} else {
				
				System.out.println("ERRADA AL EXECUTAR STRATTLS");
				
			}
			
		} catch (IOException e) {
			
			System.out.println("No pot connectar amb el servidor");
			e.printStackTrace();
			System.exit(1);
			
		}
		
		try {
			client.disconnect();
		} catch (IOException f) {f.printStackTrace(); }
		
		System.out.println("Final de l'enviament");
		System.exit(0);
		
	}

}
