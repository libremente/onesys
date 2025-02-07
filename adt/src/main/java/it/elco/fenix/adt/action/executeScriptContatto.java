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
package it.elco.fenix.adt.action;

import it.elco.core.Entry;
import it.elco.core.converters.ReaderFactory;
import it.elco.core.data.RpcResponse;
import it.elco.fenix.adt.exception.RpcContattoException;
import it.elco.listener.ElcoContextInfo;
import it.elco.scripting.ScriptAction;
import it.elco.scripting.Type;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Map;

import javax.script.ScriptException;


/**
 *
 * @author francescog
 */
public class executeScriptContatto extends RpcContattoAction{

    private final String scriptType, path;
    
    public executeScriptContatto(String scriptType, String path){
        this.scriptType = scriptType;
        this.path = path;
    }

    public executeScriptContatto(String path){
        this(Type.GROOVY.name(), path);
    }


    @Override
    public RpcResponse execute() throws RpcContattoException {
    	
        try {
            Reader scriptReader = ReaderFactory.fromInputStream(ElcoContextInfo.getContext().getResourceAsStream(path));
            Map<String, Object> scriptResult = new ScriptAction(Type.valueOf(scriptType), scriptReader, new Entry<>("contatto", contatto), new Entry<>("method", method), new Entry<>("body", body),new Entry<>("contextInfo", ctx)).execute();
            
            return new RpcResponse((String)scriptResult.get("message"), Boolean.parseBoolean(String.valueOf(scriptResult.get("success"))), (String)scriptResult.get("message"));
        } catch (ScriptException ex) {            
            throw new RpcContattoException(ex); 
        }               
    }
    
}
