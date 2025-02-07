(function ($) {

    function getPos(el, e) {
        var _pageX = parseInt(e.pageX);
        var _pageY = parseInt(e.pageY);
        var _left;
        var _top;
        var el_w = $(el).outerWidth();
        var el_h = $(el).outerHeight();
        var doc_w = $(window).width();
        var doc_h = $(window).height();

        ((_pageX + el_w) < doc_w) ? _left = _pageX : _left = (_pageX - el_w);
        ((_pageY + el_h) < doc_h) ? _top = _pageY : _top = (_pageY - el_h);
        ((_pageY + el_h) > doc_h && (_pageY - el_h) < 0) ? _top = ((doc_h - el_h) / 2) : null;

        var pos = {
            'left': _left,
            'top': _top
        };
        return pos;

    }

    function getArrowPosition(el, e) {
        var arrowPosition = new Object();
        var _pageX = parseInt(e.pageX);
        var _pageY = parseInt(e.pageY);
        var _left = 0;
        var _top = 0;
        var el_w = $(el).outerWidth();
        var el_h = $(el).outerHeight();
        var doc_w = $(window).width();
        var doc_h = $(window).height();


        var divh = 0;
        ////// basso destra
        if (((el_h + _pageY) > doc_h ) && ((_pageX + 12 +  el_w) > doc_w)) // metti la freccia a destra in basso, popup a destra
        {
            arrowPosition.orientation = 'arrow-right';
            arrowPosition.position = 'bottom';
            arrowPosition.borderOrientation = 'right'
        }

        ///// alto destra
        if (((el_w + 12 +  _pageX) > doc_w ) && ((el_h + _pageY) < doc_h) && _pageY > 11 || (_pageY < 11  && (_pageX + el_w) > doc_w))  // metti la freccia a destra, popup a destra
        {
            arrowPosition.orientation = 'arrow-right';
            arrowPosition.position = 'top';
            arrowPosition.borderOrientation = 'right'
        }


        if (((_pageX + 12  + el_w) < doc_w ) && ((_pageY + 11 ) < doc_h) && (el_h < _pageY))  // metti la freccia a sinistra in basso, popup a destra
        {
            arrowPosition.orientation = 'arrow-left';
            arrowPosition.position = 'bottom';
            arrowPosition.borderOrientation = 'left'
        }

        if (((el_w + 12 + _pageX) < doc_w ) && ((el_h + _pageY) < doc_h) && _pageY > 11 )  // metti la freccia a sinistra, popup a destra
        {
            arrowPosition.orientation = 'arrow-left';
            arrowPosition.position = 'top';
            arrowPosition.borderOrientation = 'left'
        }

        //console.log('elw ' + el_w + ' _pageX ' + _pageX + ' doc_W ' + doc_w + ' || ' + (el_w + _pageX + 12))
        if (((el_h + 12  + _pageY) < doc_h) && ((el_w + _pageX) < doc_w ) && (_pageX > 11) || (_pageY < 11 && (_pageX - 11 + el_w) < doc_w))  // metti la freccia in alto a sx, popup sotto
        {
            arrowPosition.orientation = 'arrow-up';
            arrowPosition.position = 'left'
            arrowPosition.borderOrientation = 'top'
        }

        if ((el_w + _pageX) > doc_w && _pageY < 11)  // metti la freccia in alto a dx, popup sotto    // mettere le condizioni
        {
            arrowPosition.orientation = 'arrow-up';
            arrowPosition.position = 'right'
            arrowPosition.borderOrientation = 'top'
        }
        if ((doc_h - (_pageY) < 12) && (_pageX + el_w) < doc_w)  // metti la freccia in basso a sx, sopra
        {
            arrowPosition.orientation = 'arrow-down';
            arrowPosition.position = 'left'
            arrowPosition.borderOrientation = 'bottom'
        }

        if ((doc_h - (_pageY) < 12) && (_pageX + el_w) > doc_w)  //   metti la freccia in basso a dx, sopra
        {
            arrowPosition.orientation = 'arrow-down';
            arrowPosition.position = 'right'
            arrowPosition.borderOrientation = 'bottom'
        }

        return arrowPosition;
    }


    function getElWidth(elem) {
        return $(elem).outerWidth(true);
    }

    function getElHeight(elem) {
        return $(elem).outerHeight(true);
    }

    function Info(opt) {


        this.options = opt;
        this.init(opt);
        this.setEvents(opt);

        return this;
    };


    $.infoDialog = function (options) {

        var defaults = {
            event: null, //glielo passo
            content: "",
            headerContent: "",
            width: 'auto',
            height: 'auto',
            classPopup: "",
            idPopup:"",
            multi: false,
            dataJSON: false,
            classText:"TextMini",
            appendTo: false
    };

        options = $.extend(defaults, options);

        return new Info(options);
    };


    Info.prototype = {
        init: function (opt) {

            var $this = this;
            $this.createPopup(opt);

        },

        setEvents: function (opt) {

            opt.event.stopImmediatePropagation();
            $(document).on('click', function(e){
                if(opt.multi == false)
                    $('div.infoDialogPopup').remove();
            });

            $(document).on('click','.containerHTML',function(e){
                e.preventDefault();
                e.stopImmediatePropagation();
            });



            ;
        },

        createPopup: function (opt) {
            var $this = this;
            var containerHTML = $(document.createElement("div")).addClass("containerHTML");

            if(opt.multi== false)
            {
                $('div.infoDialogPopup').remove();
            }

            var p = $(document.createElement("div")).addClass("infoDialogPopup " + opt.classPopup).attr("id",opt.idPopup);

            /*Preparo i div*/
            if(opt.dataJSON == true){
                $.each($this.options.content,function(k,v){
                    $.each(v,function(k1,v1){
                        var val;
                        val = (v1==null)?'':v1;
                        var $h5 = $(document.createElement("h5")).html(traduzione[k1]);
                        var $span = $(document.createElement("span")).html(val);
                        var item = $(document.createElement("div")).addClass(opt.classText).append($h5).append($span);
                        $(containerHTML).append(item).append(item);
                    });
                });
                $(containerHTML).appendTo(p);
            }else{
                if(typeof $this.options.content !='object')
                    $(containerHTML).addClass("containerHTML").html($this.options.content).appendTo(p);
                if(typeof $this.options.content =='object')
                    $(containerHTML).html($($this.options.content).html()).appendTo(p);

            }

            if ($this.options.headerContent != null) {
                var header = $(document.createElement("div")).addClass("infoDialogHeaderPopup")
                    .html($this.options.headerContent)
                    .prependTo(p);
            }
            var closeX = $(document.createElement("a")).addClass("close").append($(document.createElement("i")).addClass("icon-cancel-squared"));
            closeX.appendTo(p);

            closeX.on('click',function(e){
                $(this).parent().remove();
             });

            if(!$this.options.appendTo)
                $("body").append(p);
            else
                p.appendTo($this.options.appendTo);


            arrow = $(document.createElement("div")).addClass("arrow");
            //arrow.appendTo(p);     lascio solo il bordo questa ogni tanto a sinistra in basso da dei problemi e vedo due frecce


            arrow_border =  $(document.createElement("div")).addClass("arrow-border");
            arrow_border.appendTo(p);


            p.css({
                "width": $this.options.width,
                "height": $this.options.height
            });

            var pos = getPos(p, opt.event);
            var arrowPosition = getArrowPosition(p, opt.event);

            arrow.addClass(arrowPosition.orientation)    //poi la classe è una variabile definita dalla funzione getArrowPosition
            arrow_border.addClass(arrowPosition.borderOrientation)  //poi la classe è una variabile definita dalla posizione getArrowPosition


            //console.log(arrowPosition);





            switch (arrowPosition.orientation) {
                case 'arrow-up':
                    if (arrowPosition.position == 'left') {
                        p.css({
                            "left": (pos.left) - 10,
                            "top": (pos.top) + 11
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": -1,
                            "top": -12     // se si cambia colore mettere -10 per fare vedere 1px del colore del div sotto, quello del bordo
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": -1,
                            "top": -12
                        });


                    }
                    else if (arrowPosition.position == 'right') {
                        p.css({
                            "left": (pos.left) + 10,
                            "top": (pos.top) + 11
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 23,
                            "top": -11     // se si cambia colore mettere -10 per fare vedere 1px del colore del div sotto, quello del bordo
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 23,
                            "top": -11
                        });


                    }
                    break;


                case 'arrow-left':
                    if (arrowPosition.position == 'top') {
                        p.css({
                            "left": pos.left + 11,
                            "top": pos.top - 10
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": -11,
                            "top": -1
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": -11,
                            "top": -1
                        });
                    }
                    else if (arrowPosition.position == 'bottom') {
                        p.css({
                            "left": pos.left + 11,
                            "top": pos.top + 11
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": -11,
                            "top": getElHeight(p) - 23
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": -12,
                            "top": getElHeight(p) - 23
                        });

                    }

                    break;


                case 'arrow-right':

                    if (arrowPosition.position == 'top') {
                        p.css({
                            "left": pos.left - 11,
                            "top": pos.top - 10
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 2,
                            "top": 0
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 1,
                            "top": 0
                        });
                    }
                    else if (arrowPosition.position == 'bottom') {
                        p.css({
                            "left": pos.left - 11, //-getElWidth(p)-11,
                            "top": pos.top + 10
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 2,
                            "top": getElHeight(p) - 23 // spostare 2 px per avere il bordo ( facendo vedere 2 px del div sotto che da il colore al bordo)
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 2,
                            "top": getElHeight(p) - 23
                        });


                    }

                    break;

                case 'arrow-down':

                    if (arrowPosition.position == 'left') {
                        p.css({
                            "left": pos.left - 10,
                            "top": pos.top - 10
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": -1,
                            "top": getElHeight(p) - 13
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": -1,
                            "top": getElHeight(p) - 1
                        });
                    }
                    else if (arrowPosition.position == 'right') {
                        p.css({
                            "left": pos.left + 10, //-getElWidth(p)-11,
                            "top": pos.top - 10
                        });

                        arrow.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 23,
                            "top": getElHeight(p) - 2 // spostare 2 px per avere il bordo ( facendo vedere 2 px del div sotto che da il colore al bordo)
                        });

                        arrow_border.css({
                            "position": 'absolute',
                            "left": getElWidth(p) - 23,
                            "top": getElHeight(popup) - 2
                        });


                    }

                    break;

                default :

                    p.css({
                        "left": pos.left,
                        "top": pos.top
                    });
                    break;
            }


        }

    }

})(jQuery);