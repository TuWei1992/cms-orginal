(function(){
var now = { row:1, col:1 }, last = { row:0, col:0};
const towards = { up:1, right:2, down:3, left:4};
var isAnimating = false, isLoaded = true;
var s=window.innerHeight/460;
var ss=320*(1-s);

$('.wrap').css('-webkit-transform','scale('+s+','+s+') translate(0px,-'+ss+'px)');

// start
window.onload = function(){
	$('.loading').hide();
	isLoaded = false;
	$('.page-1-1').removeClass('hide');
	$('.page-1-1').addClass('page-current');
	isAnimating = false;

}

document.addEventListener('touchmove',function(event){
	event.preventDefault(); },false);

$(document).swipeUp(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row != 7) { now.row = last.row+1; now.col = 1; pageMove(towards.up);}	
	if (last.row == 7) { now.row = 1; now.col = 1; pageMove(towards.up);}
})

$(document).swipeDown(function(){
	if(isLoaded) return;
	if (isAnimating) return;
	last.row = now.row;
	last.col = now.col;
	if (last.row!=1) { now.row = last.row-1; now.col = 1; pageMove(towards.down);}	
})

$('#carPic').click(function(){
	var str = $(this).attr('src');
	$('#carBigPic img').attr('src',str);
	$('#carBigPic').show();
});
$('#carBigPic').click(function(){
	$(this).hide();
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
	

	setTimeout(function(){
		$(lastPage).removeClass('page-current');
		$(lastPage).removeClass(outClass);
		$(lastPage).addClass("hide");
		
		$(nowPage).addClass('page-current');
		$(nowPage).removeClass(inClass);
		isAnimating = false;
	},600);
}


})();