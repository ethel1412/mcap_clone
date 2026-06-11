
function getPublicKey(callback){

		$.ajax({
	                type: "GET",
	                url: "/key/get-publickey",
	           		contentType: "application/json; charset=utf-8",
	                async: false,
	                success: function(res) {
	                	callback(res);
	                },
	                error: function(xhr, status, errorThrown) {
					    alert("Error: " + errorThrown + "\nStatus: " + status + "\nResponse: " + xhr.responseText);
					}

	     });
	     
}

