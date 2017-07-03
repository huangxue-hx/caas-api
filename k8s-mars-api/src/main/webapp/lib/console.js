$(function(){

	var process = function(val){
		if(val.length ==0){
			var content = $('.hint').html();
			$('#cursor').before('<p class="line">'+content+'</p>');
		}
		else if(val =='ls'){
			var content = $('.hint').html()+' '+val;
			$('#cursor').before('<p class="line">'+content+'</p>');
			var content1 = 'hello&nbsp;&nbsp;&nbsp;&nbsp;world&nbsp;&nbsp;&nbsp;&nbsp;demo';
			$('#cursor').before('<p class="line">'+content1+'</p>');
		}
		else if(val =='cd hello'){
			var content = $('.hint').html()+' '+val;
			$('#cursor').before('<p class="line">'+content+'</p>');
			$('.hint').html('root@container22323444:/hello#');
		}
		else if(val =='cd world'){
			var content = $('.hint').html()+' '+val;
			$('#cursor').before('<p class="line">'+content+'</p>');
			$('.hint').html('root@container22323444:/world#');
		}
		else if(val =='cd demo'){
			var content = $('.hint').html()+' '+val;
			$('#cursor').before('<p class="line">'+content+'</p>');
			$('.hint').html('root@container22323444:/demo#');
		}
		else if(val =='cd ..'){
			var content = $('.hint').html()+' '+val;
			$('#cursor').before('<p class="line">'+content+'</p>');
			$('.hint').html('root@container22323444:/#');
		}
		else{
			var content = $('.hint').html()+' '+val;
			$('#cursor').before('<p class="line">'+content+'</p>');
			var content1 = val + ': command not found';

			$('#cursor').before('<p class="line">'+content1+'</p>');
		}
	};
	$('.cursor').focus();
	$('.cursor').keydown(function(e){
		console.log(e);
		if(e.originalEvent.keyCode == 13){
			var val = $(this).val();
			process(val);
			$(this).val('');
		}
	});
});