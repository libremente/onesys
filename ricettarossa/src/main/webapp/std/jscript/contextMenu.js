// script per menu contestuale
// NB la tabella del menu contestuale ha id fisso: contextualMenu
// NON pu� esistere + di un menu contestuale in una pagina


// NB IMPORTANTE ***************
// per fare la selezione automatica
// della riga devo essere sicuro di aver caricato al_selriga.js
// affinch� abbia a disp

var rigaSelezionataDalContextMenu=-1;
var idToolTip = "oLayNoMenu";
var _funzioneIllumina = null;

// funzione che crea il 
// div e lo appende al documento
function creaDivMenuVuoto(valore){
	if (valore===""){return;}
	var oDiv=document.createElement("DIV");
	oDiv.setAttribute("id", valore);
	oDiv.innerText = "Permissioni negate, nessun men� disponibile.";
	document.body.appendChild(oDiv);
	oDiv.style.visibility = "hidden";	
	oDiv.style.position = "absolute";
	oDiv.style.left = document.body.scrollLeft+event.clientX;
	oDiv.style.top = document.body.scrollTop+event.clientY;
	oDiv.style.visibility = "visible";	
	oDiv.className = "ContextNull";
}

// funzione che elimina
// un elemento specificato
// tramite ID
function eliminaDivVuoto(valore){
	if (valore===""){return;}
	var object = document.getElementById(valore);
	if (object){
		object.removeNode(true);
	}	
}

function hideContextMenu()
{
	var object = document.getElementById("contextualMenu");
	if (object){
		document.all.contextualMenu.style.visibility='hidden';
	}
	// se non esiste menu
	// ci sar� quello di accesso negato
	// lo elimino
	eliminaDivVuoto(idToolTip);
}

function MenuTxDx(){
	
	if(typeof document.all.contextualMenu == 'undefined' || !document.all.contextualMenu){
		return;
	}
	
	var altezzaDocumento = document.body.offsetHeight;
	var larghezzaDocumento = document.body.offsetWidth;
	if (rigaSelezionataDalContextMenu!=-1){
		// se esistono pi� di una riga selezionata non faccio nulla
		// altrimenti simulo il click sulla riga
		if ((vettore_indici_sel.length<2)){
			if (vettore_indici_sel[0]!=rigaSelezionataDalContextMenu){
				if(_funzioneIllumina === null){
					//illumina(parseInt(rigaSelezionataDalContextMenu));
					illumina(parseInt(rigaSelezionataDalContextMenu,10));
				}else{	
					eval(_funzioneIllumina);
				}
			}
		}
	}
	// posizionamento orizzontale
	if ((event.clientX + document.all.contextualMenu.scrollWidth)>larghezzaDocumento){
		document.all.contextualMenu.style.left = document.body.scrollLeft+event.clientX - document.all.contextualMenu.scrollWidth;
	}
	else{
		document.all.contextualMenu.style.left = document.body.scrollLeft+event.clientX;
	}
	// posizionamento verticale
	if ((event.clientY+document.all.contextualMenu.scrollHeight)>altezzaDocumento){
		if ((event.clientY-document.all.contextualMenu.scrollHeight)>0){
			document.all.contextualMenu.style.top = document.body.scrollTop+(event.clientY-document.all.contextualMenu.scrollHeight);
		}else{
			document.all.contextualMenu.style.top = document.body.scrollTop;
		}
	}
	else{
		document.all.contextualMenu.style.top = document.body.scrollTop+event.clientY;
	}
	document.all.contextualMenu.style.visibility = "visible";
	rigaSelezionataDalContextMenu=-1;
	try{
		if (baseUser.ABILITA_CONTEXT_MENU=="S"){
				return true;
		}
		else{
			return false;
		}

	}
	catch(e){
		return false;
	}
}


// funzione richiamata nel caso in cui
// non si abbia nessuna voce
// disponibile perch� non si hanno le permissioni
function mostraTooltipMenuVuoto()
{
	var bolEsisteDiv = false;
	// controllare se esiste  gi� il livello
	var object = document.getElementById(idToolTip);
	if (object){
		bolEsisteDiv = true;
	}
	if (bolEsisteDiv){
		eliminaDivVuoto(idToolTip);
	}
	creaDivMenuVuoto(idToolTip);	
	var objectNode = document.getElementById(idToolTip);
	if (objectNode){
		objectNode.style.display='block';
	}
	return false;
}

