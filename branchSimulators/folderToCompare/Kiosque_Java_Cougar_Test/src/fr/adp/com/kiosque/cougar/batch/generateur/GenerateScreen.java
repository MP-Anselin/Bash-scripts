package fr.adp.com.kiosque.cougar.batch.generateur;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import fr.adp.com.cougar.gdb.GxpDbAlone;
import fr.adp.com.cougar.std.mot_met.lib.tools.intereter.GenerationDataInitializer;
import fr.adp.com.cougar.std.mot_met.lib.tools.intereter.MetaEcranObj;
import fr.adp.com.cougar.std.mot_tech.lib.gdb_mut.GDB_U;
import fr.adp.com.gxp.std.mot_tech.lib.Exception.GxpException;
import fr.adp.com.gxp.std.mot_tech.lib.deploiement.DeploiementParametrageMgr;
import fr.adp.com.gxp.std.mot_tech.lib.logger.GxpLogger;
import fr.adp.com.gxp.std.mot_tech.lib.tools.O_UtilCaractere;
import fr.adp.com.kiosque.cougar.tools.generateur.GenerateJspKrhMgr;
import fr.adp.com.kiosque.cougar.tools.generateur.GenerateMVVMExtJsKrhMgr;


public class GenerateScreen {

	public static void main(String[] args){
		try {
			List<String> params = Arrays.asList(args);
			String url 		= params.stream().filter(p -> p.startsWith("url")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String user 	= params.stream().filter(p -> p.startsWith("user")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String password = params.stream().filter(p -> p.startsWith("password")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
			String idProjet = params.stream().filter(p -> p.startsWith("idProjet")).map(p -> p.substring(p.indexOf('=')+1)).findFirst().orElse("").trim();
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
						.collect(Collectors.toList());

			initDataBaseConnection(url,user,password);
			
			final GenerationDataInitializer generationDataInitializerNew = new GenerationDataInitializer();
			if(O_UtilCaractere.isEmptyTrim(idProjet)) {
				DeploiementParametrageMgr.preparationTableLivraison(0, "*");
				generationDataInitializerNew.initializeCashForDeploiement("TGE");
			} else {
				generationDataInitializerNew.initializeCashForBacASable("*", null, idProjet);
			}

			final List<MetaEcranObj> listMetaEcrans = generationDataInitializerNew.getEcrans()
					.stream()
					.filter(m -> (screens.size() == 1 && "".equals(screens.get(0)) && !m.getNOM_PAGE().endsWith("_WORKFLOW")) || screens.contains(m.getNOM_PAGE()))
					.collect(Collectors.toList());
			
			GenerateMVVMExtJsKrhMgr.doInitIgnoreResponsiveScreenList();
			listMetaEcrans.forEach(ecr -> GenerateScreen.genererUnEcran(ecr, idProjet));
		} catch (GxpException e) {
			GxpLogger.traceError(e);
		}
	}

	private static void genererUnEcran(MetaEcranObj unEcran, String idProjet) {
		try {
			if(GenerateJspKrhMgr.generateJspFiles(idProjet,unEcran)) {
				GenerateJspKrhMgr.getInterpreteurAsObject(unEcran);
				String repApplicatif = System.getProperty("user.dir") + "/../Kiosque_Java_Cougar_Web/WebContent";
				GenerateJspKrhMgr.stockageUneJsp(idProjet,repApplicatif, unEcran);
				getScript(unEcran,idProjet);
			} else {
				throw new GxpException("");
			}
		} catch (GxpException e) {
			GxpLogger.simpleTraceOut("Erreur lors de la génération de l'écran "+ unEcran.getLIBELLE_PAGE().replace("'", "''") + ".");
			GxpLogger.simpleTraceOut(e.getMessage());
		}
	}

	private static void getScript(MetaEcranObj unEcran, String idProjet) {
		GxpLogger.simpleTraceOut("- - - - - - - - - SCRIPT - - - - - - - - -");
		if(unEcran.isPopup()) {
			GxpLogger.simpleTraceOut("INSERT INTO SYS_PAGES_MODAL VALUES('" + unEcran.getExtjsTemplateName(idProjet) + "','" + unEcran.getLIBELLE_PAGE().replace("'", "''") + "');");
		} else {
			if(O_UtilCaractere.isEmptyTrim(idProjet)) {
				GxpLogger.simpleTraceOut("CALL ADM_ADD_PAGE_IN_SCHEMA(-1,'Kiosque RH','Expert','[Nom de l''onglet]','[Nom du sous-menu]','" + unEcran.getLIBELLE_PAGE().replace("'", "''") + "','" + unEcran.getExtjsTemplateName(idProjet) + "',3);");
			} else {
				GxpLogger.simpleTraceOut("CALL ADM_ADD_PAGE_IN_SCHEMA(-1,'Kiosque RH','Expert','Test Onglet','Test Menu','" + unEcran.getLIBELLE_PAGE().replace("'", "''") + "','" + unEcran.getExtjsTemplateName(idProjet) + "',3);");
			}
			
			String canonicalName = "fr.adp.com.kiosque.jsonservlet.XXX.XXX.XXX." + unEcran.getNOM_SERVICELET();
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SERVICELET VALUES('J','" + unEcran.getNOM_SERVICELET() + "','" + canonicalName + "','O');");
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SCH_JSON VALUES('" + unEcran.getExtjsTemplateName(idProjet) + "','" + unEcran.getNOM_SERVICELET() + "');");
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SCH_PAGE_INITIALIZER VALUES('" + unEcran.getExtjsTemplateName(idProjet) + "',NULL,NULL,'" + unEcran.getNOM_SERVICELET() + "');");
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SCH_PROFIL_P VALUES(-1,'" + unEcran.getExtjsTemplateName(idProjet) + "','E','N');");
		}
		GxpLogger.simpleTraceOut("- - - - - - - - - - - - - - - - - - - - -");
	}

	private static void initDataBaseConnection(String url, String user, String password) throws GxpException {
		GxpDbAlone gdb = new GxpDbAlone();
		GxpDbAlone.logger.setLevel(Level.parse("FINE"));
		gdb.setUrlDB(url);
		gdb.setUserDB(user);
		gdb.setPasswordDB(password);
		gdb.lanceCommandeSQLSecurized("select sysdate from dual", null);
		GDB_U.setGDB_U(gdb);
	}
}
