/* Copyright (c) 2018, EL.CO. SRL.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * THIS SOFTWARE IS PROVIDED FREE OF CHARGE AND ON AN "AS IS" BASIS,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED INCLUDING
 * WITHOUT LIMITATION THE WARRANTIES THAT IT IS FREE OF DEFECTS, MERCHANTABLE,
 * FIT FOR A PARTICULAR PURPOSE OR NON-INFRINGING. THE ENTIRE RISK
 * AS TO THE QUALITY AND PERFORMANCE OF THE SOFTWARE IS WITH YOU.
 * SHOULD THE SOFTWARE PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
 * NECESSARY SERVICING, REPAIR, OR CORRECTION.
 * IN NO EVENT SHALL ELCO SRL BE LIABLE TO YOU FOR DAMAGES, INCLUDING
 * ANY GENERAL, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING
 * OUT OF THE USE OR INABILITY TO USE THE SOFTWARE (INCLUDING, BUT NOT
 * LIMITED TO, LOSS OF DATA, DATA BEING RENDERED INACCURATE, LOSS OF
 * BUSINESS PROFITS, LOSS OF BUSINESS INFORMATION, BUSINESS INTERRUPTIONS,
 * LOSS SUSTAINED BY YOU OR THIRD PARTIES, OR A FAILURE OF THE SOFTWARE
 * TO OPERATE WITH ANY OTHER SOFTWARE) EVEN IF ELCO SRL HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package cartellaclinica.cartellaPaziente.andamentoScale;

import imago.sql.ElcoLoggerImpl;
import imago.sql.ElcoLoggerInterface;

import java.sql.ResultSet;

import generic.servletEngine;
import generic.statements.StatementFromFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class andamentoScaleFiltro extends servletEngine
{
	private HttpServletRequest request = null; 
	private final ElcoLoggerInterface logInterface = new ElcoLoggerImpl(andamentoScale.class);
	String reparto;
	
	public andamentoScaleFiltro(ServletContext pCont,HttpServletRequest pReq){
		super(pCont,pReq);
		this.request = pReq;
		this.bReparti = super.bReparti;

	}    

	@Override
	protected String getBody(){	

			String sOut = new String("");
			reparto = request.getParameter("reparto");
			sOut+="<DIV id='divMenu'>";
	    	sOut+= "<div id='divFiltroScale' class='wrapSezioneMenu'>";
	    	sOut+= "<span class='spnScale'>Scala: </span>";
	    	sOut+= "<select id='cmbScale' name='cmbScale'>"+getScale(reparto)+"</select>";
	    	sOut+= "</div>";
	    	sOut+= "<div id='refreshDati' class='HeaderButton'></div>";
	    	sOut+= "</div>";	    	
	    	sOut+= "<IFRAME  id='iframeAndamento' height=0  width=0 scrolling=no></IFRAME>";


		return sOut; 
	}
	
	private String getScale(String repartoIn){
		String sOut="";
		StatementFromFile sff = null;
		try {
			sff = new  StatementFromFile(this.hSessione);
			ResultSet rs = sff.executeQuery("andamentoScale.xml","getElencoScale",new String[]{repartoIn});
			while(rs.next()){
				sOut+="<OPTION VALUE="+rs.getString("FUNZIONE")+">"+rs.getString("INTESTAZIONE")+"</OPTION>";
			}
		} catch (Exception e) {
			sOut=e.getMessage();
			logInterface.error(e.getMessage(), e);
		}
		finally{
			sff.close();
		}		
		return sOut;
	}


	@Override
	protected String getTitle() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	protected String getBottomScript() {
		// TODO Auto-generated method stub
		return "";
	}

}