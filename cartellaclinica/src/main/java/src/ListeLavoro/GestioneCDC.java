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


package src.ListeLavoro;

import imago.a_sql.CCentriDiCosto;
import imago.a_sql.CLogError;
import imago.a_sql.CWebCdc;
import imago.a_sql.CWebCdcData;
import imago.a_util.CContextParam;
import imago.http.classColDataTable;
import imago.http.classDataTable;
import imago.http.classDivButton;
import imago.http.classDivHtmlObject;
import imago.http.classFormHtmlObject;
import imago.http.classInputHtmlObject;
import imago.http.classLabelHtmlObject;
import imago.http.classRowDataTable;
import imago.http.classTabHeaderFooter;
import imago.http.classTypeInputHtmlObject;
import imago.http.baseClass.baseUser;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

public class GestioneCDC {
    private CLogError           f_log;
    private baseUser            myUser=null;
    private HttpServletRequest  myRequest=null;
    private String              CentriSelezionati="";
    private String              SelCDC="";
    private int                 numberCDC=0;
    public GestioneCDC(baseUser logged_user,HttpServletRequest request,CContextParam myConteParam){
        myUser=logged_user;
        myRequest=request;
        try {
    f_log=new CLogError(logged_user.db.getWebConnection(), request,  "SERVLETLL", logged_user.login);
     f_log.setFileName("GestioneCDC");
     f_log.setClassName("src.ListeLavoro.GestioneCDC");
        } catch (Exception ex) { }

    }

    public void creaCDC(classFormHtmlObject form){
        try {
        leggiDatiInput(myRequest);}
         catch (Exception e) {f_log.writeLog("Errore nella Lettura del Request",CLogError.LOG_ERROR);}
     ArrayList elencoCDC=null;
        try{
            /*Se WEB.cdc_utente = 'S' => visualizzo in CDC nei filtri solo i cdc di WEB_CDC dell'utente*/
                if (myUser.CDC_UTENTE.equalsIgnoreCase("S")) {
                    CWebCdc cdc = new CWebCdc(myUser.db.getWebConnection());
                    boolean attivo = false;
                    if(myUser.filtri_voci_inattive != null && myUser.filtri_voci_inattive.equalsIgnoreCase("N"))
                        attivo = true;
                    cdc.loadData(myUser.login, "REPARTO", true, attivo);
                  elencoCDC=cdc.getData();
                }
                else
                {
                  CCentriDiCosto cdc = new CCentriDiCosto(myUser.db.getDataConnection());
                  boolean attivo = false;
                  if (myUser.filtri_voci_inattive != null && myUser.filtri_voci_inattive.equalsIgnoreCase("N"))
                      attivo = true;
                  cdc.loadData("cod_cdc", true, attivo);
                 elencoCDC=cdc.getData();
                }
        }
        catch(Exception ex){
        f_log.writeLog("Errore Connessione tabella cdc o myUser non pervenuto",CLogError.LOG_ERROR);
        }


        int max=0;
        int numeroCDC=elencoCDC.size();
        numberCDC=numeroCDC;
        int numeroCDCselezionati=0;
        classDivHtmlObject div = new classDivHtmlObject("div_cdc","display='block'");
        ArrayList mieColonne = new ArrayList();
        ArrayList mieRighe = new ArrayList();
        try {
        if (SelCDC==null || SelCDC.equalsIgnoreCase("")) {
        for (int Contatore=0;Contatore<=numeroCDC-1;Contatore=Contatore+3){

                if(numeroCDC-Contatore>=3)
                { max=2;}
                else
                {max= numeroCDC-Contatore-1;}
                for (int i=0; i<=max;i++){
                    String Centro="";
                    if (myUser.CDC_UTENTE.equalsIgnoreCase("S"))
                    {
                        CWebCdcData WebCentro = ((CWebCdcData) elencoCDC.get(Contatore + i));
                        Centro=WebCentro.m_strREPARTO;
                    }
                      else
                      {
                                elencoCDC.get(Contatore+i);
                      }


                    if (CentriSelezionati.equalsIgnoreCase(""))
                    {CentriSelezionati=Centro;}
                    else
                    {CentriSelezionati=CentriSelezionati+","+Centro;}
                    classInputHtmlObject input= new classInputHtmlObject(classTypeInputHtmlObject.typeCHECKBOX,"campoCDC"+Contatore+i,Centro);
                    input.addEvent("OnClick", "javascript:applicaLLCDC('"+numeroCDC+"');");
                    input.setChecked(true);
                    input.appendSome(Centro);
                    classColDataTable colonne=new classColDataTable("TD","",input);
                    colonne.addAttribute("class", "classTdLabelSta");
                    mieColonne.add(colonne);

                }
                 if (max<2)
                 {for (int x=max; x<2; x++)
                 {classColDataTable colovuo=new classColDataTable("TD","","");
                    colovuo.addAttribute("class", "classTdLabelSta");
                    mieColonne.add(colovuo); }}
                numeroCDCselezionati=numeroCDC;
                classRowDataTable righe=new classRowDataTable("",mieColonne);
                mieRighe.add(righe);
                mieColonne.clear();
            }}

            else
            {
                String[] ArrayCDC=null;

             ArrayCDC=SelCDC.split(",");
             for (int Contatore=0;Contatore<=numeroCDC-1;Contatore=Contatore+3){
             if(numeroCDC-Contatore>=3)
             { max=2;}
             else
             {max= numeroCDC-Contatore-1;}
             for (int i=0; i<=max;i++){
                 String Centro="";
                 if (myUser.CDC_UTENTE.equalsIgnoreCase("S"))
                    {
                        CWebCdcData WebCentro = ((CWebCdcData) elencoCDC.get(Contatore + i));
                        Centro=WebCentro.m_strREPARTO;
                    }
                      else
                      {
                                elencoCDC.get(Contatore+i);
                      }

                 classInputHtmlObject input= new classInputHtmlObject(classTypeInputHtmlObject.typeCHECKBOX,"campoCDC"+(Contatore+i),Centro);
                 input.addEvent("OnClick", "javascript:applicaLLCDC('"+numeroCDC+"');");

                 if (ArrayCDC[Contatore+i].equalsIgnoreCase("0"))
                 {input.setChecked(false);}
                 else
                 {      numeroCDCselezionati=numeroCDCselezionati+1;
                        input.setChecked(true);
                        if (CentriSelezionati.equalsIgnoreCase(""))
                                {CentriSelezionati=Centro;}
                        else
                            {CentriSelezionati=CentriSelezionati+","+Centro;}

                 }

                 input.appendSome(Centro);
                 classColDataTable colonne=new classColDataTable("TD","",input);
                 colonne.addAttribute("class", "classTdLabelSta");
                 mieColonne.add(colonne);
             }
              if (max<2)
                 {for (int x=max; x<2; x++)
                 {classColDataTable colovuo=new classColDataTable("TD","","");
                    colovuo.addAttribute("class", "classTdLabelSta");
                    mieColonne.add(colovuo); }}
             classRowDataTable righe=new classRowDataTable("",mieColonne);
             mieRighe.add(righe);
             mieColonne.clear();

             }}}
        catch (Exception e) {f_log.writeLog("Errore Creando la Grafica dei CDC",CLogError.LOG_ERROR);}
        try {
        classDataTable tabellaCDC= new classDataTable("classDataEntryTable",mieRighe);
        mieRighe.clear();
        div.appendSome(tabellaCDC);
        classLabelHtmlObject label_titolo=new classLabelHtmlObject("","","titoloCDC");
        label_titolo.addAttribute("title",CentriSelezionati);
        classTabHeaderFooter header = new classTabHeaderFooter(""+label_titolo+numeroCDCselezionati);
        classDivButton apri_chiudi= new classDivButton("", "pulsante", "javascript:ApriChiudi('div_cdc',"+numeroCDC+");", "apri_chiudiCDC","");
        header.addColumn("classButtonHeader", apri_chiudi.toString());
        classTabHeaderFooter footer = new classTabHeaderFooter(" ");
        footer.setClasses("classTabHeader","classTabFooterSx","classTabHeaderMiddle","classTabFooterDx");
        classDivButton button_sel = new classDivButton("", "pulsante", "javascript:getSeldeselCDC(true,0,"+numeroCDC+");","sel_all_CDC","");
        footer.addColumn("classButtonHeader", button_sel.toString());
        classDivButton button_desel = new classDivButton("", "pulsante", "javascript:getSeldeselCDC(false,0,"+numeroCDC+");","desel_all_CDC","");
        footer.addColumn("classButtonHeader", button_desel.toString());
        form.appendSome(header.toString());
        div.appendSome(footer.toString());
        form.appendSome(div);
        classInputHtmlObject CDC= new classInputHtmlObject(classTypeInputHtmlObject.typeHIDDEN,"CdcTOT","","");
        CDC.addAttribute("value",Integer.toString(numeroCDC));
        form.appendSome(CDC.toString());
        classInputHtmlObject CDCSELEZIONATI= new classInputHtmlObject(classTypeInputHtmlObject.typeHIDDEN,"ArrCDC","","");
        form.appendSome(CDCSELEZIONATI.toString());}
         catch (Exception e) {f_log.writeLog("Errore Appendendo i CDC alla Pagina",CLogError.LOG_ERROR);}
    }
    public String stringaCDC(){
        return CentriSelezionati;
    }
    public int getnumeroCDC(){
    return numberCDC;
    }

    private void leggiDatiInput(HttpServletRequest myInputRequest)  {


        SelCDC=myInputRequest.getParameter("ArrCDC");



    }
}



