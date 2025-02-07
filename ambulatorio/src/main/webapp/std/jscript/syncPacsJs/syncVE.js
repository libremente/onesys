// JavaScript Document
// file Js che fa da "interfaccia"
// verso i pacs web
// AZIONI Possibili:
// SHOWSTUDY_STUDY
// SHOWSTUDY_PATIENT

var globalUtente 
var globalStudio 

function sendToWebPacs(azione, objUtente, objStudio, additionalParameters){

	var regEx = /\*/g;
	var strAccessionNumber = objStudio.ACCNUM.replace(regEx,"\\");
	var strAetitle = objStudio.AETITLE.split("*")[0];
	
	alert("QUI VE: " + azione);
	if (azione == "SHOWSTUDY_STUDY"){
		// prima cosa da fare � verificare dove andare a prendere le immagini
		// quindi devo interrogare l'archivio systemv per sapere l'aetitle
		// per far ci� chiamo checkNumImagesOnCareStreamEngine
		globalUtente = objUtente;
		globalStudio = objStudio;
//		alert("carico img");
		callRetrieveImgNumber (azione, strAccessionNumber, strAetitle);
	}
	else{
		// paziente
		performAction (azione, objUtente, objStudio, additionalParameters);
	}
}


//@deprecato ?
function openCheckAetile(azione, basePacsStudy){
	var regEx = /\*/g;

	strAccessionNumber = basePacsStudy.ACCNUM.replace(regEx,"\\");
	strAetitle = basePacsStudy.AETITLE.split("*")[0];
	datiDaMandare = "?aetitle_esame=" + strAetitle;
	datiDaMandare = datiDaMandare + "&accnum="+strAccessionNumber;
	datiDaMandare = datiDaMandare + "&azione="+azione;
	var wndCheckDbSystemV = window.open("checkNumImagesOnCareStream" + datiDaMandare,"wndCheckDbSystemV","top=60000,left=60000,width=600,height=600,statusbar=yes");
	if (wndCheckDbSystemV){
		wndCheckDbSystemV.focus();
	}
	else{
		wndCheckDbSystemV = window.open("checkNumImagesOnCareStream" + datiDaMandare,"wndCheckDbSystemV","top=60000,left=60000,width=600,height=600,statusbar=yes");
	}
	
}


// questa funzione viene chiamata come callback
// da checkNumImagesOnCareStreamEngine
//
// ******** ATTENZIONE ********
// verificare i corretti parametri da passare a VE
// ******** ATTENZIONE ********
// verificare differenze chiamate tra studio e paziente
// ******** ATTENZIONE ********
// l'integrazione tra ImagoWard e VE
// era stata sviluppata con un controllo sulla
// differenza di aetitle richiamati (consecutivamente)
// in tal caso si forzava (kill processo) la chiusura di VE
// NON lo implemento (forse) il baco � stato corretto
function performAction(azione,objUtente, objStudio, additionalParameter){
	
	var urlCareStreamVEToCall = "";
	var regEx = /\*/g;
	var strAccessionNumber="";	
	var patId = "";
	

//alert("performAction " + azione);
	urlCareStreamVEToCall = basePC.URL_VE;
	strAccessionNumber = objStudio.ACCNUM.replace(regEx,"\\");
	patId = objStudio.PATID;				
	urlCareStreamVEToCall = urlCareStreamVEToCall + "?user_name="+ objUtente.LOGIN;
	urlCareStreamVEToCall = urlCareStreamVEToCall + "&password="+ objUtente.PWD;
	switch (azione)
	{
		case "SHOWSTUDY_PATIENT":
			
			urlCareStreamVEToCall = urlCareStreamVEToCall + "&patient_id="+ patId;
			// in questo caso NON posso avere il node_name....
			break;
		case "SHOWSTUDY_STUDY":
			urlCareStreamVEToCall = urlCareStreamVEToCall + "&patient_id="+ patId;		
			urlCareStreamVEToCall = urlCareStreamVEToCall + "&accession_number="+ strAccessionNumber;
			urlCareStreamVEToCall = urlCareStreamVEToCall + "&server_name="+ objStudio.NODE_NAME;									
			break;
		default:
			return;
	}	
	alert(urlCareStreamVEToCall);
	var hndMP = window.open(urlCareStreamVEToCall,"wndMP","top =0, left=0,width=500,height=500");
	if (hndMP){
		hndMP.focus();
	}
	else{
		hndMP = window.open(urlCareStreamVEToCall,"wndMP","top =0, left=0,width=500,height=500");
	}
	
}


// ************************************************************
// ************************ AJAX ******************************
// ************************************************************
function callRetrieveImgNumber(azione, accessionNumber,aetitleTarget){

	if (accessionNumber==""){return;}
	try{
		ajaxRetrieveImgNumber.getAeTitleToCall(azione, accessionNumber,aetitleTarget,replyRetrieveImgNumber)
	}
	catch(e){
		alert("callRetrieveImgNumber: " + e.description)
	}
}

// funzione di callback
var replyRetrieveImgNumber = function (returnValue){
	
	var aetitleToCall = "";
	var numImg = "";
	var azione = "";
	var node_name = "";

	if (returnValue==""){return ;}
	alert(" replyRetrieveImgNumber returnValue: " + returnValue);
	try{
		azione= returnValue.toString().split('*')[0];
		numImg = returnValue.toString().split('*')[1];
		aetitleToCall = returnValue.toString().split('*')[2];
		node_name = returnValue.toString().split('*')[3];
		// rimappo info dello studio
		globalStudio.AETITLE = aetitleToCall;
		globalStudio.NODE_NAME = node_name;
		
		// **************
		performAction (azione, globalUtente, globalStudio, "")
		// azzero variabili
		globalUtente = null;
		globalStudio = null;		
	}
	catch(e){
		alert("replyRetrieveImgNumber - " + e.description);
	}
	
}
// ************************************************************