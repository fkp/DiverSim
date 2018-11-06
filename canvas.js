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
}

window.addEventListener('mousemove',
	function(event) {
	mouse.x = event.x;
	mouse.y = event.y;
})

window.addEventListener('resize', function()
{
	canvas.width = window.innerWidth;
	canvas.height = window.innerHeight;
	
	init();
});

var hitTest = 50;
var maxRadius = 40;

var colourArray = [
	'#ffaa33',
	'#99ffaa',
	'#00ff00',
	'#4411aa',
	'#ff1100'];

function Circle(x, y, dx, dy, radius, minRadius)
{
	this.x = x;
	this.y = y;
	this.dx = dx;
	this.dy = dy;
	this.radius = radius;
	this.colour = colourArray[Math.floor(Math.random() * colourArray.length)];
	this.minRadius = radius;
	
	this.draw = function()
	{
		c.beginPath();
		c.arc(this.x,this.y,this.radius,0, Math.PI * 2, false);
		//c.strokeStyle = 'blue';
		c.fillStyle = this.colour;
		c.fill();
		c.stroke();
	}
	
	this.update = function()
	{
		if (this.x+this.radius>innerWidth || this.x-radius<0)
		{
			this.dx = -this.dx;
		}
	
		if (this.y+this.radius>innerHeight || this.y-this.radius<0)
		{
			this.dy = -this.dy;
		}
	
		this.x+=this.dx;
		this.y+=this.dy;
		
		if (Math.abs(mouse.x - this.x) < hitTest && Math.abs(mouse.y - this.y) < hitTest && this.radius < maxRadius) {
			this.radius +=1;
		} else if (this.radius > this.minRadius) {
			this.radius -=1;
		}
		
		this.draw();
	}
}

function Rectangle(x, y, dx, dy, height, width)
{
	this.x = x;
	this.y = y;
	this.dx = dx;
	this.dy = dy;
	this.height = height;
	this.width = width;
	
	this.draw = function()
	{
		c.fillStyle = 'rgba(255,0,0,0.5)';
		c.fillRect(this.x,this.y,width,height);
	}
	
	this.update = function()
	{
		if (this.x+this.width>innerWidth || this.x-width<0)
		{
			this.dx = -this.dx;
		}
	
		if (this.y+this.height>innerHeight || this.y-this.height<0)
		{
			this.dy = -this.dy;
		}
	
		this.x+=this.dx;
		this.y+=this.dy;
		
		this.draw();
	}
}


var objectsArray = [];

var speedMultiplier = 2;

var rectHeight = 30;
var rectWidth = 30;

var noCircles = 100;
var noRectangles = 10;

init();

function init()
{
	objectsArray = [];
	
	for (var i =0; i < noCircles; i++)
	{
		var radius = Math.random() * 6 + 1;
		
		objectsArray.push(new Circle(Math.random() * (innerWidth-radius*2)+radius,
			Math.random() * (innerHeight-radius*2)+radius,
			(Math.random() - 0.5) * speedMultiplier,
			(Math.random() - 0.5) * speedMultiplier,
			radius));
	}

	for (var i =0; i < noRectangles; i++)
	{
		objectsArray.push(new Rectangle(Math.random() * (innerWidth-rectWidth*2)+rectWidth,
			Math.random() * (innerHeight-rectHeight*2)+rectHeight,
			(Math.random() - 0.5) * speedMultiplier,
			(Math.random() - 0.5) * speedMultiplier,
			rectWidth,
			rectHeight));
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