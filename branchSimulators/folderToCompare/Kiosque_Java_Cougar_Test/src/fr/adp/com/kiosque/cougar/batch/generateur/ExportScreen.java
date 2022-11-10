package fr.adp.com.kiosque.cougar.batch.generateur;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.adp.com.cougar.std.mot_met.lib.objects.TableRequetesForBatch;
import fr.adp.com.cougar.std.mot_met.lib.tools.intereter.GenerationDataInitializer;
import fr.adp.com.cougar.std.mot_met.lib.tools.intereter.MetaEcranObj;
import fr.adp.com.cougar.std.mot_tech.lib.gdb_mut.GDB_U;
import fr.adp.com.cougar.std.mot_tech.lib.livraison.obj.MetaDonneeEcranObj;
import fr.adp.com.cougar.std.mot_tech.lib.livraison.obj.MetaDonneeGeneraleObj;
import fr.adp.com.cougar.std.mot_tech.lib.livraison.obj.MetaGroupeDonneesInfoObj;
import fr.adp.com.cougar.std.mot_tech.lib.livraison.obj.MetaGroupeDonneesObj;
import fr.adp.com.gxp.std.mot_tech.lib.GDB;
import fr.adp.com.gxp.std.mot_tech.lib.Exception.GxpException;
import fr.adp.com.gxp.std.mot_tech.lib.context.PersistenceContexte;
import fr.adp.com.gxp.std.mot_tech.lib.deploiement.DeploiementParametrageMgr;
import fr.adp.com.gxp.std.mot_tech.lib.logger.GxpLogger;
import fr.adp.com.gxp.std.mot_tech.lib.tools.O_UtilCaractere;
import fr.adp.com.gxp.std.moteur.comm_base.gxp_db.GxpDb;
import oracle.jdbc.pool.OracleDataSource;

public class ExportScreen {

	private static String currentScreen = null;
	
	public static void main(String[] args){
		try {
			List<String> params = Arrays.asList(args);
			String url 		= params.stream().filter(p -> p.startsWith("url")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String port		= params.stream().filter(p -> p.startsWith("port")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String instance	= params.stream().filter(p -> p.startsWith("instance")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String user 	= params.stream().filter(p -> p.startsWith("user")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String password = params.stream().filter(p -> p.startsWith("password")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String idProjet = params.stream().filter(p -> p.startsWith("idProjet")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String numpac = params.stream().filter(p -> p.startsWith("numpac")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			List<String> screens = Arrays.asList(
					params.stream()
					.filter(p -> p.startsWith("screens"))
					.map(p -> p.substring(p.indexOf('=')+1))
					.findFirst()
					.orElse("")
					.trim()
					.split(","))
					.stream()
						.map(String::trim)
						.filter(O_UtilCaractere::isNotEmptyTrim)
						.collect(Collectors.toList());

			initDataBaseConnection(url,port,instance,user,password);
			PersistenceContexte.init(numpac, null);
			
			final GenerationDataInitializer generationDataInitializerNew = new GenerationDataInitializer();
			if(O_UtilCaractere.isEmptyTrim(idProjet)) {
	            DeploiementParametrageMgr.preparationTableLivraison(0, "*");
				generationDataInitializerNew.initializeCashForDeploiement("TGE");
			} else {
				generationDataInitializerNew.initializeCashForBacASable("*", null, idProjet);
			}

			List<MetaEcranObj> lesEcrans = generationDataInitializerNew.getEcrans()
					.stream()
					.filter(m -> !m.getNOM_PAGE().endsWith("_WORKFLOW"))
					.filter(m -> screens.isEmpty() || screens.contains(m.getNOM_PAGE()))
					.collect(Collectors.toList());
			
			for(MetaEcranObj unEcran : lesEcrans) {
				currentScreen = unEcran.getNOM_PAGE();
				Map<String,Object> jsonEcran = getEcran(unEcran);
				String json = mapToJson(jsonEcran);
				GxpLogger.simpleTraceOut("var " + currentScreen + " = " + json + ";");
				saveJson(unEcran.getNOM_PAGE(),json);
			}
			
		} catch (GxpException | IOException e) {
			GxpLogger.traceError(e);
		}
	}
	
	private static void saveJson(String nomPage, String json) throws IOException {
		String currentDir = System.getProperty("user.dir");
		Files.write(Paths.get(currentDir, "..", "Kiosque_Java_Cougar_Script", "Ecrans", nomPage + ".json"), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	private static String mapToJson(Map<String, Object> jsonEcran) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(Feature.QUOTE_FIELD_NAMES);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonEcran);
	}

	private static Map<String,Object> getEcran(MetaEcranObj unEcran) {
		Map<String,Object> jsonEcran = new HashMap<>();
		jsonEcran.put("element", "ecran");
		jsonEcran.put("code", unEcran.getNOM_PAGE());
		jsonEcran.put("libelle", unEcran.getLIBELLE_PAGE());
		jsonEcran.put("type", unEcran.getTYPE_ECRAN());
		jsonEcran.put("servicelet", unEcran.getNOM_SERVICELET());
		if(unEcran.isUnitaire()) {
			jsonEcran.put("boutons", unEcran.getBOUTONS_ECRAN());
		}
		jsonEcran.put("groupe", getGroupe(unEcran.getGroupeDonneesInfo()));
		return jsonEcran;
	}
	
	private static Map<String,Object> getGroupe(MetaGroupeDonneesInfoObj unGroupe) {
		Map<String,Object> jsonGroupe = new HashMap<>();
		jsonGroupe.put("element", "groupe");
		jsonGroupe.put("code", unGroupe.getID_GROUPE());
		jsonGroupe.put("libelle", unGroupe.getLIBELLE_GROUPE());
		if("O".equals(unGroupe.getEST_ONGLET())) {
			jsonGroupe.put("onglet", true);
		}
		if(unGroupe.getReglegestionAffRD() != null) {
			jsonGroupe.put("affichage", unGroupe.getReglegestionAffRD().ID_REGLEGESTION);
		}
		List<Map<String,Object>> lesDonnees = new ArrayList<>();
		for(MetaGroupeDonneesObj donnee : unGroupe.getGroupeDonnees()) {
			lesDonnees.add(getDonnee(donnee));
		}
		jsonGroupe.put("donnees", lesDonnees);
		return jsonGroupe;
	}
	
	private static Map<String,Object> getDonnee(MetaGroupeDonneesObj donnee) {
		if(donnee.getNOM_DONNEE().startsWith("#GROUPE")) {
			return getGroupe(donnee.getGroupeDonneesInclus());
		} else if (donnee.getNOM_DONNEE().startsWith("#")) {
			return getDonneeStandard(donnee);
		} else {
			return getDonneeSpecif(donnee);
		}
	}
	
	private static Map<String,Object> getDonneeStandard(MetaGroupeDonneesObj donnee) {
		Map<String,Object> jsonDonnee = new HashMap<>();
		String nomDonnee = donnee.getNOM_DONNEE().lastIndexOf('_') > -1 ? donnee.getNOM_DONNEE().substring(0,donnee.getNOM_DONNEE().lastIndexOf('_')) : donnee.getNOM_DONNEE();
		jsonDonnee.put("element", "donnee");
		jsonDonnee.put("code", nomDonnee);
		if("O".equals(donnee.getA_COTE_DONNEE_PRECEDENTE())) {
			jsonDonnee.put("memeColonne", true);
		}
		switch (nomDonnee) {
		case "#TABLEAU":
			jsonDonnee.put("requete", TableRequetesForBatch.getId(donnee.getTABLEAU_REQUETE_NOMPAGE(), donnee.getTABLEAU_REQUETE_IDREQUETE()));
			if(O_UtilCaractere.isNotEmptyTrim(donnee.getNOM_PAGE_AJOUT())) {
				jsonDonnee.put("pageAjout", donnee.getNOM_PAGE_AJOUT());
			}
			if(O_UtilCaractere.isNotEmptyTrim(donnee.getNOM_PAGE_MODIF())) {
				jsonDonnee.put("pageModif", donnee.getNOM_PAGE_MODIF());
			}
			if(O_UtilCaractere.isNotEmptyTrim(donnee.getBOUTONS_TABLEAU_MULTIPLE())) {
				List<Map<String,String >> lesBoutonsTableaux = new ArrayList<>();
				Arrays.asList(donnee.getBOUTONS_TABLEAU_MULTIPLE().split(",")).forEach(unBouton -> {
					Map<String,String> mapBouton = new HashMap<>();
					mapBouton.put("libelle", unBouton.split("#")[0]);
					mapBouton.put("operation", unBouton.split("#")[1]);
					lesBoutonsTableaux.add(mapBouton);
				});
				jsonDonnee.put("boutons", lesBoutonsTableaux);
			}
			break;
		case "#TEXTE":
		case "#LIBELLE":
			if(donnee.getSUFFIXE_OU_LIBELLE().contains("µ")) {
				jsonDonnee.put("style", donnee.getSUFFIXE_OU_LIBELLE().split("µ")[0]);
				jsonDonnee.put("libelle", donnee.getSUFFIXE_OU_LIBELLE().split("µ").length>1? donnee.getSUFFIXE_OU_LIBELLE().split("µ")[1]:"");
			} else {
				jsonDonnee.put("libelle", donnee.getSUFFIXE_OU_LIBELLE());
			}
			break;
		case "#BOUTONPERSO":
			jsonDonnee.put("operation", donnee.getNOM_DONNEE().split("_")[1]);
			jsonDonnee.put("libelle", donnee.getSUFFIXE_OU_LIBELLE().split("µ")[0]);
			if(donnee.getSUFFIXE_OU_LIBELLE().contains("µ")) {
				jsonDonnee.put("icon", donnee.getSUFFIXE_OU_LIBELLE().split("µ")[1]);
			}
			break;
		case "#BOUTONS":
		case "#EOL":
		case "#SEP":
		default:
			break;
		}
		return jsonDonnee;
	}
	
	private static Map<String,Object> getDonneeSpecif(MetaGroupeDonneesObj donnee) {
		MetaDonneeGeneraleObj donneeGen = donnee.getDonneeGenerale();
		MetaDonneeEcranObj donneeEcr = donneeGen.getDonneeEcran(currentScreen);
		
		Map<String,Object> jsonDonnee = new HashMap<>();
		jsonDonnee.put("element", "donnee");
		jsonDonnee.put("code", donnee.getNOM_DONNEE());
		jsonDonnee.put("libelle", donneeGen.getLIBELLE_DONNEE());
		jsonDonnee.put("type", donneeGen.getTYPE_DONNEE());
		jsonDonnee.put("saisie", donneeEcr.getMODE_SAISIE());
		if("O".equals(donnee.getA_COTE_DONNEE_PRECEDENTE())) {
			jsonDonnee.put("memeColonne", true);
		}
		if("O".equals(donnee.getMASQUER_LE_LIBELLE())) {
			jsonDonnee.put("masquerLibelle", true);
		}
		if(O_UtilCaractere.isNotEmptyTrim(donneeEcr.getEST_OBLIGATOIRE()) && !"N".equals(donneeEcr.getEST_OBLIGATOIRE())) {
			jsonDonnee.put("obligatoire", "O".equals(donneeEcr.getEST_OBLIGATOIRE()) ? Boolean.valueOf(true) : donneeEcr.getEST_OBLIGATOIRE());
		}
		getAppelTable(jsonDonnee, donneeEcr);
		if("select".equals(donneeEcr.getMODE_SAISIE()) && "O".equals(donneeEcr.getAFFICHE_CHOIX_VIDE_SELECT())) {
			jsonDonnee.put("vide", true);
		}
		getSuffixe(jsonDonnee, donnee);
		if("textarea".equals(donneeEcr.getMODE_SAISIE()) && donneeEcr.getNbLigneTexteArea() > 1) {
			jsonDonnee.put("lignes", donneeEcr.getNbLigneTexteArea());
		}
		if(donneeEcr.getReglegestionAffRD() != null) {
			jsonDonnee.put("affichage", donneeEcr.getReglegestionAffRD().ID_REGLEGESTION);
		}
		if(donneeEcr.getReglegestionAutRD() != null) {
			jsonDonnee.put("automatisme", donneeEcr.getReglegestionAutRD().ID_REGLEGESTION);
		}
		switch (donneeGen.getTYPE_DONNEE()) {
		case "X":
			jsonDonnee.put("longueur", Integer.parseInt(donneeGen.getLONGUEUR_MASQUE_NB_ENTIERS()));
			if("O".equals(donneeGen.getMINUSCULES_AUTORISEES())) {
				jsonDonnee.put("minuscle", true);
			}
			break;
		case "N":
			jsonDonnee.put("entier", Integer.parseInt(donneeGen.getLONGUEUR_MASQUE_NB_ENTIERS()));
			break;
		case "9":
			getType9(donneeGen, jsonDonnee);
			break;
		case "D":
			jsonDonnee.put("format", donneeGen.getLONGUEUR_MASQUE_NB_ENTIERS());
			break;
		default:
			break;
		}
		return jsonDonnee;
	}

	private static void getSuffixe(Map<String, Object> jsonDonnee, MetaGroupeDonneesObj donnee) {
		if(O_UtilCaractere.isNotEmptyTrim(donnee.getSUFFIXE_OU_LIBELLE())) {
			if(donnee.getSUFFIXE_OU_LIBELLE().contains("µ")) {
				jsonDonnee.put("style", donnee.getSUFFIXE_OU_LIBELLE().split("µ", -1)[0]);
				if(donnee.getSUFFIXE_OU_LIBELLE().indexOf('µ') < donnee.getSUFFIXE_OU_LIBELLE().length() - 1) {
					jsonDonnee.put("suffixe", donnee.getSUFFIXE_OU_LIBELLE().split("µ", -1)[1]);
				}
			} else {
				jsonDonnee.put("suffixe", donnee.getSUFFIXE_OU_LIBELLE());
			}
		}
	}

	private static void getType9(MetaDonneeGeneraleObj donneeGen, Map<String, Object> jsonDonnee) {
		jsonDonnee.put("entier", Integer.parseInt(donneeGen.getLONGUEUR_MASQUE_NB_ENTIERS()));
		jsonDonnee.put("decimal", Integer.parseInt(donneeGen.getNB_DECIMAUX()));
		if(O_UtilCaractere.isNotEmptyTrim(donneeGen.getMINUSCULES_AUTORISEES())) {
			switch (donneeGen.getMINUSCULES_AUTORISEES()) {
			case "X":
				jsonDonnee.put("negatif", true);
				jsonDonnee.put("zero", true);
				break;
			case "G":
				jsonDonnee.put("negatif", true);
				break;
			case "Z":
				jsonDonnee.put("zero", true);
				break;
			default:
				break;
			}
		}
	}

	private static void getAppelTable(Map<String, Object> jsonDonnee, MetaDonneeEcranObj donneeEcr) {
		if(O_UtilCaractere.isNotEmptyTrim(donneeEcr.getAPPELTABLE_NOMPAGE()) && donneeEcr.getAPPELTABLE_IDREQUETE() != null) {
			jsonDonnee.put("requete", TableRequetesForBatch.getId(donneeEcr.getAPPELTABLE_NOMPAGE(), Integer.toString(donneeEcr.getAPPELTABLE_IDREQUETE())));
			if(O_UtilCaractere.isNotEmptyTrim(donneeEcr.getAPPELTABLE_LISTECHAMPS())) {
				jsonDonnee.put("retour", donneeEcr.getAPPELTABLE_LISTECHAMPS());
			}
			if(O_UtilCaractere.isNotEmptyTrim(donneeEcr.getAPPELTABLE_PARAMETRES())) {
				jsonDonnee.put("params", donneeEcr.getAPPELTABLE_PARAMETRES());
			}
			if(donneeEcr.getNbLigneTexteArea() > 1) {
				jsonDonnee.put("maxSelect", donneeEcr.getNbLigneTexteArea());
			}
		} else if(O_UtilCaractere.isNotEmptyTrim(donneeEcr.getAPPELTABLE_LISTECHAMPS()) && O_UtilCaractere.isNotEmptyTrim(donneeEcr.getAPPELTABLE_LISTELIBELLE())) {
			List<Map<String,String>> valeurs = new ArrayList<>();
			List<String> lib = Arrays.asList(donneeEcr.getAPPELTABLE_LISTELIBELLE().split(","));
			List<String> val = Arrays.asList(donneeEcr.getAPPELTABLE_LISTECHAMPS().split(","));
			for(int i = 0; i < lib.size(); i++) {
				Map<String, String> valeur = new HashMap<>();
				valeur.put("code", val.get(i));
				valeur.put("libelle", lib.get(i));
				valeurs.add(valeur);
			}
			jsonDonnee.put("valeurs", valeurs);
		}
	}

	private static void initDataBaseConnection(String url, String port, String instance, String user, String password) throws GxpException {
		GxpDb db = new GxpDb() {
			@Override
			protected String getDataSourceName() { return null; }
			
			@Override
			protected boolean initGxpDB(String jndiDSName) throws GxpException {
				OracleDataSource ds;
				try {
					ds = new OracleDataSource();
					ds.setDriverType("thin");
					ds.setServerName(String.valueOf(url));
					ds.setPortNumber(Integer.parseInt(port));
					ds.setDatabaseName(String.valueOf(instance));
					ds.setUser(String.valueOf(user));
					ds.setPassword(String.valueOf(password));
					this.setDataSource(ds);
				} catch (SQLException e) {
					GxpLogger.traceError(e);
					return false;
				}
				return true;
			}
		};
		GDB.init(db);
		GDB_U.setGDB_U(db);
	}
}
