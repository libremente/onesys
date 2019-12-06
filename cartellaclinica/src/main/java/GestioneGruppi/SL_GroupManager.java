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
 * SL_GroupManager.java
 *
 * Created on 10 giugno 2005, 15.53
 */

package GestioneGruppi;

import imago.a_sql.CDataBaseIX;
import imago.a_sql.CLogError;
import imago.a_util.CClientParam;
import imago.a_util.CContextParam;
import imago.a_util.CUtility;
import imago.http.baseClass.baseUser;
import imago.util.CVarContextSession;
import imago.winbuild.CSessionError;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author  MAZZORAN Andrea
 * @version
 */
public class SL_GroupManager extends HttpServlet
{
    private ServletContext context = null;
    private HttpSession session = null;

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        context = config.getServletContext();

    }

    /** Destroys the servlet.
     */
    public void destroy()
    {

    }

    private baseUser getVarContextSession()
    {
        CVarContextSession var_cs = new CVarContextSession(this.session, this.context);
        return(var_cs.getBaseUser());
    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        new String("");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        CGroupManager groupMan = null;
        CLogError log = null;
        new CClientParam(request);

        this.session = request.getSession(false);
        if(this.session != null)
        {
            baseUser logged_user = this.getVarContextSession();
            CContextParam cp = new CContextParam(this);
            try
            {
                CDataBaseIX dbx = new CDataBaseIX(logged_user, Integer.parseInt(cp.getParam("TIPODB")));

                dbx.connect();

                //Impostazione Log
                log = new CLogError(dbx.m_dbWeb.getConnection(), request, "SL_GroupManager", logged_user.login);
                log.setClassName("imago.GesUtilita.gesUtenti.SL_GroupManager");
                log.setFileName("SL_GroupManager.java");

                log.writeLog("Inizio della costruzione della pagina HTML per SL_GroupManager", CLogError.LOG_DEBUG);

                CUtility.datiFormLog("SL_GroupManager", request, log);

                groupMan = new CGroupManager(log, logged_user, session, context, request);
                out.println(groupMan.creaWorklist());
            }
            catch(Exception e)
            {
                e.printStackTrace();
                log.writeError("GestioneGruppi.SL_GroupManager.processRequest:creazione worklist " + e.getMessage());
            }
        }
        else
        {
            out.println(CSessionError.buildHTML());
        }
        out.close();
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** Returns a short description of the servlet.
     */
    public String getServletInfo()
    {
        return "Short description";
    }

}