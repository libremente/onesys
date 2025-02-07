var $esenzioni;

$(function() {
	NS_SALVA_ACCERTAMENTI.init();
	NS_SALVA_ACCERTAMENTI.buildAccertamentiTable();
	NS_SALVA_ACCERTAMENTI.setEvents();
	NS_SALVA_ACCERTAMENTI.checkPrivacy();
});

var NS_SALVA_ACCERTAMENTI = {
		
		objSalvataggio : null,
		
		//controlloStampa:false,
		
		selectedLine: '',
		
		objNote : {},
		
		init: function(){
			
			home.RICETTA_ACCERTAMENTI.setNScheda($("#N_SCHEDA").val());
			
			home.PRESALVATAGGIO_ACCERTAMENTI = this;
			
			NS_SALVA_ACCERTAMENTI.objSalvataggio = home.RICETTA_ACCERTAMENTI.getObjSalvataggio();
			NS_SALVA_ACCERTAMENTI.setDiagnosi();
			NS_SALVA_ACCERTAMENTI.setNota();
			NS_SALVA_ACCERTAMENTI.checkDoppi();
			
			NS_SALVA_ACCERTAMENTI.originalDiagnosi = $('#lblDiagnosi').text();
			NS_SALVA_ACCERTAMENTI.updateDiagnosiChars();
			
			radUrgenza.selectByValue("P");
			
			$("#txtSuggerito_da, #txtSuggerito_struttura").closest("tr").hide();
			
			$(".butSalvataggio").addClass("butVerde");
			
			if (!home.MMG_CHECK.isMedico()) {
				$(".butSalvaNoPrint").hide();
			}
			
			$("#lblPOCI").parent("tr").hide();
		},
		
		setEvents: function() {
			
			$esenzioni = $(".classEsenzione").contextMenu(menuEsenzioni, {
				openSubMenuEvent : "mousedown",
				openInfoEvent : "mousedown"
			});
			
			$(".butChiudi").off("click");
			$(".butChiudi").on("click", function(){
				
				home.NS_MMG.confirm(traduzione.lblChiudiScheda, function() {

					NS_FENIX_SCHEDA.chiudi();
				});
			});
			
			$('.esenzione input').on("dblclick",function(){
				selectedLine = $(this).parent("td").parent("tr").attr("title");
				home.NS_MMG.apri("MMG_SCELTA_ESENZIONI&PROVENIENZA=PRESALVATAGGIO_ACCERTAMENTI&CAMPO_VALUE=");
			}).on("mousedown", function(e){ 
				if (e.button == '2') {
					$esenzioni.test(e, this);
				}
			}); 
			
			$("body").on("keyup",function(e){
				
				if(!home.MMG_CHECK.isDead()){return;}
				
				//tasto f2
				if(e.keyCode == "113"){
					e.preventDefault();
					$(".butSalvataggio").trigger("click");
				}
				//tasto esc
				else if(e.keyCode == "27"){
					e.preventDefault();
					$(".butChiudi").trigger("click");
				}
			});
			
			
			$('#txtDiagnosi').on('keyup blur', NS_SALVA_ACCERTAMENTI.updateDiagnosiChars );
			
			$(".butSalvataggio").on("click",function(){
				NS_SALVA_ACCERTAMENTI.salva("S");
			});
			
			$(".butSalvaNoPrint").on("click",function(){
				NS_SALVA_ACCERTAMENTI.salva("N");
			});
			
			$("#butInserisci").on("click", function(){
				
				$("#txtDiagnosi").val($("#txtDiagnosi").val() + "Proc. di prevenzione");
				$("#txtDiagnosi").trigger('blur');
			});
			
			$(document).on("click",".delAcc", NS_SALVA_ACCERTAMENTI.cancellaAccertamento ) ;
			
			$('#radSuggerita').on("change", function(){
				
				if($("#h-radSuggerita").val() == 'S'){
					
					$("#txtSuggerito_da, #txtSuggerito_struttura").closest("tr").show();
				
				}else{
					
					$("#txtSuggerito_da, #txtSuggerito_struttura").closest("tr").hide();
					$("#txtSuggerito_da, #txtSuggerito_struttura").val('');
					$("#h-txtSuggerito_da").val('');
				}
			});
			
			NS_MMG_UTILITY.infoPopup('Dalla pagina 2, capitolo 2 della  <a href="#" onclick="ACCERTAMENTI_COMMON.apriCircolareDecreto()">circolare</a>: "Durante la fase di sperimentazione e monitoraggio del decreto in esame, i medici prescrittori possono non applicare le condizioni di appropriatezza quando le prestazioni debbano essere erogate a pazienti oncologici, cronici o invalidi."<br><br>\
Selezionando una delle scelte proposte, la selezione delle condizioni di erogabilità diventerà non obbligatoria (sebbene possibile) per la prescrizione su ricetta SSN, e verrà aggiunta nel quesito diagnostico la dicitura "Circolare MdS Prot. 3012 - Punto 2".<br><br>\
Se si è selezionata una scelta per errore e non si vuole effettuare nessuna scelta, cliccare nuovamente per deselezionarla e tornare a rendere obbligatorie le condizioni di erogabilità.', {
				title: "Circolare 3012 del 25/03/2016 del Ministero della Salute",
				width: "40%"
			}, $("#lblPOCI"));
                        
                        $('#radPOCI').on("change", function(){
                            
                            home.NOTIFICA.warning({
                                message:traduzione.lblInfoCircolare,
                                title: "Attenzione",
                                timeout:30
                            });
                        });
		},
		
		buildAccertamentiTable:function() {
			
			$("#cmbUrgenza").closest("tr").hide();
			
			var vTr, vDia,vUrg,vAcc,vEsenz,vNotePrest;
			var vTable = $("#fldRiepilogo table");
			vTable.addClass("tableRiepilogo");
			
			//comincio a costruire la tabella di riepilogo degli accertamenti
			vTable.append("<tr><td colSpan = 8><button type='button' class='btn' id='butDelDiagnosi'><i class='icon-cancel-1'><i><span>"+traduzione.butDelDiagnosi+"</span></button></td></tr>");
			vTable.append("<tr class='head'><th>"
					+ traduzione.lblCancella + "</th><th>"
					+ traduzione.accertamento + "</th><th>"
					+ traduzione.diagnosi + "</th><th>"
					+ traduzione.urgenza + "</th><th>"
					+ traduzione.esenzione + "</th><th class='sedute'>" 
					+ traduzione.sedute + "</th><th class='note'>" 
					+ traduzione.notePrestazioni + "</th></tr>");
			
			var show_sedute = false;
			
			var ciclo_acc = $.each(NS_SALVA_ACCERTAMENTI.objSalvataggio.accertamento, function(idx, obj) {
				var sedute;
				vUrg = $("select#cmbUrgenza").clone().removeAttr("id");
				vTr = $("<tr/>",{	"codice":obj.cod_accertamento, "class":"dati", "title":idx, "nota_prestazione":""});
				vTable.append(
						vTr
						.append(
						$("<td/>")
							.append(
								$('<i>', { 'class' : 'delAcc icon-cancel-1' })
						))
						.append(
						$("<td/>",{"class":"accertamento"})
							.append(
								vAcc = $("<input/>", {"value":obj.accertamento, "title": obj.accertamento})
						))
						.append(
						$("<td/>",{"class":"diagnosi"})
							.append(NS_SALVA_ACCERTAMENTI.getComboRao(obj.cod_accertamento.split("@")[0],vUrg,obj.cod_situazione_clinica))
							.append(
								vDia = $("<input/>", {"value":obj.diagnosi}))
						)
						.append(
						$("<td/>",{"class":"urgenza"})
							.append(
								vUrg
						))
						.append(
						$("<td/>",{"class":"esenzione"})
							.append(
							vEsenz = $("<input/>", {
								"value":home.RICETTA_ACCERTAMENTI.getEsenzioneFromIdx(obj.index),
								"title":home.RICETTA_ACCERTAMENTI.getEsenzioneFromIdx(obj.index),
								"data-value":obj.cod_esenzione,
								"style": "width: 5em;"
							})
						))
						.append(
							sedute = $("<td/>",{
								"class":"sedute",
								"style": "width: 2em;"
							})
						)
						.append(
							note  = $("<td/>",{
								"class":"note",
								"style": "width: 2em;"
							}).append(vNotePrest = NS_SALVA_ACCERTAMENTI.getNotePrestazione(obj, vTr))
						)
				);
				
				if (obj.cicliche >= "1" && obj.num_sedute >= "1") {
					var valore;
					var title;
					if (typeof obj.note != "undefined" && obj.note != "") {
						valore = obj.note;
						title = obj.note;
					} else {
						valore = obj.num_sedute;
						title = obj.num_sedute + " sedute";
					}
					sedute.append(
						$("<input/>", {
							"value": valore,
							"title": title,
							"data-value": 10
						})
					);
					show_sedute = true;
				}

				$("#butDelDiagnosi").on("click", function(){
					NS_SALVA_ACCERTAMENTI.cancellaDiagnosi();
				});
				
				if(obj.suggerita == "S" && radSuggerita.val() != "S") {
					$("#radSuggerita_S").trigger("click");
					SALVATAGGIO.getDatiSuggerita("ACCERTAMENTI", obj.iden);
				}
			});
			
			$.when(ciclo_acc).then(function() {
				if (show_sedute) {
					$(".sedute").show();
				}
			});
		},
		
		cancellaAccertamento: function() {
			var offset = 2; // header row
			if ($("#fldRiepilogo tr.dati").length < 2 ) {
				home.NOTIFICA.warning({message:"Impossibile cancellare tutti gli accertamenti", title: "Attenzione"});
				return;
			}
			var tr = $(this).closest("tr");
			var idx = tr.index() - offset;

			NS_SALVA_ACCERTAMENTI.objSalvataggio.accertamento.splice(idx,1);
			tr.remove();
		},
		
		cancellaDiagnosi:function(){
			
			var dialog = home.$.dialog(
				traduzione.lblDialogDelDiagnosi,
				{
					'id'				: "dialogDelDiagnosi",
					'title'				: traduzione.lblDelDiagnosi,
					'width'				: 350,
					'showBtnClose'		: false,
					'modal' 			: true,
					'movable' 			: true,
					'ESCandClose'		: true,
					'created'			: function(){ $('.dialog').focus(); },
					'buttons'			: [ {
						label : traduzione.lblOk,
						action : function(ctx) {
							$("td.diagnosi input").val("");
							home.$.dialog.hide();
						},
						"keycode"	: "13"
					}, {
						label : traduzione.lblAnnulla,
						action : function(ctx) {
							home.$.dialog.hide();
						}
					} ]
			});;
		},

		checkDoppi: function() {
			var arAcc = NS_SALVA_ACCERTAMENTI.objSalvataggio.accertamento;
			
			for (var i = 0; i < arAcc.length; i++ ) {
				var test = arAcc[i];
				for (var j = 0; j < arAcc.length; j++ ) {
					if (j != i && arAcc[i].cod_accertamento == arAcc[j].cod_accertamento && arAcc[i].accertamento == arAcc[j].accertamento)
						return home.NOTIFICA.warning({message:"Accertamenti uguali inseriti piu' volte", title: "Attenzione", timeout: 5});					
				}
			}
		},
		
		checkPrivacy:function(){
			
			NS_MMG_UTILITY.checkPermessoSpecialista([$("#txtDiagnosi")]);
		},

		context_menu : {
			
			applicaEsenzioni: function(val, descr){
				
				$(".esenzione input").each(function() {
					$(this).attr("data-value", val);
					$(this).attr("title", descr);
					$(this).attr("value", descr);
				});
			},
			
			cancellaEsenzioni:function(){

				$(".esenzione input").each(function() {
					$(this).attr("data-value", '');
					$(this).attr("title", '');
					$(this).attr("value", '');
				});
			}
		},
		
		getComboRao : function(pCodice, pUrg, pCodSituazioneClinica) {
			
			var div = null;
			dwr.engine.setAsync(false);
			toolKitDB.getResultDatasource('RICETTE.SITUAZIONI_CLINICHE','MMG_DATI',{cod_prestazione:pCodice},null,function(resp){
				if (resp.length>0) {
					cmb = $("<select/>");
					cmb.append($("<option/>"));

					$.each(resp,function(k,v){
						var opt = $("<option/>", {value:v["VALORE"]}).text(v["DESCR"]);
						if (v["VALORE"].split("@")[0]==pCodSituazioneClinica) {
							opt.attr("selected",true);
							pUrg.find("option[value="+v["VALORE"].split("@")[1]+"]").attr("selected",true);
						}
						cmb.append(opt);
					});
					pUrg.attr("disabled", true);
					cmb.on("change", function() {
						/*var inputDia;
						if((inputDia = $(this).closest("td").find("input")).val()==''){
							inputDia.val($(this).find("option:selected").text());
						}*/
						pUrg.find("option:first").attr("selected",true);
						pUrg.find("option[value="+$(this).find("option:selected").val().split('@')[1]+"]").attr("selected",true);
					});
					div = $("<div/>").append(
							$("<label/>").text("Situazione clinica RAO: "))
							.append(cmb);
				};
			});
			
			dwr.engine.setAsync(true);	
			return div;  
		},
		
		getNotePrestazione:function(obj, vTr){
			var vCodAccertamento = obj.cod_accertamento;

			//alert("il codice accertamento è " + vCodAccertamento);
			function setDiv(pTestoNota, pCondizErogabilita, pCodPrestazione, pTipoCondizione, pIdDettaglio){
				
				var id_opt = pTipoCondizione + "-" + pCondizErogabilita + "-" + pIdDettaglio;
				
				var titleInformazione = pTipoCondizione == 'CON' ? 'Condizione di erogabilità' : 'Indicazione di appropriatezza prescrittiva' ;
				var classe = pTipoCondizione == 'CON' ? 'clsCondizione' : 'clsIndicazione';
				var div = $("<div title='"+titleInformazione+"'/>").html("<a>"+pTestoNota+"</a>");
				if (pCondizErogabilita != "B") {
					var i = $("<i class='icon-info-circled btnNota' cod_prestazione='"+pCodPrestazione+"'></i>");
					i.on("click",function(e){
						e.preventDefault();
						e.stopImmediatePropagation();
						ACCERTAMENTI_COMMON.getInfoNote($(this).attr("cod_prestazione"));
					});
					div.append(i);
				}
				div.addClass("notePrestazioni "+classe);
				div.attr({"id_nota" : pCondizErogabilita,"cod_prestazione" : pCodPrestazione, "tipo_nota" : pTipoCondizione});
				div.attr({"id_dett": pIdDettaglio, "id_opt": id_opt});
				div.on("click", function(){
					NS_SALVA_ACCERTAMENTI.setSelNota($(this));
				});
				
				if (id_opt == obj.id_nota_appropriatezza || (pCondizErogabilita == "B" && obj.forzatura == "B")) {
					var icon = $( document.createElement('i') ).addClass('icon-ok').css({"color":"green","padding-left":"5px"});
					div.addClass("selected").append(icon);
				}
				return div;
			}

			var divWrapper = $("<div/>");
			divWrapper.attr("id","wrapperNote");	
			divWrapper.attr("cod_prestazione", vCodAccertamento);
			
			if (LIB.isValid(obj.appropriatezza) && obj.appropriatezza != "") {
			
				home.$.NS_DB.getTool({_logger : home.logger}).select({
					id:'RICETTE.GET_NOTE_PRESTAZIONI_OPT',
					parameter:
					{
						v_cod_accertamento		: { v : vCodAccertamento, t : 'V'}
					}
				}).done( function(resp) {
					var cnt = 0;
					var note_dm_1996 = "";
					var allow_forzatura_bianca = true;
					$.each(resp.result,function(k,v){
						cnt++;
						var dettaglio;
						if (v["ID_DETT"] == "0") {
							dettaglio = "";
						} else {
							dettaglio = "-" + v["ID_DETT"];
						}
						var testoNota = v["ID_NOTA"] + dettaglio;
						var codiceNota = v["ID_NOTA"];
						
						if (v['TIPO_NOTA'] == "CON") {
							allow_forzatura_bianca = false;
						}
						divWrapper.append(setDiv(testoNota, codiceNota, vCodAccertamento, v['TIPO_NOTA'], v["ID_DETT"] ));

						note_dm_1996 = v["NOTE_DM_1996"];
					});
					if (cnt>0) {
						if (allow_forzatura_bianca) {
							divWrapper.append(setDiv("R. Bianca", "B", vCodAccertamento, "IND", "0"));
						}
						if (note_dm_1996 == "") {
							//coloro di giallo la riga
							vTr.addClass("rigaNote");
							$("#lblPOCI").parent("tr").addClass("rigaNote").show();
							if (!NS_SALVA_ACCERTAMENTI.flag_appropriatezza_avviso) {
								NS_SALVA_ACCERTAMENTI.flag_appropriatezza_avviso = true;
								home.NOTIFICA.warning({
									title: "Attenzione",
									message: "Valutare l'applicabilità delle note esaminando le informazioni relative, e selezionare quelle appropriate prima di procedere alla prescrizione",
									timeout: 15
								});
							}
						} else {
							//coloro di rosso la riga
							vTr.addClass("rigaNoteDM1996");
							if (!NS_SALVA_ACCERTAMENTI.flag_appropriatezza_specialisti) {
								NS_SALVA_ACCERTAMENTI.flag_appropriatezza_specialisti = true;
								home.NOTIFICA.error({
									title: "Attenzione",
									message: "Si stanno prescrivendo accertamenti prescrivibili solo da specialisti",
									timeout: 30
								});
							}
						}
					}
				} );
			}
			return divWrapper;
		},
		
		flag_appropriatezza_avviso: false,
		flag_appropriatezza_specialisti: false,
		
		salva: function(stampa_ricetta) {
			
			/*
			if(NS_SALVA_ACCERTAMENTI.controlloStampa){
				return;
			}
			
			NS_SALVA_ACCERTAMENTI.controlloStampa = true;
			*/
			
			NS_SALVA_ACCERTAMENTI.objSalvataggio.urgenza 				= radUrgenza.val();
			NS_SALVA_ACCERTAMENTI.objSalvataggio.diagnosi 				= NS_MMG_UTILITY.stripHTML($.trim($("#txtDiagnosi").val()));
			NS_SALVA_ACCERTAMENTI.objSalvataggio.ricovero 				= radRicovero.val();
			NS_SALVA_ACCERTAMENTI.objSalvataggio.suggerita 				= radSuggerita.val();
			NS_SALVA_ACCERTAMENTI.objSalvataggio.suggerita_da 			= ($("#h-txtSuggerito_da").length > 0) ? $("#h-txtSuggerito_da").val() : "";
			NS_SALVA_ACCERTAMENTI.objSalvataggio.suggerita_struttura 	= ($("#txtSuggerito_struttura").length > 0) ? $("#txtSuggerito_struttura").val() : "";
			NS_SALVA_ACCERTAMENTI.objSalvataggio.altro 					= radAltro.val();
			NS_SALVA_ACCERTAMENTI.objSalvataggio.poci					= typeof $("#h-radPOCI").val() != "undefined" ? $("#h-radPOCI").val() : "";
			
			var chkDiaDef 		= (NS_SALVA_ACCERTAMENTI.objSalvataggio.diagnosi == ''); 
			var chkUrgDef 		= (NS_SALVA_ACCERTAMENTI.objSalvataggio.urgenza == '');
			var accertamenti 	= NS_SALVA_ACCERTAMENTI.objSalvataggio.accertamento;
			var errMsg 			= '';
			
			$("#fldRiepilogo tr.dati").each(function(i) {
				
				var opt;
				var accertamento = accertamenti[i];
				var $this = $(this);
				accertamento.accertamento = NS_MMG_UTILITY.stripHTML($this.find("td.accertamento input").val());
				accertamento.urgenza = $this.find("td.urgenza option:selected").val()!='' ? $this.find("td.urgenza option:selected").val() : radUrgenza.val();
				accertamento.diagnosi = $.trim($this.find("td.diagnosi input").val()) !='' ? NS_MMG_UTILITY.stripHTML($.trim($this.find("td.diagnosi input").val())) : NS_SALVA_ACCERTAMENTI.objSalvataggio.diagnosi;

				if ((opt = $this.find("td.diagnosi select option:selected")).length>0) {
					accertamento.situazioneClinica=opt.val().split("@")[0];
				}else{
					accertamento.situazioneClinica = '';
				}
//				obj.farmaco[i].cod_esenzione = $(this).find("td.esenzione input").attr("data-value") != undefined && $(this).find("td.esenzione input").val() != ''? $(this).find("td.esenzione input").attr("data-value") : '';
				accertamento.cod_esenzione = $this.find("td.esenzione input").attr("data-value") != undefined && $this.find("td.esenzione input").val() != ''? $this.find("td.esenzione input").attr("data-value") : '';
//				accertamento.cod_esenzione = $this.find("td.esenzione input").attr("data-value");
//				delete accertamento.descr_esenzione;
				/*if($(this).find("td.diagnosi select option:selected").val()=='') { // controllo di verifica che tutti gli accertamenti abbiano una diagnosi o che ci sia quella di default
					errMsg = "Inserire una situazione clinica per ogni accertamento RAO";
					return;
				}*/
				if(chkDiaDef && accertamento.diagnosi=='' && accertamento.situazioneClinica=='') { // controllo di verifica che tutti gli accertamenti abbiano una diagnosi o che ci sia quella di default
					errMsg = "Inserire una diagnosi per ogni accertamento o inserire quella di default";
					return false;
				}
				/*Controllo che la diagnosi contenga almeno una lettera*/
				if (!/[a-zA-Z]+/.test(accertamento.diagnosi)) {
					errMsg = 'La nota diagnosi in forma testuale non &egrave; significativa, inserirne una di senso compiuto.';
					return false;
				}
				if (chkUrgDef && accertamento.urgenza=='') { // controllo di verifica che tutti gli accertamenti abbiano una urgenza o che ci sia quella di default
					errMsg = "Inserire una urgenza per ogni accertamento o inserire quella di default";
					return false;
				}
				if ($this.find("td.sedute input").length > 0) {
					accertamento.note = NS_MMG_UTILITY.stripHTML($this.find("td.sedute input").val());
				}
				var noteApp = $this.find(".notePrestazioni");
				if (noteApp.length > 0) {
					var notaAppropriatezza = noteApp.filter(".selected").attr("id_opt");
					var haCondizioni = noteApp.filter(".clsCondizione").length > 0;
					if (typeof notaAppropriatezza == "undefined" || (haCondizioni && notaAppropriatezza.indexOf("CON") < 0)) {
						accertamento.id_nota_appropriatezza = "";
						if (haCondizioni && NS_SALVA_ACCERTAMENTI.objSalvataggio.poci == "") {
							accertamento.forzatura="B";
						}
					} else {
						if (notaAppropriatezza == "IND-B-0") {
							accertamento.id_nota_appropriatezza = "";
							accertamento.forzatura="B";
						} else {
							accertamento.id_nota_appropriatezza = notaAppropriatezza;
							accertamento.forzatura="";
						}
					}
				}
			});
			
			if (errMsg!='') {
				home.NOTIFICA.warning({message:errMsg, timeout:10});
				return; 
			}
//			alert(NS_SALVA_ACCERTAMENTI.objSalvataggio)
			//alert(JSON.stringify(accertamenti));
			home.CARTELLA.NOTE = '';
			
			home.RICETTA_ACCERTAMENTI.saveStampa(stampa_ricetta);
			//NS_SALVA_ACCERTAMENTI.controlloStampa = false;
			//home.NS_FENIX_TOP.chiudiUltima();
			var n_scheda = $("#N_SCHEDA").val();
			var id_scheda = "#iScheda-" + n_scheda;
			home.$(id_scheda).hide();
		},
		
		//funzione che setta il campo delle diagnosi concatenando i risultati della query
		setDiagnosi:function(){
			
			var vProblemi = '',
			    vDiagnosi = '',
			    callDb = false;
			
			$.each(NS_SALVA_ACCERTAMENTI.objSalvataggio.accertamento, function(k,v)
			{
				(v.problema != '') ? ( callDb = true, vProblemi += v.problema +"," ) : ( vProblemi += "0," );
			});
			
			if (callDb) 
			{
				toolKitDB.getResultDatasource('RICETTE.SET_DIAGNOSI','MMG_DATI',{"iden_problemi": vProblemi.substring(0, vProblemi.length - 1 )},null,function(resp){
					
					$.each(resp,function(k,v){
						
						if(vDiagnosi != ''){
							vDiagnosi += "  |  " + v["DESCRIZIONE"];
						}else{
							vDiagnosi += v["DESCRIZIONE"];
						}
					});
					$("#txtDiagnosi").val(vDiagnosi);
				});
			}
		
		},
		
		setNota:function(){
			if (LIB.isValid(home.CARTELLA.NOTE) && home.CARTELLA.NOTE != "") {
				if($("#txtDiagnosi").val()!=''){
					$("#txtDiagnosi").val($("#txtDiagnosi").val()+'\n'+ home.CARTELLA.NOTE);
				}else{
					$("#txtDiagnosi").val(home.CARTELLA.NOTE);
				}
				
			}
		},
		
		setSelNota:function(divButton){
			
			var vCodiceAccertamento = divButton.attr("cod_prestazione");
			var icon = $( document.createElement('i') ).addClass('icon-ok').css({"color":"green","padding-left":"5px"});
			var divNotePrestazioni = $(".notePrestazioni[cod_prestazione='" + vCodiceAccertamento+ "']");
			
			if(divButton.hasClass('selected')){
				divButton.removeClass('selected').find('i.icon-ok').remove();
			}else{
				divButton.addClass('selected').append( icon );
			}
			
			//tolgo la selezione a tutti gli altri button della prestazione
			divNotePrestazioni.each(function(){
				if($(this).attr("id_opt") != divButton.attr("id_opt")){
					$(this).removeClass('selected').find('i.icon-ok').remove();
				}
			});
		},
		
		setEsenzione: function (cod, descr){
			$("tr[title=" + selectedLine + "] .esenzione input").attr("title", cod + ' - ' + descr).attr( "value", cod + ' - ' + descr).attr("data-value", cod);
		},
		
		updateDiagnosiChars: function() {
			
			var max = 256, current = $('#txtDiagnosi').val().length;
			
			if( !LIB.isValid( $('#txtDiagnosi').attr('maxLength') ) )
				 $('#txtDiagnosi').attr('maxLength', max);
			 
			$('#lblDiagnosi').text( NS_SALVA_ACCERTAMENTI.originalDiagnosi + ' (caratteri usati '+ current +'/'+ max +')' );
			
		}
};

//MENU CONTESTUALE CAMPO ESENZIONI PER APPLICA TUTTI
var menuEsenzioni = {
	"menu" : {
		"id" 		: "MENU_RICETTA_ESENZIONI",
		"structure" : {
		"list" 		: [ {
				"concealing" 	: "true",
				"link" 			: function(rec) {NS_SALVA_ACCERTAMENTI.context_menu.applicaEsenzioni($(rec).attr("data-value"), $(rec).val());},
				"enable" 		: "S",
				"icon_class" 	: "incolla",
				"where" 		: function(rec) {return true;},
				"output" 		: "traduzione.lblApplicaEsenzioni",
				"separator" 	: "false"
			},{
				"concealing" 	: "true",
				"link" 			: function(rec) {NS_SALVA_ACCERTAMENTI.context_menu.cancellaEsenzioni($(rec).attr("data-value"), $(rec).val());},
				"enable" 		: "S",
				"icon_class" 	: "bidone",
				"where" 		: function(rec) {return true;},
				"output" 		: "traduzione.lblCancellaEsenzioni",
				"separator" 	: "false"
			}  ]
		},
		"title" 	: "traduzione.lblMenu",
		"status" 	: true
	}
};

var AC = {
		
		select:function(){
			
		},
		
		choose:function(){
			
		}
};