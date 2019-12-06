/**
 * User: carlog
 *
 * Date: 11/03/14
 */

jQuery(document).ready(function () {

    NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.init();
    NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.setEvents();

});

var NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE = {

    firma : false,
    idenScheda: null,
    validator : null,

    init: function(){

        home.NS_CONSOLEJS.addLogger({name: 'GIUNTO_CADAVERE', console: 0});
        window.logger = home.NS_CONSOLEJS.loggers['GIUNTO_CADAVERE'];

        NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.validator = NS_FENIX_SCHEDA.addFieldsValidator({config: "V_PS_GIUNTO_CADAVERE"});

        NS_FENIX_SCHEDA.customizeParam = function (params) {params.extern = true; return params;};
        NS_FENIX_SCHEDA.successSave = NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.successSave;
        NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.detectStatoPagina();
        NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.checkStatoFirma($("#hStatoScheda").val());
        home.NS_FENIX_PS.IDEN_SCHEDA = jsonData.hIDEN;

    },

    detectStatoPagina: function(){
        if($("#STATO_PAGINA").val() == "E"){
            $("div.headerTabs").html("<h2 style='color: rgb(255, 255, 0);' id='lblAlertModifica'>MODIFICA</h2>");
            $("#h-radConstatazioneGiunto").trigger("change");
        }
    },

    checkStatoFirma : function(stato){

        $("#IS_DA_FIRMARE").val(stato == "F" ? "S" : "N");

        logger.debug("IS_DA_FIRMARE -> " + $("#IS_DA_FIRMARE").val());

        if(stato === "F"){
            $(".butSalva").hide();
        }
    },

    setEvents: function(){
        $(".butFirma").on("click", function () {

            if (NS_FENIX_SCHEDA.validateFields()){

                NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.firma = true;
                home.NS_LOADING.showLoading({"timeout": "0", "testo": "FIRMA", "loadingclick": function () {home.NS_LOADING.hideLoading();}});
                NS_FENIX_SCHEDA.registra();
            }

        });
    },

    successSave: function (message) {

        NS_FIRMA_MODULI.setIdenScheda(message);

        $("div.headerTabs").html("<h2 style='color: rgb(0, 255, 0);' id='lblAlertCompleto'>COMPLETATO</h2>");



        if (NS_MOD_CONSTATAZIONE_GIUNTOCADAVERE.firma == true)
        {
            NS_REGISTRAZIONE_FIRMA.firma();
        }
        /*else
        {
            NS_MODULISTICA_FUNZIONI_COMUNI.refreshModulistica($("#LISTA_CHIUSI").val(),true,function(){home.CARTELLA.jsonData.H_STATO_PAGINA.MODULO_CONSTATAZIONE_GIUNTOCADAVERE = 'E';});
        } */
    }
};

var NS_REGISTRAZIONE_FIRMA =
{
    firma : function (p) {


        $("#butSalva, #butFirma").off("click");

        NS_FIRMA_MODULI.setIdenContatto($("#IDEN_CONTATTO").val());
        NS_FIRMA_MODULI.setStatoVerbale(jsonData.hStatoScheda);
        NS_FIRMA_MODULI.setDocumento("MODULO_CONSTATAZIONE_GIUNTOCADAVERE");
        NS_FIRMA_MODULI.setReport("MODULO_CONSTATAZIONE_GIUNTOCADAVERE");
        NS_FIRMA_MODULI.setCallback(function(){
            //NS_MODULISTICA_FUNZIONI_COMUNI.refreshModulistica($("#LISTA_CHIUSI").val(), true, function(){});
            if (typeof home.CARTELLA !== 'undefined') {
                home.CARTELLA.jsonData.H_STATO_PAGINA.MODULO_CONSTATAZIONE_GIUNTOCADAVERE = 'E';
            }
            home.NS_LOADING.hideLoading();});
        NS_FIRMA_MODULI.setListaChiusi($("#LISTA_CHIUSI").val());

        NS_FIRMA_MODULI.firmaGenericaModuli();
    }

};
