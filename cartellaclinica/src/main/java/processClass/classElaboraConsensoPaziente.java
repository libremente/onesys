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
/*
 * classElaboraInformazioni.java
 *
 * Created on 7 febbraio 2006, 11.36
 */

package processClass;

import imago.http.classDivHtmlObject;
import imagoView.ImagoViewException;

public class classElaboraConsensoPaziente implements imagoCreateWk.IprocessDataTable{

    private int indice_selezionato      = 0;
    private String valoreConsensoAnag   = "";
    private String valoreConsensoEvento = "";
    /** Creates a new instance of classElaboraInformazioni */
    public classElaboraConsensoPaziente()
    {
    }

    public imago.http.classColDataTable processColumn(imago.http.classColDataTable oggetto)
    {
    	classDivHtmlObject divConsensoAnag      = null;
        divConsensoAnag = new classDivHtmlObject("btnConsensoPaziente");
        if (!"CAMPO_NON_PRESENTE".equalsIgnoreCase(valoreConsensoAnag)){
            if ("".equalsIgnoreCase(valoreConsensoAnag) || valoreConsensoAnag==null){
                divConsensoAnag.addAttribute("class", "consensoPazienteAssente");
                divConsensoAnag.addAttribute("title", "consensoPazienteAssente");
                oggetto.appendSome(divConsensoAnag);
            }
            else{
                divConsensoAnag.addAttribute("class","0".equalsIgnoreCase(valoreConsensoAnag)?"consensoPazienteOK":"consensoPazienteKO");
                divConsensoAnag.addAttribute("title","0".equalsIgnoreCase(valoreConsensoAnag)?"consensoPazienteOK":"consensoPazienteKO");
                oggetto.appendSome(divConsensoAnag);
            }
        }

    	classDivHtmlObject divConsensoEvento    = null;
        divConsensoEvento = new classDivHtmlObject("btnConsensoEvento");        

        if (!"CAMPO_NON_PRESENTE".equalsIgnoreCase(valoreConsensoEvento)){       
            if ("".equalsIgnoreCase(valoreConsensoEvento) || valoreConsensoEvento==null){   
                divConsensoEvento.addAttribute("class", "consensoEventoAssente");
                oggetto.appendSome(divConsensoEvento);
            }else{
                divConsensoEvento.addAttribute("class","N".equalsIgnoreCase(valoreConsensoEvento)?"consensoEventoOK":"consensoEventoKO");
                divConsensoEvento.addAttribute("title","N".equalsIgnoreCase(valoreConsensoEvento)?"consensoEventoOK":"consensoEventoKO");
                oggetto.appendSome(divConsensoEvento);
            }
        }

        return oggetto;
    }
   
    public String processData(imagoView.Iview iview)
    {
    	indice_selezionato=iview.getRowIndex()-1;
    	try {
			this.valoreConsensoAnag = iview.getField("PRIVACY_PAZIENTE");
		} catch (ImagoViewException e) {
			this.valoreConsensoAnag = "CAMPO_NON_PRESENTE";           
		}

    	try {
			this.valoreConsensoEvento = iview.getField("CONSENSO_ESPRESSO");
        } catch (ImagoViewException e) {
            this.valoreConsensoEvento = "CAMPO_NON_PRESENTE"; 
        }
                
                
        return "";
       
    }

    public String processData(String str)
    {
        return "&nbsp;";
    }



}

