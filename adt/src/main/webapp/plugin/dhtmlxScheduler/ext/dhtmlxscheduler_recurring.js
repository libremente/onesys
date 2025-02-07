/*
 This software is allowed to use under GPL or you need to obtain Commercial or Enterise License
 to use it in non-GPL project. Please contact sales@dhtmlx.com for details
 */
scheduler.config.occurrence_timestamp_in_utc = !1;
scheduler.form_blocks.recurring = {render: function () {
    return scheduler.__recurring_template
}, _ds: {}, _init_set_value: function (a, b, c) {
    function d(a) {
        for (var b = 0; b < a.length; b++) {
            var c = a[b];
            c.type == "checkbox" || c.type == "radio" ? (h[c.name] || (h[c.name] = []), h[c.name].push(c)) : h[c.name] = c
        }
    }

    function f(a) {
        for (var b = h[a], c = 0; c < b.length; c++)if (b[c].checked)return b[c].value
    }

    function e() {
        m("dhx_repeat_day").style.display = "none";
        m("dhx_repeat_week").style.display = "none";
        m("dhx_repeat_month").style.display = "none";
        m("dhx_repeat_" + this.value).style.display = "block"
    }

    function g(a) {
        var b = [f("repeat")];
        for (q[b[0]](b, a); b.length < 5;) {
            b.push("");
        }
        var c = "";
        h = document.getElementById('recurring_form');
        if (h.end[0].checked){
            a.end = new Date(9999, 1, 1);
            c = "no";
        }else if (h.end[1].checked){
            a.end = j(h.date_of_end.value);
            c = moment(a.end).format('YYYYMMDD');
        }
        return b.join("_") +"#" + c
    }

    function i(a, b) {
        var c = a.split("#"), a = c[0].split("_");
        r[a[0]](a, b);
        var e = h.repeat[{day: 0, week: 1, month: 2, year: 3}[a[0]]];
        switch (c[1]) {
            case "no":
                h.end[0].checked = !0;
                break;
            case "":
                h.end[2].checked = !0;
                h.date_of_end.value = l(b.end);
                break;
            default:
                h.end[1].checked = !0, h.occurences_count.value = c[1]
        }
        e.checked = !0;
        e.onclick()
    }

    scheduler.form_blocks.recurring._ds = {start: c.start_date, end: c._end_date};
    var k = scheduler.date.str_to_date(scheduler.config.repeat_date), j = function (a) {
        var b = k(a);
        scheduler.config.include_end_by &&
        (b = scheduler.date.add(b, 1, "day"));
        return b
    }, l = scheduler.date.date_to_str(scheduler.config.repeat_date), n = a.getElementsByTagName("FORM")[0], h = [];
    d(n.getElementsByTagName("INPUT"));
    d(n.getElementsByTagName("SELECT"));
    if (!scheduler.config.repeat_date_of_end) {
        var s = scheduler.date.date_to_str(scheduler.config.repeat_date);
        scheduler.config.repeat_date_of_end = s(scheduler.date.add(scheduler._currentDate(), 30, "day"))
    }
    h.date_of_end.value = scheduler.config.repeat_date_of_end;
    var m = function (a) {
        return document.getElementById(a)
    };
    scheduler.form_blocks.recurring._get_repeat_code = g;
    var q = {month: function (a, b) {
        h = document.getElementById('recurring_form');
        //alert(h.month_day2.value)
        (a.push(1), a.push(h.month_day2.value), a.push(Math.max(1, h.month_week2.value)), b.start.setDate(1));
        b._start = !0
    }, week: function (a, b) {
        a.push(1);
        a.push("");
        a.push("");
        for (var c = [], e = h.week_day, d = b.start.getDay(), f = !1, g = 0; g < e.length; g++)e[g].checked && (c.push(e[g].value),
            f = f || e[g].value == d);
        c.length || (c.push(d), f = !0);
        c.sort();
        if (scheduler.config.repeat_precise) {
            if (!f)scheduler.transpose_day_week(b.start, c, 1, 7), b._start = !0
        } else b.start = scheduler.date.week_start(b.start), b._start = !0;
        a.push(c.join(","))
    }, day: function (a) {
        f("day_type") == "d" ? a.push(Math.max(1, h.day_count.value)) : (a.push("week"), a.push(1), a.push(""), a.push(""), a.push("0,1,2,3,4,5,6"), a.splice(0, 1))
    }}, r = {week: function (a) {
        h.week_count.value = a[1];
        for (var b = h.week_day, c = a[4].split(","), e = {}, d = 0; d < c.length; d++)e[c[d]] = !0;
        for (d = 0; d < b.length; d++)b[d].checked = !!e[b[d].value]
    }, month: function (a, b) {
        a[2] == "" ?
            (h.month_type[0].checked = !0, h.month_count.value = a[1], h.month_day.value = b.start.getDate())
            :
            (h.month_type[1].checked = !0, h.month_count2.value = a[1], h.month_week2.value = a[3], h.month_day2.value = a[2])
    }, day: function (a) {
        h.day_type[0].checked = !0;
        h.day_count.value = a[1]
    }};
    scheduler.form_blocks.recurring._set_repeat_code = i;
    for (var o = 0; o < n.elements.length; o++) {
        var p = n.elements[o];
        switch (p.name) {
            case "repeat":
                p.onclick = e
        }
    }
    scheduler._lightbox._rec_init_done = !0
}, set_value: function (a, b, c) {
    var d = scheduler.form_blocks.recurring;
    scheduler._lightbox._rec_init_done || d._init_set_value(a, b, c);
    a.open = !c.rec_type;
    a.blocked = c.event_pid && c.event_pid != "0" ? !0 : !1;
    var f = d._ds;
    f.start = c.start_date;
    f.end = c._end_date;
    d.button_click(0, a.previousSibling.firstChild.firstChild, a, a);
    b && d._set_repeat_code(b, f)
}, get_value: function (a, b) {
    if (a.open) {
        var c = scheduler.form_blocks.recurring._ds, d = {};
        scheduler.formSection("time").getValue(d);
        c.start = d.start_date;
        b.rec_type = scheduler.form_blocks.recurring._get_repeat_code(c);
        c._start ? (b.start_date = new Date(c.start), b._start_date = new Date(c.start), c._start = !1) : b._start_date = null;
        b._end_date = c.end;
        b.rec_pattern = b.rec_type.split("#")[0]
    } else b.rec_type = b.rec_pattern = "", b._end_date = b.end_date;
    return b.rec_type
}, focus: function () {
}, button_click: function (a, b, c, d) {
    !d.open && !d.blocked ? (d.style.height = "115px", b.style.backgroundPosition = "-5px 0px", b.nextSibling.innerHTML = scheduler.locale.labels.button_recurring_open) : (d.style.height = "0px", b.style.backgroundPosition = "-5px 20px",
        b.nextSibling.innerHTML = scheduler.locale.labels.button_recurring);
    d.open = !d.open;
    scheduler.setLightboxSize()
}};
scheduler._rec_markers = {};
scheduler._rec_markers_pull = {};
scheduler._add_rec_marker = function (a, b) {
    a._pid_time = b;
    this._rec_markers[a.id] = a;
    this._rec_markers_pull[a.event_pid] || (this._rec_markers_pull[a.event_pid] = {});
    this._rec_markers_pull[a.event_pid][b] = a
};
scheduler._get_rec_marker = function (a, b) {
    var c = this._rec_markers_pull[b];
    return c ? c[a] : null
};
scheduler._get_rec_markers = function (a) {
    return this._rec_markers_pull[a] || []
};
scheduler._rec_temp = [];
(function () {
    var a = scheduler.addEvent;
    scheduler.addEvent = function (b, c, d, f, e) {
        var g = a.apply(this, arguments);
        if (g) {
            var i = scheduler.getEvent(g);
            i.event_pid != 0 && scheduler._add_rec_marker(i, i.event_length * 1E3);
            if (i.rec_type)i.rec_pattern = i.rec_type.split("#")[0]
        }
        return g
    }
})();
scheduler.attachEvent("onEventIdChange", function (a, b) {
    if (!this._ignore_call) {
        this._ignore_call = !0;
        for (var c = 0; c < this._rec_temp.length; c++) {
            var d = this._rec_temp[c];
            if (d.event_pid == a)d.event_pid = b, this.changeEventId(d.id, b + "#" + d.id.split("#")[1])
        }
        delete this._ignore_call
    }
});
scheduler.attachEvent("onConfirmedBeforeEventDelete", function (a) {
    var b = this.getEvent(a);
    if (a.toString().indexOf("#") != -1 || b.event_pid && b.event_pid != "0" && b.rec_type && b.rec_type != "none") {
        var a = a.split("#"), c = this.uid(), d = a[1] ? a[1] : b._pid_time / 1E3, f = this._copy_event(b);
        f.id = c;
        f.event_pid = b.event_pid || a[0];
        var e = d;
        f.event_length = e;
        f.rec_type = f.rec_pattern = "none";
        this.addEvent(f);
        this._add_rec_marker(f, e * 1E3)
    } else {
        b.rec_type && this._lightbox_id && this._roll_back_dates(b);
        var g = this._get_rec_markers(a), i;
        for (i in g)if (g.hasOwnProperty(i))a = g[i].id, this.getEvent(a) && this.deleteEvent(a, !0)
    }
    return!0
});
scheduler.attachEvent("onEventChanged", function (a) {
    if (this._loading)return!0;
    var b = this.getEvent(a);
    if (a.toString().indexOf("#") != -1) {
        var a = a.split("#"), c = this.uid();
        this._not_render = !0;
        var d = this._copy_event(b);
        d.id = c;
        d.event_pid = a[0];
        var f = a[1];
        d.event_length = f;
        d.rec_type = d.rec_pattern = "";
        this._add_rec_marker(d, f * 1E3);
        this.addEvent(d);
        this._not_render = !1
    } else {
        b.rec_type && this._lightbox_id && this._roll_back_dates(b);
        var e = this._get_rec_markers(a), g;
        for (g in e)e.hasOwnProperty(g) && (delete this._rec_markers[e[g].id],
            this.deleteEvent(e[g].id, !0));
        delete this._rec_markers_pull[a];
        for (var i = !1, k = 0; k < this._rendered.length; k++)this._rendered[k].getAttribute("event_id") == a && (i = !0);
        if (!i)this._select_id = null
    }
    return!0
});
scheduler.attachEvent("onEventAdded", function (a) {
    if (!this._loading) {
        var b = this.getEvent(a);
        b.rec_type && !b.event_length && this._roll_back_dates(b)
    }
    return!0
});
scheduler.attachEvent("onEventSave", function (a, b) {
    var c = this.getEvent(a);
    if (!c.rec_type && b.rec_type && (a + "").indexOf("#") == -1)this._select_id = null;
    return!0
});
scheduler.attachEvent("onEventCreated", function (a) {
    var b = this.getEvent(a);
    if (!b.rec_type)b.rec_type = b.rec_pattern = b.event_length = b.event_pid = "";
    return!0
});
scheduler.attachEvent("onEventCancel", function (a) {
    var b = this.getEvent(a);
    b.rec_type && (this._roll_back_dates(b), this.render_view_data())
});
scheduler._roll_back_dates = function (a) {
    a.event_length = (a.end_date.valueOf() - a.start_date.valueOf()) / 1E3;
    a.end_date = a._end_date;
    a._start_date && (a.start_date.setMonth(0), a.start_date.setDate(a._start_date.getDate()), a.start_date.setMonth(a._start_date.getMonth()), a.start_date.setFullYear(a._start_date.getFullYear()))
};
scheduler._validId = function (a) {
    return a.toString().indexOf("#") == -1
};
scheduler.showLightbox_rec = scheduler.showLightbox;
scheduler.showLightbox = function (a) {
    var b = this.locale, c = scheduler.config.lightbox_recurring, d = this.getEvent(a), f = d.event_pid, e = a.toString().indexOf("#") != -1;
    e && (f = a.split("#")[0]);
    var g = function (a) {
        var b = scheduler.getEvent(a);
        b._end_date = b.end_date;
        b.end_date = new Date(b.start_date.valueOf() + b.event_length * 1E3);
        return scheduler.showLightbox_rec(a)
    };
    if ((f || f == 0) && d.rec_type)return g(a);
    if (!f || f == 0 || !b.labels.confirm_recurring || c == "instance" || c == "series" && !e)return this.showLightbox_rec(a);
    if (c == "ask") {
        var i =
            this;
        dhtmlx.modalbox({text: b.labels.confirm_recurring, title: b.labels.title_confirm_recurring, width: "500px", position: "middle", buttons: [b.labels.button_edit_series, b.labels.button_edit_occurrence, b.labels.icon_cancel], callback: function (b) {
            switch (+b) {
                case 0:
                    return g(f);
                case 1:
                    return i.showLightbox_rec(a)
            }
        }})
    } else g(f)
};
scheduler.get_visible_events_rec = scheduler.get_visible_events;
scheduler.get_visible_events = function (a) {
    for (var b = 0; b < this._rec_temp.length; b++)delete this._events[this._rec_temp[b].id];
    this._rec_temp = [];
    for (var c = this.get_visible_events_rec(a), d = [], b = 0; b < c.length; b++)c[b].rec_type ? c[b].rec_pattern != "none" && this.repeat_date(c[b], d) : d.push(c[b]);
    return d
};
(function () {
    var a = scheduler.isOneDayEvent;
    scheduler.isOneDayEvent = function (b) {
        return b.rec_type ? !0 : a.call(this, b)
    };
    var b = scheduler.updateEvent;
    scheduler.updateEvent = function (a) {
        var d = scheduler.getEvent(a);
        if (d.rec_type)d.rec_pattern = (d.rec_type || "").split("#")[0];
        d && d.rec_type && a.toString().indexOf("#") === -1 ? scheduler.update_view() : b.call(this, a)
    }
})();
scheduler.transponse_size = {day: 1, week: 7, month: 1, year: 12};
scheduler.date.day_week = function (a, b, c) {
    a.setDate(1);
    var c = (c - 1) * 7, d = a.getDay(), f = b * 1 + c - d + 1;
    a.setDate(f <= c ? f + 7 : f)
};
scheduler.transpose_day_week = function (a, b, c, d, f) {
    for (var e = (a.getDay() || (scheduler.config.start_on_monday ? 7 : 0)) - c, g = 0; g < b.length; g++)if (b[g] > e)return a.setDate(a.getDate() + b[g] * 1 - e - (d ? c : f));
    this.transpose_day_week(a, b, c + d, null, c)
};
scheduler.transpose_type = function (a) {
    var b = "transpose_" + a;
    if (!this.date[b]) {
        var c = a.split("_"), d = 864E5, f = "add_" + a, e = this.transponse_size[c[0]] * c[1];
        if (c[0] == "day" || c[0] == "week") {
            var g = null;
            if (c[4] && (g = c[4].split(","), scheduler.config.start_on_monday)) {
                for (var i = 0; i < g.length; i++)g[i] = g[i] * 1 || 7;
                g.sort()
            }
            this.date[b] = function (a, b) {
                var c = Math.floor((b.valueOf() - a.valueOf()) / (d * e));
                c > 0 && a.setDate(a.getDate() + c * e);
                g && scheduler.transpose_day_week(a, g, 1, e)
            };
            this.date[f] = function (a, b) {
                var c = new Date(a.valueOf());
                if (g)for (var d = 0; d < b; d++)scheduler.transpose_day_week(c, g, 0, e); else c.setDate(c.getDate() + b * e);
                return c
            }
        } else if (c[0] == "month" || c[0] == "year")this.date[b] = function (a, b) {
            var d = Math.ceil((b.getFullYear() * 12 + b.getMonth() * 1 - (a.getFullYear() * 12 + a.getMonth() * 1)) / e);
            d >= 0 && a.setMonth(a.getMonth() + d * e);
            c[3] && scheduler.date.day_week(a, c[2], c[3])
        }, this.date[f] = function (a, b) {
            var d = new Date(a.valueOf());
            d.setMonth(d.getMonth() + b * e);
            c[3] && scheduler.date.day_week(d, c[2], c[3]);
            return d
        }
    }
};
scheduler.repeat_date = function (a, b, c, d, f) {
    var d = d || this._min_date, f = f || this._max_date, e = new Date(a.start_date.valueOf());
    if (!a.rec_pattern && a.rec_type)a.rec_pattern = a.rec_type.split("#")[0];
    this.transpose_type(a.rec_pattern);
    for (scheduler.date["transpose_" + a.rec_pattern](e, d); e < a.start_date || scheduler._fix_daylight_saving_date(e, d, a, e, new Date(e.valueOf() + a.event_length * 1E3)).valueOf() <= d.valueOf() || e.valueOf() + a.event_length * 1E3 <= d.valueOf();)e = this.date.add(e, 1, a.rec_pattern);
    for (; e < f && e < a.end_date;) {
        var g =
            scheduler.config.occurrence_timestamp_in_utc ? Date.UTC(e.getFullYear(), e.getMonth(), e.getDate(), e.getHours(), e.getMinutes(), e.getSeconds()) : e.valueOf(), i = this._get_rec_marker(g, a.id);
        if (i)c && b.push(i); else {
            var k = new Date(e.valueOf() + a.event_length * 1E3), j = this._copy_event(a);
            j.text = a.text;
            j.start_date = e;
            j.event_pid = a.id;
            j.id = a.id + "#" + Math.ceil(g / 1E3);
            j.end_date = k;
            j.end_date = scheduler._fix_daylight_saving_date(j.start_date, j.end_date, a, e, j.end_date);
            j._timed = this.isOneDayEvent(j);
            if (!j._timed && !this._table_view && !this.config.multi_day)break;
            b.push(j);
            c || (this._events[j.id] = j, this._rec_temp.push(j))
        }
        e = this.date.add(e, 1, a.rec_pattern)
    }
};
scheduler._fix_daylight_saving_date = function (a, b, c, d, f) {
    var e = a.getTimezoneOffset() - b.getTimezoneOffset();
    return e ? e > 0 ? new Date(d.valueOf() + c.event_length * 1E3 - e * 6E4) : new Date(b.valueOf() - e * 6E4) : new Date(f.valueOf())
};
scheduler.getRecDates = function (a, b) {
    var c = typeof a == "object" ? a : scheduler.getEvent(a), d = 0, f = [], b = b || 100, e = new Date(c.start_date.valueOf()), g = new Date(e.valueOf());
    if (!c.rec_type)return[
        {start_date: c.start_date, end_date: c.end_date}
    ];
    if (c.rec_type == "none")return[];
    this.transpose_type(c.rec_pattern);
    for (scheduler.date["transpose_" + c.rec_pattern](e, g); e < c.start_date || e.valueOf() + c.event_length * 1E3 <= g.valueOf();)e = this.date.add(e, 1, c.rec_pattern);
    for (; e < c.end_date;) {
        var i = this._get_rec_marker(e.valueOf(),
            c.id), k = !0;
        if (i)i.rec_type == "none" ? k = !1 : f.push({start_date: i.start_date, end_date: i.end_date}); else {
            var j = new Date(e), l = new Date(e.valueOf() + c.event_length * 1E3), l = scheduler._fix_daylight_saving_date(j, l, c, e, l);
            f.push({start_date: j, end_date: l})
        }
        e = this.date.add(e, 1, c.rec_pattern);
        if (k && (d++, d == b))break
    }
    return f
};
scheduler.getEvents = function (a, b) {
    var c = [], d;
    for (d in this._events) {
        var f = this._events[d];
        if (f && f.start_date < b && f.end_date > a)if (f.rec_pattern) {
            if (f.rec_pattern != "none") {
                var e = [];
                this.repeat_date(f, e, !0, a, b);
                for (var g = 0; g < e.length; g++)!e[g].rec_pattern && e[g].start_date < b && e[g].end_date > a && !this._rec_markers[e[g].id] && c.push(e[g])
            }
        } else f.id.toString().indexOf("#") == -1 && c.push(f)
    }
    return c
};
scheduler.config.repeat_date = "%m.%d.%Y";
scheduler.config.lightbox.sections = [
    {name: "description", height: 130, map_to: "text", type: "textarea", focus: !0},
    {name: "recurring", type: "recurring", map_to: "rec_type", button: "recurring"},
    {name: "time", height: 72, type: "time", map_to: "auto"}
];
scheduler._copy_dummy = function () {
    var a = new Date(this.start_date), b = new Date(this.end_date);
    this.start_date = a;
    this.end_date = b;
    this.event_length = this.event_pid = this.rec_pattern = this.rec_type = null
};
scheduler.config.include_end_by = !1;
scheduler.config.lightbox_recurring = "ask";
scheduler.attachEvent("onClearAll", function () {
    scheduler._rec_markers = {};
    scheduler._rec_markers_pull = {};
    scheduler._rec_temp = []
});
