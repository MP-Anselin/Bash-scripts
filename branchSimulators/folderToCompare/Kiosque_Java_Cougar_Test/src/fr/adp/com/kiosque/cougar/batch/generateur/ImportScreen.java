package fr.adp.com.kiosque.cougar.batch.generateur;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.adp.com.cougar.gdb.GxpDbAlone;
import fr.adp.com.cougar.std.mot_tech.lib.gdb_mut.GDB_U;
import fr.adp.com.gxp.std.mot_tech.lib.Exception.GxpException;
import fr.adp.com.gxp.std.mot_tech.lib.logger.GxpLogger;



public class ImportScreen {

	private static final String OBLIGATOIRE2 = "obligatoire";
	private static final String SAISIE = "saisie";
	private static final String LIBELLE_DONNEE = "LIBELLE_DONNEE";
	private static final String CODE = "code";
	private static final String STYLE = "style";
	private static final String SUFFIXE = "suffixe";
	private static final String TYPE_GROUPE = "#GROUPE";
	private static final String AFFICHAGE = "affichage";
	private static final String NOM_DONNEE = "NOM_DONNEE";
	private static final String BOUTONS = "boutons";
	private static final String LIBELLE = "libelle";
	private static final String GROUPE = "groupe";
	private static final String ID_GROUPE = "ID_GROUPE";
	private static final String NOM_PAGE = "NOM_PAGE";
	private static final String COMPANY_ID = "COMPANY_ID";
	private static final String REQUETE = "requete";
	private static final Map<String,String> ecran = new HashMap<>();
	private static final List<Map<String,String>> groupeInfo = new ArrayList<>();
	private static final List<Map<String,String>> groupeDonnees = new ArrayList<>();
	private static final List<Map<String,String>> donneeGen = new ArrayList<>();
	private static final List<Map<String,String>> donneeEcr = new ArrayList<>();
	
	public static void main(String[] args){
		try {
			List<String> params = Arrays.asList(args);
			String url 		= params.stream().filter(p -> p.startsWith("url")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String user 	= params.stream().filter(p -> p.startsWith("user")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String password = params.stream().filter(p -> p.startsWith("password")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String ecranPath= params.stream().filter(p -> p.startsWith("screen")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			initDataBaseConnection(url,user,password);
			String ecrStr = Files.readAllLines(Paths.get(ecranPath),StandardCharsets.ISO_8859_1).stream().collect(Collectors.joining(" "));
			JSONObject jsonEcr = new JSONObject(ecrStr);
			addScreen(jsonEcr);
			insertBase("REF_ECRAN", Arrays.asList(COMPANY_ID,NOM_PAGE), ecran);
			insertBase("META_ECRAN", Arrays.asList(COMPANY_ID,NOM_PAGE), ecran);
			for(Map<String,String> grp : groupeInfo) {
				insertBase("REF_GROUPE_DONNEES_INFO", Arrays.asList(COMPANY_ID,ID_GROUPE), grp);
				insertBase("META_GROUPE_DONNEES_INFO", Arrays.asList(COMPANY_ID,ID_GROUPE), grp);
				// On supprime tout le contenu du GROUPE_DONNEES si jamais on a supprimé des données 
				GDB_U.getGDB_U().dBDeleteReq_Securized("DELETE FROM REF_GROUPE_DONNEES WHERE COMPANY_ID = ? AND ID_GROUPE = ?", Arrays.asList("*", grp.get(ID_GROUPE)));
				GDB_U.getGDB_U().dBDeleteReq_Securized("DELETE FROM META_GROUPE_DONNEES WHERE COMPANY_ID = ? AND ID_GROUPE = ?", Arrays.asList("*", grp.get(ID_GROUPE)));
			}
			
			for(Map<String,String> grp : groupeDonnees) {
				insertBase("REF_GROUPE_DONNEES", Arrays.asList(COMPANY_ID,ID_GROUPE,NOM_DONNEE), grp);
				insertBase("META_GROUPE_DONNEES", Arrays.asList(COMPANY_ID,ID_GROUPE,NOM_DONNEE), grp);
			}
			for(Map<String,String> donnee : donneeGen) {
				insertBase("REF_DONNEE_GENERALE", Arrays.asList(COMPANY_ID,NOM_DONNEE), donnee);
				insertBase("META_DONNEE_GENERALE", Arrays.asList(COMPANY_ID,NOM_DONNEE), donnee);
			}
			for(Map<String,String> donnee : donneeEcr) {
				insertBase("REF_DONNEE_ECRAN", Arrays.asList(COMPANY_ID,NOM_DONNEE, NOM_PAGE), donnee);
				insertBase("META_DONNEE_ECRAN", Arrays.asList(COMPANY_ID,NOM_DONNEE, NOM_PAGE), donnee);
			}
			GxpLogger.simpleTraceOut("Import de l'écran " + ecran.get(NOM_PAGE) + " terminé avec succès");
		} catch (GxpException | IOException | JSONException e) {
			GxpLogger.traceError(e);
		}
	}
	
	private static void addScreen(JSONObject jsonEcr) throws JSONException {
		JSONObject jsonGroupe = jsonEcr.getJSONObject(GROUPE);

		ecran.put(COMPANY_ID, "*");
		ecran.put(NOM_PAGE, jsonEcr.getString(CODE));
		ecran.put("LIBELLE_PAGE", jsonEcr.getString(LIBELLE));
		ecran.put("CODE_DESTINATION", "RD");
		ecran.put("ROLE_ECRAN", "EXP");
		ecran.put("NOM_DICTIONNAIRE", "DIC_DONNEES_VIRTUELLES");
		ecran.put("MODULE_ECRAN", "STD");
		ecran.put("BOUTONS_ECRAN", jsonEcr.has(BOUTONS) ? jsonEcr.getString(BOUTONS) : "INT");
		ecran.put("ID_GROUPE_DONNEES", jsonGroupe.getString(CODE));
		ecran.put("TYPE_ECRAN", jsonEcr.getString("type"));
		ecran.put("NOM_SERVICELET", jsonEcr.getString("servicelet"));
		ecran.put("EST_SUPPRIME", "N");
		ecran.put("SURCHARGE_EDITION", "N");
		ecran.put("ONGLET_VERTICAL", "N");
		
		addGroupe(jsonGroupe);
	}
	
	private static void addGroupe(JSONObject jsonGroupe) throws JSONException {
		addGroupeInfo(jsonGroupe);
		addGroupeDonnees(jsonGroupe);
	}
	
	private static void addGroupeInfo(JSONObject jsonGroupe) throws JSONException {
		Map<String,String> unGroupeInfo = new HashMap<>();
		unGroupeInfo.put(COMPANY_ID, "*");
		unGroupeInfo.put(ID_GROUPE, jsonGroupe.getString(CODE));
		unGroupeInfo.put("ID_REGLEGESTION_AFF_RD", jsonGroupe.has(AFFICHAGE) ? jsonGroupe.getString(AFFICHAGE) : null);
		unGroupeInfo.put("EST_ONGLET", getOorN(jsonGroupe,"onglet"));
		unGroupeInfo.put("LIBELLE_GROUPE", jsonGroupe.getString(LIBELLE));
		unGroupeInfo.put("IS_VISIBLE", "1");
		unGroupeInfo.put("DEGRE_LIBERTE", "1");
		groupeInfo.add(unGroupeInfo);
	}

	private static void addGroupeDonnees(JSONObject jsonGroupe) throws JSONException {
		Map<String,Integer> compteurs = new HashMap<>();
		compteurs.put(TYPE_GROUPE, 1);
		compteurs.put("#TABLEAU", 1);
		compteurs.put("#TEXTE", 1);
		compteurs.put("#LIBELLE", 1);
		compteurs.put("#EOL", 1);
		compteurs.put("#SEP_ZONE", 1);
		int ordre = 1;
		
		JSONArray jsonDonnees = jsonGroupe.getJSONArray("donnees");
		for(int i = 0; i < jsonDonnees.length(); i++) {
			JSONObject uneDonnee = jsonDonnees.getJSONObject(i);
			Map<String,String> unGroupe = new HashMap<>();
			unGroupe.put(COMPANY_ID, "*");
			unGroupe.put(ID_GROUPE, jsonGroupe.getString(CODE));
			if(GROUPE.equals(uneDonnee.getString("element"))) {
				unGroupe.put(NOM_DONNEE, TYPE_GROUPE + "_" + compteurs.get(TYPE_GROUPE));
				compteurs.put(TYPE_GROUPE, compteurs.get(TYPE_GROUPE) + 1);
				unGroupe.put("SUFFIXE_OU_LIBELLE", uneDonnee.getString(CODE));
				addGroupe(uneDonnee);
			} else if(compteurs.containsKey(uneDonnee.getString(CODE))) {
				unGroupe.put(NOM_DONNEE, uneDonnee.getString(CODE) + "_" + compteurs.get(uneDonnee.getString(CODE)));
				compteurs.put(uneDonnee.getString(CODE), compteurs.get(uneDonnee.getString(CODE)) + 1);
			} else if("#BOUTONPERSO".equals(uneDonnee.getString(CODE))){
				unGroupe.put(NOM_DONNEE, uneDonnee.getString(CODE) + "_" + uneDonnee.getString("operation"));
			} else {
				unGroupe.put(NOM_DONNEE, uneDonnee.getString(CODE));
				addDonnee(uneDonnee);
			}
			unGroupe.put("ORDRE", Integer.toString(ordre++));
			unGroupe.put("A_COTE_DONNEE_PRECEDENTE", getOorN(uneDonnee,"memeColonne"));
			if(!GROUPE.equals(uneDonnee.getString("element")) && (uneDonnee.has(SUFFIXE) || uneDonnee.has(LIBELLE) || uneDonnee.has(STYLE))) {
				unGroupe.put("SUFFIXE_OU_LIBELLE", getSuffixeOuLibelle(uneDonnee));
			}
			getInfoTabGroupe(unGroupe,uneDonnee);
			unGroupe.put("MASQUER_LE_LIBELLE", getOorN(uneDonnee,"masquerLibelle"));
			groupeDonnees.add(unGroupe);
			
		}
	}

	private static void addDonnee(JSONObject jsonDonnee) throws JSONException {
		getDonneeGen(jsonDonnee);
		getDonneeEtoile(jsonDonnee);
		getDonneeSpecif(jsonDonnee);
	}
	
	private static void getDonneeGen(JSONObject jsonDonnee) throws JSONException {
		Map<String,String> uneDonneeGen = new HashMap<>();
		uneDonneeGen.put(COMPANY_ID, "*");
		uneDonneeGen.put(NOM_DONNEE, jsonDonnee.getString(CODE));
		uneDonneeGen.put(LIBELLE_DONNEE, jsonDonnee.getString(LIBELLE));
		uneDonneeGen.put("NOM_DICTIONNAIRE", "DIC_DONNEES_VIRTUELLES");
		uneDonneeGen.put("TYPE_DONNEE", jsonDonnee.getString("type"));
		uneDonneeGen.put("LONGUEUR_MASQUE_NB_ENTIERS", getLongueurMasqueEntiers(jsonDonnee));
		uneDonneeGen.put("NB_DECIMAUX", jsonDonnee.has("decimal") ? jsonDonnee.getString("decimal") : null);
		uneDonneeGen.put("CHECKBOX_VALEUR_ON", "checkbox".equals(jsonDonnee.getString(SAISIE)) ? "O" : null);
		uneDonneeGen.put("CHECKBOX_VALEUR_OFF", "checkbox".equals(jsonDonnee.getString(SAISIE)) ? "N" : null);
		uneDonneeGen.put("UTILISATION", "Z");
		uneDonneeGen.put("ACTIF", "O");
		uneDonneeGen.put("MINUSCULES_AUTORISEES", getMinuscule(jsonDonnee));
		uneDonneeGen.put("PROPAGER_TOUTES_OCCURRENCES", "N");
		donneeGen.add(uneDonneeGen);
	}
	
	private static void getDonneeEtoile(JSONObject jsonDonnee) throws JSONException {
		Map<String,String> uneDonneeEcrEtoile = new HashMap<>();
		uneDonneeEcrEtoile.put(COMPANY_ID, "*");
		uneDonneeEcrEtoile.put(NOM_DONNEE, jsonDonnee.getString(CODE));
		uneDonneeEcrEtoile.put(NOM_PAGE, "*");
		uneDonneeEcrEtoile.put(LIBELLE_DONNEE, jsonDonnee.getString(LIBELLE));
		uneDonneeEcrEtoile.put("EST_OBLIGATOIRE", "N");
		uneDonneeEcrEtoile.put("MODE_SAISIE", "text");
		uneDonneeEcrEtoile.put("TYPE_REGLEGESTION_AUT_CLI", "C");
		uneDonneeEcrEtoile.put("TYPE_REGLEGESTION_AFF_CLI", "C");
		uneDonneeEcrEtoile.put("TYPE_REGLEGESTION_CTL_CLI", "C");
		donneeEcr.add(uneDonneeEcrEtoile);
	}
	
	private static void getDonneeSpecif(JSONObject jsonDonnee) throws JSONException {
		Map<String,String> uneDonneeEcrSpecif = new HashMap<>();
		uneDonneeEcrSpecif.put(COMPANY_ID, "*");
		uneDonneeEcrSpecif.put(NOM_DONNEE, jsonDonnee.getString(CODE));
		uneDonneeEcrSpecif.put(NOM_PAGE, ecran.get(NOM_PAGE));
		uneDonneeEcrSpecif.put(LIBELLE_DONNEE, jsonDonnee.getString(LIBELLE));
		uneDonneeEcrSpecif.put("EST_OBLIGATOIRE", getObligatoire(jsonDonnee));
		uneDonneeEcrSpecif.put("MODE_SAISIE", jsonDonnee.getString(SAISIE));
		
		getAppelTable(uneDonneeEcrSpecif, jsonDonnee);
		
		uneDonneeEcrSpecif.put("ID_REGLEGESTION_AFF_RD", jsonDonnee.has(AFFICHAGE) ? jsonDonnee.getString(AFFICHAGE) : null);
		uneDonneeEcrSpecif.put("ID_REGLEGESTION_AUT_RD", jsonDonnee.has("automatisme") ? jsonDonnee.getString("automatisme") : null);
		
		if("textarea".equals(jsonDonnee.getString(SAISIE))) {
			uneDonneeEcrSpecif.put("NB_LIGNE_TEXTEAREA", jsonDonnee.has("lignes") ? jsonDonnee.getString("lignes") : "1");
		}
		
		uneDonneeEcrSpecif.put("TYPE_REGLEGESTION_AUT_CLI", "C");
		uneDonneeEcrSpecif.put("TYPE_REGLEGESTION_AFF_CLI", "C");
		uneDonneeEcrSpecif.put("TYPE_REGLEGESTION_CTL_CLI", "C");
		donneeEcr.add(uneDonneeEcrSpecif);
	}

	private static void getAppelTable(Map<String, String> uneDonneeEcrSpecif, JSONObject jsonDonnee)
			throws JSONException {
		if(jsonDonnee.has(REQUETE)) {
			int sepIndex = jsonDonnee.getString(REQUETE).lastIndexOf('_');
			uneDonneeEcrSpecif.put("APPELTABLE_NOMPAGE", jsonDonnee.getString(REQUETE).substring(0,sepIndex));
			uneDonneeEcrSpecif.put("APPELTABLE_IDREQUETE", jsonDonnee.getString(REQUETE).substring(sepIndex + 1));
			if(jsonDonnee.has("retour")) {
				uneDonneeEcrSpecif.put("APPELTABLE_LISTECHAMPS", jsonDonnee.getString("retour"));
			}
			if(jsonDonnee.has("params")) {
				uneDonneeEcrSpecif.put("APPELTABLE_PARAMETRES", jsonDonnee.getString("params"));
			}
			uneDonneeEcrSpecif.put("NB_LIGNE_TEXTEAREA", jsonDonnee.has("maxSelect") ? jsonDonnee.getString("maxSelect") : "1");
		} else if(jsonDonnee.has("valeurs")) {
			JSONArray lesValeurs = jsonDonnee.getJSONArray("valeurs");
			List<String> codes = new ArrayList<>();
			List<String> libelles = new ArrayList<>();
			for(int i = 0; i < lesValeurs.length(); i++) {
				codes.add(lesValeurs.getJSONObject(i).getString(CODE));
				libelles.add(lesValeurs.getJSONObject(i).getString(LIBELLE));
			}
			uneDonneeEcrSpecif.put("APPELTABLE_LISTECHAMPS", codes.stream().collect(Collectors.joining(",")));
			uneDonneeEcrSpecif.put("APPELTABLE_LISTELIBELLE", libelles.stream().collect(Collectors.joining(",")));
		}
	}
	
	private static String getSuffixeOuLibelle(JSONObject uneDonnee) throws JSONException {
		String lib = Arrays.asList("#TEXTE","#LIBELLE","#BOUTONPERSO").contains(uneDonnee.getString(CODE)) ? uneDonnee.getString(LIBELLE) : "";
		return  (uneDonnee.has(STYLE) ? uneDonnee.getString(STYLE) + "µ" : "") + 
				(uneDonnee.has(SUFFIXE) ? uneDonnee.getString(SUFFIXE) : lib) +
				(uneDonnee.has("icon") ? "µ" + uneDonnee.getString("icon") : "");
	}

	private static void getInfoTabGroupe(Map<String, String> unGroupe, JSONObject uneDonnee) throws JSONException {
		if("#TABLEAU".equals(uneDonnee.getString(CODE))) {
			if(uneDonnee.has(REQUETE)) {
				int sepIndex = uneDonnee.getString(REQUETE).lastIndexOf('_');
				unGroupe.put("TABLEAU_REQUETE_NOMPAGE", uneDonnee.getString(REQUETE).substring(0,sepIndex));
				unGroupe.put("TABLEAU_REQUETE_IDREQUETE", uneDonnee.getString(REQUETE).substring(sepIndex + 1));
			}
			unGroupe.put("NOM_PAGE_AJOUT", uneDonnee.has("pageAjout") ? uneDonnee.getString("pageAjout") : null);
			unGroupe.put("NOM_PAGE_MODIF", uneDonnee.has("pageModif") ? uneDonnee.getString("pageModif") : null);
			if(uneDonnee.has(BOUTONS)) {
				JSONArray lesBoutons = uneDonnee.getJSONArray(BOUTONS);
				List<String> lesBoutonsStr = new ArrayList<>();
				for(int j = 0; j < lesBoutons.length(); j++) {
					lesBoutonsStr.add(lesBoutons.getJSONObject(j).getString(LIBELLE) + "#" + lesBoutons.getJSONObject(j).getString("operation"));
				}
				unGroupe.put("BOUTONS_TABLEAU_MULTIPLE",lesBoutonsStr.stream().collect(Collectors.joining(",")));
			}
		}
	}
	
	private static String getOorN(JSONObject json, String propertie) throws JSONException {
		return json.has(propertie) && json.getBoolean(propertie) ? "O" : "N";
	}
	
	private static String getObligatoire(JSONObject jsonDonnee) throws JSONException {
		String obligatoire = null;
		if(jsonDonnee.has(OBLIGATOIRE2)) {
			if("true".equals(jsonDonnee.getString(OBLIGATOIRE2))) obligatoire = "O";
			else if("false".equals(jsonDonnee.getString(OBLIGATOIRE2))) obligatoire = "N";
			else obligatoire = jsonDonnee.getString(OBLIGATOIRE2);
		} else {
			obligatoire = "N";
		}
		return obligatoire;
	}

	private static String getMinuscule(JSONObject jsonDonnee) throws JSONException {
		String minusculeAutorisees = null;
		Boolean negatif = jsonDonnee.has("negatif") ? jsonDonnee.getBoolean("negatif") : null;
		Boolean zero = jsonDonnee.has("zero") ? jsonDonnee.getBoolean("zero") : null;
		if(jsonDonnee.has("minuscule") && jsonDonnee.getBoolean("jsonGroupe")) {
			minusculeAutorisees = "O";
		} else if(Boolean.TRUE.equals(negatif) && Boolean.TRUE.equals(zero)) {
			minusculeAutorisees = "X";
		} else if(Boolean.TRUE.equals(negatif)) {
			minusculeAutorisees = "G";
		} else if(Boolean.TRUE.equals(zero)) {
			minusculeAutorisees = "Z";
		} else if("9".equals(jsonDonnee.getString("type"))) {
			minusculeAutorisees = "Y";
		} else if("X".equals(jsonDonnee.getString("type"))) {
			minusculeAutorisees = "N";
		}
		return minusculeAutorisees;
	}

	private static String getLongueurMasqueEntiers(JSONObject jsonGroupe) throws JSONException {
		String longMasqueEntier = null;
		if(jsonGroupe.has("longueur")) {
			longMasqueEntier = jsonGroupe.getString("longueur");
		} else if(jsonGroupe.has("entier")) {
			longMasqueEntier = jsonGroupe.getString("entier");
		} else if(jsonGroupe.has("format")) {
			longMasqueEntier = jsonGroupe.getString("format");
		}
		return longMasqueEntier;
	}
	
	private static void insertBase(String table, List<String> keys, Map<String,String> datas) throws GxpException {
		String where = keys.stream().map(key -> key + " = ?").collect(Collectors.joining(" AND "));
		List<Object> params = keys.stream().map(datas::get).collect(Collectors.toList());
		GDB_U.getGDB_U().dBDeleteReq_Securized("DELETE FROM " + table + " WHERE " + where, params);
		GDB_U.getGDB_U().insereLigneSecurised(
				table + " (" + datas.keySet().stream().collect(Collectors.joining(",")) + ") ", 
				"VALUES (" + datas.values().stream().map(v -> "?").collect(Collectors.joining(",")) + ")", 
				true, 
				datas.values().stream().collect(Collectors.toList()));
	}
	
	private static void initDataBaseConnection(String url, String user, String password) throws GxpException {
		GxpDbAlone gdb = new GxpDbAlone();
		fr.adp.com.gxp.std.moteur.comm_base.gxp_db.GxpDbSecurized.logger.setLevel(Level.parse("FINE"));
		gdb.setUrlDB(url);
		gdb.setUserDB(user);
		gdb.setPasswordDB(password);
		gdb.lanceCommandeSQLSecurized("select sysdate from dual", null);
		GDB_U.setGDB_U(gdb);
	}
}
