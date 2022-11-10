package fr.adp.com.kiosque.cougar.batch.gsRest;

import java.util.logging.Level;

import fr.adp.com.cougar.gdb.GxpDbAlone;
import fr.adp.com.cougar.std.mot_tech.lib.gdb_mut.GDB_U;
import fr.adp.com.gxp.std.mot_met.lib.objects.admin.EndUserAEBodyDetails;
import fr.adp.com.gxp.std.mot_met.lib.objects.admin.GsToolsRest;
import fr.adp.com.gxp.std.mot_tech.lib.Exception.GxpException;
import fr.adp.com.gxp.std.mot_tech.lib.logger.GxpLogger;
import fr.adp.com.gxp.std.mot_tech.lib.tools.O_UtilCaractere;
import fr.adp.com.kiosque.cougar.tools.rest.GsToolsRestKrh;

public class GsRestCall {

	public static void main(String[] args) {
        
		GxpDbAlone gdb;
		try {
			gdb = new GxpDbAlone();
			GxpDbAlone.logger.setLevel(Level.parse("FINE"));
			gdb.setUrlDB("jdbc:oracle:thin:@vksqd01.esi.adp.com:12000:VKSQD01");
			gdb.setUserDB("krhmodele");
			gdb.setPasswordDB("krhvmod1");
			gdb.lanceCommandeSQLSecurized("select sysdate from dual", null);			
			GDB_U.setGDB_U(gdb);
			String grestUrl = GsToolsRest.getGsRestUrl();
			//String aoid=inputData.getContexte().getAoid();//"MBONNET-N78";
			String aoid="MBONNET-N78";
			//String cid=inputData.getContexte().getCurrentClientId();//"FR200904214300453";//"FR20050517112734263"
			String cid="FR200904214300453";
			
			if(!O_UtilCaractere.isEmpty(cid) && !O_UtilCaractere.isEmpty(grestUrl)){
				//Lecture des informations détails de l'utilisateur sur GS
				EndUserAEBodyDetails userToModify = GsToolsRestKrh.readEndUserAEBodyDetails(aoid, cid);
				GxpLogger.simpleTraceOut("---GET USER---");
				GxpLogger.simpleTraceOut(userToModify.getFirstName()+"-"+userToModify.getLastName()+"-Login="+userToModify.getLogin()+"-mail="+userToModify.getEmail()+"-Birth date="+userToModify.getBirthDate());				
				GxpLogger.simpleTraceOut("---Update USER---");
				userToModify.setBirthDate("20230706");
				GsToolsRestKrh.updateEndUserAEBodyDetails(userToModify, cid);
				GxpLogger.simpleTraceOut("---GET USER AFTER UPDATE---");
				GxpLogger.simpleTraceOut(userToModify.getFirstName()+"-"+userToModify.getLastName()+"-Login="+userToModify.getLogin()+"-mail="+userToModify.getEmail()+"-Birth date="+userToModify.getBirthDate());				

		}
		} catch (GxpException e1) {

			GxpLogger.traceError(e1);
		}
		


}
}
