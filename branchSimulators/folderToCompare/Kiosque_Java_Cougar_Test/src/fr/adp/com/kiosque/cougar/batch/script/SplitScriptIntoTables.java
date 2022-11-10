package fr.adp.com.kiosque.cougar.batch.script;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.adp.com.gxp.std.mot_tech.lib.logger.GxpLogger;
import fr.adp.com.gxp.std.mot_tech.lib.tools.O_UtilCaractere;

public class SplitScriptIntoTables {
	
	// Projet courant
	private static Path destPath = Paths.get(System.getProperty("user.dir"), "..", "Kiosque_Java_Cougar_Script");

	public static void main(String[] args) {
		// On recupere le script d'import (ressource selectionnee)
		String scriptPath = Arrays.asList(args)
			.stream()
			.filter(p -> p.startsWith("script"))
			.map(p -> p.substring(p.indexOf('=')+1))
			.findFirst()
			.orElse("")
			.trim();
		
		List<String> commandes = new ArrayList<>();
		String fileName = null;
		List<String> bloc = new ArrayList<>();
		boolean lineTableName = false;

		try (Stream<String> stream = Files.lines(Paths.get(scriptPath),Charset.forName("windows-1252"))) {
			commandes = stream.collect(Collectors.toList());
			
			// On va lire bloc par bloc (## jusqu'au prochain ##)
			for(String line : commandes) {
				if(lineTableName) {
					fileName = line.split(" ")[0];
					lineTableName = false;
				}
				// Nouveau bloc
				if("##".equals(line)) {
					if(!bloc.isEmpty()) {
						// On ecrit le bloc precedent
						writeBloc(fileName, bloc);
					}
					bloc.clear();
					// La prochaine ligne sera le nom du fichier/objet
					lineTableName = true;
				}
				// Ajout de la ligne au bloc
				bloc.add(line);
			}
			// Ecrit le dernier bloc
			writeBloc(fileName, bloc);
		} catch (IOException e) {
			GxpLogger.traceError(e);
		}
	}
	

	/**
	 * Ecrit un bloc MAJBASE dans le fichier correspondant
	 * @param fileName
	 * @param bloc
	 * @throws IOException
	 */
	private static void writeBloc(String fileName, List<String> bloc) throws IOException {
		// On ignore les blocs TAG_CHARSET et TAG_IMPORT
		if(fileName.startsWith("TAG_CHARSET=") || fileName.startsWith("TAG_IMPORT=")) return;
		
		// Determine le dossier ou ranger le fichier
		String folderModel = findScript("Model",fileName);
		String folderClient = findScript("Client",fileName);
		
		if(O_UtilCaractere.isNotEmptyTrim(folderModel)) {
			if("Structures/Procedures".equals(folderModel) && "ADM_ADD_PAGE_IN_SCHEMA".equals(fileName) && bloc.get(3).toUpperCase().startsWith("CALL")) {
				folderModel = "Autres";
				fileName = "SCHEMA";
			}
			writeModel(fileName, bloc, folderModel);
		} else if(O_UtilCaractere.isNotEmptyTrim(folderClient)) {
			writeClient(fileName, bloc, folderClient);
		} else {
			bloc.addAll(0, getEntete());
			Files.write(destPath.resolve("NewScript").resolve(fileName + ".txt"), bloc, Charset.forName("windows-1252"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
	}
	
	
	/**
	 * Determine le dossier ou ranger le fichier
	 * @param fileName
	 * @return
	 */
	private static String findScript(String clientOrModel, String fileName) {
		List<String> folders = Arrays.asList(
			"Tables",
			"Structures/Functions",
			"Structures/Indexes",
			"Structures/Packages",
			"Structures/Procedures",
			"Structures/Sequences",
			"Structures/Triggers",
			"Structures/Types",
			"Structures/Views"
		);
		return folders
			.stream()
			.filter(f -> destPath.resolve(clientOrModel).resolve(f).resolve(fileName + ".txt").toFile().exists())
			.findFirst()
			.orElse(null);
	}
	
	
	/**
	 * Ecrit dans le dossier Model et dans le dossier client si le script se trouve au meme endroit
	 * @param fileName
	 * @param bloc
	 * @param folderModel
	 * @throws IOException
	 */
	private static void writeModel(String fileName, List<String> bloc, String folderModel) throws IOException {
		Path fileModel = destPath.resolve("Model").resolve(folderModel).resolve(fileName + ".txt");
		// On cherche le script au meme endroit dans le dossier Clent
		Path fileClient = destPath.resolve("Client").resolve(folderModel).resolve(fileName + ".txt");
		// Pour les structures on ecrase, et les tables on ajoute a la fin
		if(folderModel.contains("Structures")) {
			bloc.addAll(0, getEntete());
			Files.write(fileModel, bloc, Charset.forName("windows-1252"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			// Si le script structure existe aussi dans le dossier client on l'ecrase
			if(fileClient.toFile().exists()) {
				Files.write(fileClient, bloc, Charset.forName("windows-1252"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			}
		} else {
			Files.write(fileModel, bloc, Charset.forName("windows-1252"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			// Si le script table existe aussi dans le dossier client on ajoute
			if(fileClient.toFile().exists()) {
				Files.write(fileClient, bloc, Charset.forName("windows-1252"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
		}
	}

	
	/**
	 * Ecrit dans le dossier Client
	 * @param fileName
	 * @param bloc
	 * @param folderModel
	 * @param folderClient
	 * @throws IOException
	 */
	private static void writeClient(String fileName, List<String> bloc, String folderClient)
			throws IOException {
		Path fileClient = destPath.resolve("Client").resolve(folderClient).resolve(fileName + ".txt");
		// Pour les structures on ecrase, et les tables on ajoute a la fin
		if(folderClient.contains("Structures")) {
			Files.write(fileClient, bloc, Charset.forName("windows-1252"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} else {
			Files.write(fileClient, bloc, Charset.forName("windows-1252"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
	}

	
	/**
	 * Renvoie les lignes pour le TAG_CHARSET
	 * @return
	 */
	private static List<String> getEntete() {
		return Arrays.asList(
			"##",
			"TAG_CHARSET=@€ιθη"
		);
	}
}
