$(function(){
	$('.input-text').on('keyup',function(e){
		if(e.keyCode ==13){
			login($('.login-button'));
		}
		if($(this).val().length>0){
			$(this).parents('.input-item').find('.hint').hide();
		}
		else{
			$(this).parents('.input-item').find('.hint').show();
		}
	});
	$('.input-text').focus(function(){
		$(this).parents('.input-item').removeClass('error');
	});
	$('.input-text').blur(function(){
		if($(this).attr('data-reg')&&$(this).val().length>0){
			var patt = new RegExp($(this).attr('data-reg'));
			if(patt.test($(this).val())){
				$(this).parents('.input-item').removeClass('error').addClass('ok');
			}
			else{
				$(this).parents('.input-item').addClass('error').removeClass('ok');
			}
		}
		if($(this).val().length ==0){
			$(this).parents('.input-item').removeClass('error').removeClass('ok');
		}
	});
	var login = function($this){
		var $input = $('.input-item');
		var isValid = true;
		for(var i=0,l = $input.length;i<l;i++){
			if($input.eq(i).find('.input-text').val().length==0){
				$input.eq(i).addClass('error');
			}
			if($input.eq(i).hasClass('error')){
				isValid = false;
			}
		}
		if(!isValid){
			return;
		}
		var host = $('.j-host').val();
		var user = $('.j-user').val();
		var password = $('.j-password').val();
		$this.addClass('loading');
		$.post('rest/login',{
			host: host,
			userName: user,
			passwd: password
		},function(resp){
			$this.removeClass('loading');
			if(resp.success){
				window.location.href='index.html';
			}
			else{
				$('.j-password').parents('.input-item').addClass('error');
			}
		});
	};
	$('.login-button').on('click',function(){
		var $this = $(this);
		login($this);
		


	});


});