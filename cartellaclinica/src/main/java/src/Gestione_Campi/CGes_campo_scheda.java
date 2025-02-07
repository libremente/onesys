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
 * CGes_campo_scheda.java
 *
 * Created on 11 settembre 2006, 12.53
 */

package src.Gestione_Campi;


import imago.a_sql.ISQLException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
/**
 *
 * @author  fabioc
 */
public class CGes_campo_scheda {
    private Statement stm=null;

    private Connection m_conConn;
    /**
     * Variabile contenente la classe <B>CDataBaseIX</B>
     */

    /**
     * Variabile privata contenente una lista di classi <B>CTabPerData</B>
     * corrispondenti ai record dell' ultima lettura eseguita in Data Base
     */
    private ArrayList m_tpData=new ArrayList();

    /**
     * Variabile contenete il valore del campo <B>IDEN</B> della tabella <B>TAB_PER</B>
     * @deprecated Utilizzare la struttura <B>CTabPerData</B> ed estrarre i dati con i metodi <B>getData</B>
     */
    public int m_iIDEN=-1;
    /**
     * Variabile contenete il valore del campo <B>DESCR</B> della tabella <B>TAB_PER</B>
     * @deprecated Utilizzare la struttura <B>CTabPerData</B> ed estrarre i dati con i metodi <B>getData</B>
     */
    public String m_strDESCR=new String("");
    /**
     * Variabile contenete il valore del campo <B>COD_DEC</B> della tabella <B>TAB_PER</B>
     * @deprecated Utilizzare la struttura <B>CTabPerData</B> ed estrarre i dati con i metodi <B>getData</B>
     */
    public String m_strCOD_DEC=new String("");
    /**
     * Variabile contenete il valore del campo <B>TIPO</B> della tabella <B>TAB_PER</B>
     * @deprecated Utilizzare la struttura <B>CTabPerData</B> ed estrarre i dati con i metodi <B>getData</B>
     */
    public String m_strTIPO=new String("");
    /**
     * Variabile contenete il valore del campo <B>TIPO_MED</B> della tabella <B>TAB_PER</B>
     * @deprecated Utilizzare la struttura <B>CTabPerData</B> ed estrarre i dati con i metodi <B>getData</B>
     */
    public String m_strTIPO_MED=new String("");

    /**
     * Metodo costruttore della classe CTabPer
     * @param Connessione Indica una Classe di connessione al Data Base precedentemente settata con
     * CDataBase
     */
    public CGes_campo_scheda(Connection Connessione) {
        this.m_conConn = Connessione;

    }


    public void loadData(String where) throws ISQLException {
        java.lang.String strSql=new java.lang.String("");

        this.clearData();
        try {

                //Esecuzione della query
                strSql="SELECT * FROM GES_CAMPO_SCHEDA WHERE " + where +"order by scheda";
                stm= this.m_conConn.createStatement();
                ResultSet rst;
                rst=stm.executeQuery(strSql);

                //Estrazione dei Dati
                while(rst.next()==true) {
                    this.fillStruct(rst);
                }
                rst.close();
                stm.close();

        }
        catch(SQLException SqlE) {
            throw new ISQLException(SqlE.getMessage());
        }
        catch(NullPointerException NpE) {
            throw new ISQLException("Null Pointer Exception");
        }
        finally
      {
            try{
                stm.close();
                stm=null;
            }
            catch (Exception e){}
      }

    }


    private void fillStruct(ResultSet rst) throws SQLException {
        try {
            /*ATTENZIONE !!! Codice da cancellare nelle prossime versioni
             *Utilizzato solo per la compatibilit� all' indietro
             */
//            this.m_iIDEN=rst.getInt("IDEN");
//            if(rst.wasNull()) this.m_iIDEN=-1;
//            this.m_strCOD_DEC=rst.getString("COD_DEC");
//            if(!rst.wasNull()) this.m_strCOD_DEC=this.m_strCOD_DEC.trim();
//            this.m_strDESCR=rst.getString("DESCR");
//            if(!rst.wasNull()) this.m_strDESCR=this.m_strDESCR.trim();
//            this.m_strTIPO=rst.getString("TIPO");
//            if(!rst.wasNull()) this.m_strTIPO=this.m_strTIPO.trim();
//            this.m_strTIPO_MED=rst.getString("TIPO_MED");
//            if(!rst.wasNull()) this.m_strTIPO_MED=this.m_strTIPO_MED.trim();
//
            //Nuovo codice
            CGes_campo_scheda_Data tp=new CGes_campo_scheda_Data();
            tp.m_iden=rst.getInt("IDEN");
            if(rst.wasNull()) tp.m_iden=-1;
            tp.m_scheda=rst.getString("SCHEDA");
            if(!rst.wasNull()) tp.m_scheda=tp.m_scheda.trim();
            tp.m_descrizione=rst.getString("DESCRIZIONE");
            if(!rst.wasNull()) tp.m_descrizione=tp.m_descrizione.trim();

            this.m_tpData.add(tp);
        }
        catch(SQLException sqlE) {
            throw sqlE;
        }
    }

    /**
     * Metodo privato della classe per la cancellazione delle variabili pubbliche
     */
    private void clearData() {

        //Nuovo Codice
        this.m_tpData.clear();
    }

    /**
     * Metodo pubblico per l' estrazione di un record preceentemente letto con il
     * metodo <B>loadData</B>; bisogna passare al metodo l' indice del record che si
     * vuole
     * @param index Indica l' indice del record precedentemente estratto con il metodo
     * <B>loadData</B>; se il parametro � minore di zero oppure � maggiore o uguale al
     * numero di record caricati con l' ultima operazion di lettura, allora il metodo
     * ritorner� il valore <B>null</B>
     * @return Ritorna la classe <B>CTabPerData</B> contenente le informazioni di un record
     */
    public CGes_campo_scheda_Data getData(int index) {
        CGes_campo_scheda_Data tpD=null;

        if(index>=0 && index<this.m_tpData.size()) {
            tpD=new CGes_campo_scheda_Data();
            tpD=(CGes_campo_scheda_Data) this.m_tpData.get(index);
        }

        return tpD;
    }

    /**
     * Metodo pubblico per l' estrazione di tutti i record preceentemente letti con il
     * metodo <B>loadData</B>; il metodo ritorna una classe <B>ArrayList</B>
     * @return Ritorna una lista di classi CTabPerData
     */
    public ArrayList getData() {
        return this.m_tpData;
    }
    public void executeQuery(String query) throws ISQLException {
        new java.lang.String("");

        this.clearData();
        try {

                //Esecuzione della query

                stm= this.m_conConn.createStatement();
                stm.executeQuery(query);

                //Estrazione dei Dati


                stm.close();
                m_conConn.commit();

        }
        catch(SQLException SqlE) {
            throw new ISQLException(SqlE.getMessage());
        }
        catch(NullPointerException NpE) {
            throw new ISQLException("Null Pointer Exception");
        }
        finally
          {
                try{
                    stm.close();
                    stm=null;
                }
                catch (Exception e){}
          }

    }

}
