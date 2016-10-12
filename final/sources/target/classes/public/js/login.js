$('html').animate({"opacity":1}, 500);
$('html').css('background-color', '#61799a'); 

$( document ).ready(function() {
	$( "#send" ).click(function() {
		$.ajax({
			type: "POST",
			url: "/",
			data: {
				'username': $("#username").val(),
				'password': $("#password").val()
			}
		}).done(function(data) {
			if(data == '0'){
				$('#error').html('<div id="error-content">Vos identifiants sont incorrects</div>');
			}
			else{
				$('#error').html('');

				$.ajax({
					type: "GET",
					url: "/"
				}).done(function(data) {
					$('html').animate({"opacity":0}, 500, function(){
						window.location.href = "/";
					});
				});
			}
		});
	});

	$("#username, #password").on('keypress', function(e) {
	    var tag = e.target.tagName.toLowerCase();

	    if ( e.which === 13) 
	        $("#send").trigger("click");
	});
});