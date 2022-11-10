package fr.adp.com.kiosque.cougar.batch.generateur;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.adp.com.cougar.std.mot_met.lib.tools.intereter.MetaEcranObj;
import fr.adp.com.cougar.std.mot_met.lib.tools.intereter.threadLinks.LoadDestinationEcran;
import fr.adp.com.cougar.std.mot_tech.lib.gdb_mut.GDB_U;
import fr.adp.com.cougar.std.mot_tech.lib.livraison.obj.MetaDestinationEcranObj;
import fr.adp.com.cougar.std.mot_tech.lib.livraison.updateproduct.MapperFromTableUtility;
import fr.adp.com.gxp.std.mot_tech.lib.GDB;
import fr.adp.com.gxp.std.mot_tech.lib.Exception.GxpException;
import fr.adp.com.gxp.std.mot_tech.lib.context.PersistenceContexte;
import fr.adp.com.gxp.std.mot_tech.lib.logger.GxpLogger;
import fr.adp.com.gxp.std.mot_tech.lib.tools.O_UtilCaractere;
import fr.adp.com.gxp.std.moteur.comm_base.gxp_db.GxpDb;
import fr.adp.com.kiosque.cougar.tools.generateur.GenerateScreenMgr;
import oracle.jdbc.pool.OracleDataSource;


public class GenerateScreen {

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
			GenerateScreenMgr.generateScreen(screens, "".equals(idProjet) ? null : Integer.parseInt(idProjet));
			
			String nomTable = "".equals(idProjet) ? "REF_ECRAN" : "META_ECRAN";
			String query = "SELECT * FROM " + nomTable;
			if(!screens.isEmpty()) {
				query += " WHERE NOM_PAGE IN (" + screens.stream().map(s -> "?").collect(Collectors.joining(",")) + ")";
			}
			List<Object> list = new ArrayList<>();
			list.addAll(screens);
			GDB_U.getGDB_U().dBLoadReqSecurized(MetaEcranObj.class, query, 0,  list).forEach(ecr -> getScript(ecr, idProjet));
		} catch (GxpException | NumberFormatException | IOException e) {
			GxpLogger.traceError(e);
		}
	}

	private static void getScript(MetaEcranObj unEcran, String idProjet) {
		MetaDestinationEcranObj dest = new MetaDestinationEcranObj();
		dest.setREP_DESTINATION("RD");
		unEcran.setDestinationEcran(dest);
		GxpLogger.simpleTraceOut("- - - - - - - - - SCRIPT - - - - - - - - -");
		if(unEcran.isPopup()) {
			GxpLogger.simpleTraceOut("INSERT INTO SYS_PAGES_MODAL VALUES('" + unEcran.getExtjsTemplateName(idProjet) + "','" + unEcran.getLIBELLE_PAGE().replace("'", "''") + "');");
		} else {
			if(O_UtilCaractere.isEmptyTrim(idProjet)) {
				GxpLogger.simpleTraceOut("CALL ADM_ADD_PAGE_IN_SCHEMA(-1,'Kiosque RH','Expert','[Nom de l''onglet]','[Nom du sous-menu]','" + unEcran.getLIBELLE_PAGE().replace("'", "''") + "','" + unEcran.getExtjsTemplateName(idProjet) + "',NULL,'fr_fr',3);");
			} else {
				GxpLogger.simpleTraceOut("CALL ADM_ADD_PAGE_IN_SCHEMA(-1,'Kiosque RH','Expert','Test Onglet','Test Menu','" + unEcran.getLIBELLE_PAGE().replace("'", "''") + "','" + unEcran.getExtjsTemplateName(idProjet) + "',NULL,'fr_fr',3);");
			}
			
			String canonicalName = "fr.adp.com.kiosque.jsonservlet.XXX.XXX.XXX." + unEcran.getNOM_SERVICELET();
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SERVICELET VALUES('J','" + unEcran.getNOM_SERVICELET() + "','" + canonicalName + "','O');");
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SCH_JSON VALUES('" + unEcran.getExtjsTemplateName(idProjet) + "','" + unEcran.getNOM_SERVICELET() + "');");
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SCH_PAGE_INITIALIZER VALUES('" + unEcran.getExtjsTemplateName(idProjet) + "',NULL,NULL,'" + unEcran.getNOM_SERVICELET() + "');");
			GxpLogger.simpleTraceOut("INSERT INTO SYS_SCH_PROFIL_P VALUES(-1,'" + unEcran.getExtjsTemplateName(idProjet) + "','E','N');");
		}
		GxpLogger.simpleTraceOut("- - - - - - - - - - - - - - - - - - - - -");
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
