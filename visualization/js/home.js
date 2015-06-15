var data;
  function handleFileSelect(evt) {
    var file = evt.target.files[0];
 
    Papa.parse(file, {
      header: true,
      dynamicTyping: true,
      complete: function(results) {
        var keys = [];
        var values = [];

        for(var k in results.data) keys.push(k);
        for(var i=0; i<keys.length; i++)
			values.push(results.data[i][0]);
		
		var array = []
		for (var i = 0; i < values.length; i++) {
			array.push(i+1);
			array.push(values[i]);	
		}
		console.log(array);
		//var array = [13,0,14,1,15,3,16,2];

    for (var i = 0; i < array.length; i=i+2) {
    	if(array[i]%13 == 0)
    		var col = 13;
		else
			col = array[i]%13;
		var row = 0;
		while(row*13 < array[i]){
			row = row + 1;
		}
		//console.log("row"+row);
		//console.log("col"+col);
		if(array[i+1]==0)
			$("#row"+row+" #c"+col).css("background-color","rgba(111,232,202,0.6)");
		if(array[i+1]==1)
			$("#row"+row+" #c"+col).css("background-color","rgba(241,241,135,0.6)");
		if(array[i+1]==2)
			$("#row"+row+" #c"+col).css("background-color","rgba(255,87,115,0.6)");	
		/*if(array[i+1]==0)
			$("#row"+row+" #c"+col).css("background-color","rgba(111,232,202,0.5)");
		if(array[i+1]==1)
			$("#row"+row+" #c"+col).css("background-color","rgba(111,232,202,0.5)");
		if(array[i+1]==2)
			$("#row"+row+" #c"+col).css("background-color","rgba(111,232,202,0.5)");*/	    	
	}
      	
         $('body').css({
        '-moz-transform':'rotate(270deg)',
        '-webkit-transform':'rotate(270deg)',
        '-o-transform':'rotate(270deg)',
        '-ms-transform':'rotate(270deg)',
        'transform':'rotate(270deg)'
        });
         $("body").css("position", "absolute").animate({
            left: 0,
            top:  -14
        });

		/*$("#row13 #c10").css("background-color","rgba(111,232,202,0.5)");
		$("#row13 #c11").css("background-color","rgba(111,232,202,0.5)");
		$("#row13 #c12").css("background-color","rgba(111,232,202,0.5)");
		$("#row13 #c13").css("background-color","rgba(111,232,202,0.5)");*/
        //for(var key in results) {
    		//console.log('key: ' + key + '\n' + 'value: ' + results[key]);
		//}
        
      }
    });
  }


$(document).ready(function() {
    //console.log("hello");
    $("#csv-file").change(handleFileSelect);
    
    console.log("Done");
	
	
    
});

/*function readTextFile(file)
{

    var rawFile = new XMLHttpRequest();
    rawFile.open("GET", file, false);
    rawFile.onreadystatechange = function ()
    {
        if(rawFile.readyState === 4)
        {
            if(rawFile.status === 200 || rawFile.status == 0)
            {
                var allText = rawFile.responseText;
                alert(allText);
            }
        }
    }
    rawFile.send(null);
}*/



 /*ocpu.seturl("//public.opencpu.org/ocpu/library/utils/R")
    $("#submitbutton").on("click", function(){
    	var myfile = $("#csv-file")[0].files[0];
    	var req = ocpu.call("read.csv", {
        "file" : myfile,
        "header" : myheader
    	}, function(session){
        session.getConsole(function(outtxt){
            $("#output").text(outtxt); 
        });
    });
    });*/


//var file="file:///useroutput.txt"
    //readTextFile(file);
    
     /*$.ajax({
        type: "GET",
        url: "useroutput.csv",
        dataType: "text",
        success: function(data) {processData(data);}
     });*/
    /*var array0 = [16,80];
    var array2 = [18,19];
	for (var i = 0; i < array0.length; i++) {
    	var leftValue = array0[i];
    	var bottomValue = array0[i] ;
    	$( "#allDiv" ).add( "div" ).appendTo( document.body).addClass("orange").css("left", bottomValue).css("top",leftValue);
	}
    	
	function makeCells(ro) {
        var t = document.createElement("TABLE");

        for (var rows = 0; rows < ro; rows++) {
            var newRow = document.createElement("TR");
            
            t.appendChild(newRow);
            for (var cols = 0; cols < 12; cols++) {
                var newCell = document.createElement("TD");
                newRow.appendChild(newCell); 
            }
        }

        document.body.appendChild(t);
    }*/
    

    //makeCells(1);

/*function processData(allText) {
    var allTextLines = allText.split(/\r\n|\n/);
    var headers = allTextLines[0].split(',');
    var lines = [];

    for (var i=1; i<allTextLines.length; i++) {
        var data = allTextLines[i].split(',');
        if (data.length == headers.length) {

            var tarr = [];
            for (var j=0; j<headers.length; j++) {
                tarr.push(headers[j]+":"+data[j]);
            }
            lines.push(tarr);
        }
    }
     alert(lines);
}*/