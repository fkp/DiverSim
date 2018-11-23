var canvas = document.querySelector('canvas');

canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

var c = canvas.getContext('2d');

// Lines
/*c.beginPath();
c.moveTo(50,300);
c.lineTo(300,100);
c.lineTo(400,300);
c.strokeStyle = "#fa34a3";
c.stroke();*/

var mouse = {
	x: undefined,
	y: undefined
};

window.addEventListener('mousemove',
	function(event) {
	mouse.x = event.x;
	mouse.y = event.y;
});

//window.addEventListener('

window.addEventListener('resize', function()
{
	canvas.width = window.innerWidth;
	canvas.height = window.innerHeight;
	
	init();
});

var hitTest = 50;
var maxRadius = 40;

function Bubble (x, y, radius, xIncrement, yIncrement, sizeIncrement, colour)
{
	this.x = x;
	this.y = y;
	this.initialX = x;
	this.initialY = y;
	this.radius = radius;
	this.xIncrement = xIncrement;
	this.yIncrement = yIncrement;
	this.sizeIncrement = sizeIncrement;
	this.colour = colour;

	this.draw = function()
	{
		c.beginPath();
		c.arc(this.x,this.y,this.radius,0, Math.PI * 2, false);
		c.fillStyle = this.colour;
		c.fill();
		c.stroke();
	}
	
	this.update = function()
	{
		this.x += this.xIncrement;
		this.y += this.yIncrement;
		this.size += this.sizeIncrement;
		
		if (this.y < 0)
			this.y = this.initialY
		
		this.draw();
	}
}

//function Compartment
	
var objectsArray = [];

var speedMultiplier = 2;

var rectHeight = 30;
var rectWidth = 30;
var bubbleStartRadius = 3;

var noCircles = 100;
var noRectangles = 10;

init();

function init()
{
	objectsArray = [];
	
	for (var i =0; i < noCircles; i++)
	{
		objectsArray.push(new Bubble(Math.random() * (innerWidth-bubbleStartRadius*2)+bubbleStartRadius,
			innerHeight,
			bubbleStartRadius,
			0,
			-2,
			3,
			'#00ADB5'));
	}
}


function animate()
{
	requestAnimationFrame(animate);

	c.clearRect(0,0,innerWidth, innerHeight);
	
	for (var i=0; i<objectsArray.length; i++)
	{
		objectsArray[i].update();
	}
	
}

animate();