var timeoutLoginDaSmartCard = 1000;
var timerLoginDaSmartCard;
var myobject;
var risposta;
var array_risposta
var pwd
// funzione che dovr� essere richiamata
// al momento del click sul pulsante
// questa funzione DEVE essere sempre implmentata
// e da usare a mo' di interfaccia
function getJsFunctionToCall(){

	var functionToCall = "javascript:getUserPwdFromCard()";
	return functionToCall;
}

// inizializza a posteriori il
// testo da metterenel pulsante
// questa funzione DEVE essere implementata
function initLabelButtonSmartCard(id){

	var objectNode;
	var testo ;

	objectNode = document.getElementById(id)
	if (objectNode){
		testo = ritornaJsMsg(id);
		objectNode.innerText =testo;
	}
}

// funzione che estrapola
// dalla DLL della smartcard
// l'utente e la pwd corretti
function getUserPwdFromCard(){

//myobject = new ActiveXObject("Acc_Smart_Card.GetUte_pwd")

	var ArrRisp
//	risposta=myobject.GetUte_pwd('http://192.168.1.20:8081/polarisTest/','N')
	dgst = new ActiveXObject("CCypher.Digest")
        risposta=dgst.GetSmartCardProperty(16)
	dwrSISS.AccessoSmartCard(risposta,getUserPwdFromCard2);
}
function getUserPwdFromCard2(myRisposta){
        ArrRisp=myRisposta.split('*')

	if (ArrRisp[0] == 'OK')
	{
		document.accesso.UserName.focus();
		document.accesso.UserName.value = ArrRisp[1];
		document.accesso.Password.focus();
		pwd=ArrRisp[2];
		// l'autenticazione deve essere ritardata per dare la
		// possibilit� ad ajax di fare i suoi controlli
		// opppure provo a chiamare startup

		timerLoginDaSmartCard = setTimeout('autentica()', timeoutLoginDaSmartCard);
	}
	else
	{
		alert(ArrRisp[1])
		}
}


// effettua "simulandolo"
// l'autenticazione
function autentica(){
	clearTimeout(timerLoginDaSmartCard);
	document.accesso.Password.value = pwd;
	autenticaLogin();
}

