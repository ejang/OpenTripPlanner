/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

otp.namespace("otp.util");

/**
 * Utility routines for operating on unknown objects
 */
otp.util.DateUtils = {

    DATE_TIME_FORMAT_STRING : "D, M jS g:ia",
    TIME_FORMAT_STRING : "g:ia",

    /** creates a nicely formatted date @ time string */
    getPrettyDate : function(pre, post, date)
    {
        var retVal = "";
        try
        {
            if(date == null)
                date = new Date();
            if(pre == null)
                pre = "";
            if(post == null)
                post = "";
                
            retVal = pre + date.toDateString() + ' @ ' + date.toLocaleTimeString() + post;
        }
        catch(e)
        {
        }

        return retVal;
    },

    /** minutes / seconds */
    getMinutesAndSeconds : function(m, s, mStr, mmStr, sStr)
    {
        var retVal = "";

        if(mStr  == ":"){ mmStr = ":"; sStr="";}
        if(mStr  == "."){ mmStr = "."; sStr="";}
        if(mStr  == null) mStr  = " " + this.locale.time.minute_abbrev + ", ";
        if(mmStr == null) mmStr = " " + this.locale.time.minutes_abbrev;
        if(sStr  == null) sStr  = " " + this.locale.time.seconds_abbrev + " ";

        if(m && m > 0)
            retVal += m + (m == 1 ? mStr : mmStr);

        retVal +=  (s < 10 ? "0" + s : s) + sStr;

        return retVal;
    },

    /** */ 
    getMonthAsInt : function(str, pad)
    {
        var retVal = str;
        var months = this.locale.time.months;

        for(var i = 0; i < months.length; i++)
        {
            if(str == months[i])
            {
                i++;  // advance to real month number
                retVal = "";

                if(pad && i < 10)
                    retVal = "0";

                retVal += i;
                break;
            }
        }

        return retVal;
    },

    /** */ 
    getElapsedTime : function(oldDate, min, sec)
    {
        if(min == null) min = 0;
        if(sec == null) sec = 0;

        var retVal = {};
        try
        {
            var tsecs  = Math.round(((new Date().getTime() - oldDate.getTime()) / 1000) + sec);
            retVal.min = Math.floor(tsecs / 60) + min;
            retVal.sec = tsecs % 60;
        }
        catch(e)
        {
            retVal = {"min":this.locale.time.minute_abbrev, "sec":this.locale.time.second_abbrev};
        }

        return retVal;
    },

    
    /** default to now + 365 days */ 
    addDays : function(days, date)
    {
        if(date == null)
            date = new Date();
        if(days == null)
            days = 365;

        return new Date(date.getTime()+(1000*60*60*24*days));
    },
    
    /** Make a Date object from an ISO 8601 date string (ignoring time zone) */
    isoDateStringToDate : function(str)
    {
        var date = null;
        if(str)
        {
            if (str.lastIndexOf("Z") != -1) {
                str = str.substring(0, str.length - 1);
            }
            var tokens = str.split(/[\-\+T:]/);
            date = new Date(tokens[0], tokens[1] - 1, tokens[2], tokens[3], tokens[4], tokens[5], 0);
        }
        return date;
    },
    
    pad : function (n, digits) {
	    var string = n.toString();
	    var missingDigits = digits - string.length;
	    if (missingDigits > 0) {
	    	string = ('0' * missingDigits) + string;
	    }
	    return string;
	},

    /** make an iso [YYYY]-[MM]-[DD] (2012-04-22) date for the api */
    dateToIsoDateString : function(date, defVal)
    {
        var retVal = defVal;
        try
        {
        	// there is a Date.toISOString() method, but it will account for the browser time zone
        	// we want to assume the date is expressed in the _server_ time zone
        	retVal = [date.getFullYear(), this.pad(date.getMonth() + 1, 2), 
        	          this.pad(date.getDate(), 2)].join('-'); 
        	console.log(retVal);
        }
        catch(e)
        {
            console.log("WARN EXCEPTION in dateToIsoString(): " + e);
        }
        return retVal;
    },

    /** */
    prettyDateTime : function(date)
    {
        if (typeof date == "string") {
            date = this.isoDateStringToDate(date);
        }
        return date.format(this.DATE_TIME_FORMAT_STRING);
    },

    /** */
    prettyTime : function(date)
    {
        if (typeof date == "string") {
            date = this.isoDateStringToDate(date);
        }
        return date.format(this.TIME_FORMAT_STRING);
    },

    /** my own parse int, since I've seen "09" not work natively */ 
    parseInt : function(int, defVal)
    {
        var retVal = defVal;
        try
        {
            if(int)
            {
                var i = int * 1;
                i = i * 1;
                i = i / 1;
                retVal = i;
            }
        }
        catch(e)
        {}
        return retVal;
    },

    /** arbitrary am/pm time string correction ... e.g., 1233pm gets formatted into 12:33pm, etc... */
    correctAmPmTimeString : function(time, format)
    {
        time = time.trim();
        time = time.match(/(\d+)(?::(\d\d))?\s*(p?)/);

        var h = time[1];
        var m = this.parseInt(time[2], null);
        var am = time[3];

        if(h && h.length > 2)
        {
            if(m == null)
                m = h.substring(h.length-2);
            h = h.substring(0, h.length-2);
        }
        h = this.parseInt(h, 12);
        if(h > 12)
        {
            h = h % 12;
            if(h == 0)
                h = 12;
            if(am == null || am == '')
                am = 'p';
        }
        if(m == null || m > 59 || m < 0)
            m = "00";
        else if(m.length != 2 && m >= 0 && m <= 9)
        {
            m = "0" + m          // pad single digit number
        }

        m = "" + m + "";
        if(m.length != 2)
            console.log("ERROR: we have problem with our minutes string:== " + m);

        if(am)
        {
            if(am == 'p')
                am = "pm"
            else
                am = "am"
        } 
        else
        {
            if(h > 6 && h < 12)
                am = "am";
            else
                am = "pm";
        }

        var space = "";
        if(format.toLowerCase().charAt(format.length-2) == " ")
            space = " ";

        return  h + ":" + m + space + am;
    },

    /** time string parser / correction */
    parseTime : function(time, format)
    {
        var retVal = time;
        if(format && format.toLowerCase().charAt(format.length-1) == "a" && format.toLowerCase().indexOf("g:i") == 0)
        {
            retVal = this.correctAmPmTimeString(time, format);
        }
        return retVal;
    },

    /** */
    parseTimeTest : function(t)
    {
        var times = ['1:00 pm','1:00 p.m.','100 p','1:00p.m.','1:00p','1 pm','1 p.m.','1 p','1pm','1p.m.','1p','1:pm','13:00','13','944am', '1354','12335','1232p'];

        for ( var i = 0; i < times.length; i++ )
        {
          console.log(this.parseTime(times[i], 'g:i a'));
          console.log(this.parseTime(times[i], 'H:i'));
        }
    },

    CLASS_NAME : "otp.util.DateUtils"
};
