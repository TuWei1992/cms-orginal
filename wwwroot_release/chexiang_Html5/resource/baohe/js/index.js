(function(){
// alert(window.screen.width+'/'+window.screen.height);
// alert(document.documentElement.clientHeight);
var now = { row:1, col:1 }, last = { row:0, col:0};
const towards = { up:1, right:2, down:3, left:4};
var isAnimating = false, isLoaded = true;
var timer;
// start
var hash = location.hash, anchor = hash.indexOf('#page'),arr=[];
window.onload = function(){
	$('.loading').hide();
	isLoaded = false;
	if(hash && anchor >-1){
		arr=hash.split('-');
		$(hash).removeClass('hide');
		if(hash.indexOf('page-5-1')> -1){
			if($('.pop-work').hasClass('slideOutDown')){
				$('.pop-work').removeClass('slideOutDown');
			}
			timer=setTimeout(function(){
					$('.pop-work').addClass('slideOutDown');
				},4000);
		}

		now.row = arr[1]*1;
		now.col = arr[2]*1;
		timer=setTimeout(function(){
			clearTimeout(timer);
			$(hash).addClass('page-current');
			isAnimating = false;

		},600);

	}else{
		$('.page-1-1').removeClass('hide');
		$('.page-1-1').addClass('page-current');
		isAnimating = false;
	}

}

document.addEventListener('touchmove',function(event){
	event.preventDefault(); },false);

$(document).swipeUp(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row != 7) { now.row = last.row+1; now.col = 1; pageMove(towards.up);}	
})

$(document).swipeDown(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row!=1) { now.row = last.row-1; now.col = 1; pageMove(towards.down);}	
})

$(document).swipeLeft(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row>1 && last.row<7) { 
		now.row = last.row;
		 if(last.col<6 && last.row == 3){
		 	now.col = now.col+1;
			pageMove(towards.left);
		 }
	}
});

$('.arrow-left').click(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row>1 && last.row<7) {
		now.row = last.row;
		 if(last.col>1 && last.row == 3){
			now.col=now.col-1;
			pageMove(towards.right);
		 }
	}
});

$('.arrow-right').click(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row>1 && last.row<7) { 
		now.row = last.row;
		 if(last.col<6 && last.row == 3){
		 	now.col = now.col+1;
			pageMove(towards.left);
		 }
	}
});
$('#carPic').click(function(){
	var str = $(this).attr('src');
	$('#carBigPic img').attr('src',str);
	$('#carBigPic').show();
});
$('#carBigPic').click(function(){
	$(this).hide();
});

$(document).swipeRight(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row>1 && last.row<7) {
		now.row = last.row;
		 if(last.col>1 && last.row == 3){
			now.col=now.col-1;
			pageMove(towards.right);
		 }
	}
});

function pageMove(tw){
	var lastPage = ".page-"+last.row+"-"+last.col,
		nowPage = ".page-"+now.row+"-"+now.col;

	switch(tw) {
		case towards.up:
			outClass = 'pt-page-moveToTop';
			inClass = 'pt-page-moveFromBottom';
			break;
		case towards.right:
			outClass = 'pt-page-moveToRight';
			inClass = 'pt-page-moveFromLeft';
			break;
		case towards.down:
			outClass = 'pt-page-moveToBottom';
			inClass = 'pt-page-moveFromTop';
			break;
		case towards.left:
			outClass = 'pt-page-moveToLeft';
			inClass = 'pt-page-moveFromRight';
			break;
	}
	isAnimating = true;
	$(nowPage).removeClass("hide");
	
	$(lastPage).addClass(outClass);
	$(nowPage).addClass(inClass);
	

	timer=setTimeout(function(){
		clearTimeout(timer);
		$(lastPage).removeClass('page-current');
		$(lastPage).removeClass(outClass);
		$(lastPage).addClass("hide");
		//$(lastPage).find("img").addClass("hide");
		
		$(nowPage).addClass('page-current');
		$(nowPage).removeClass(inClass);
		if(nowPage == '.page-5-1'){
			if($('.pop-work').hasClass('slideOutDown')){
				$('.pop-work').removeClass('slideOutDown');
			}
			timer=setTimeout(function(){
				$('.pop-work').addClass('slideOutDown');
			},4000);
		}
		isAnimating = false;
	},600);
}


})();