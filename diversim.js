var canvas = document.querySelector('canvas');

canvas.width = 800;
canvas.height = 500;

var c = canvas.getContext('2d');

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
			this.y = this.initialY;
		
		this.draw();
	}
}

function Compartment (ppNitrogen, halftime)
{
	this.ppNitrogen = ppNitrogen;
	this.halftime = halftime;

	this.updateNitrogen = function(pp, time)
	{
		/*
		* Haldane's formula to calculate the tissue nitrogen content goes
        * like this:
        *    pp(t) = pp(t-1) + (ppat-pp(t-1))*(1-2^(-t/Ht))
        *
        * where pp(t) = nitrogen partial pressure at time t
        *       pp(t-1) = nitrogen pp at time t-1
        *       ppat = nitrogen pp in atmosphere
        *       t = time tissue exposed to ppat
        *       Ht = tissue halftime
		*/
		this.ppNitrogen = this.ppNitrogen+(pp-this.ppNitrogen)*
			(1-Math.pow(2,(-time/this.halftime)));

		/*
		* If the pp nitrogen is more than twice the ambient pp nitrogen
		* then by Haldane's model, nitrogen bubbles will develop in this
		* tissue. Therefore ascertain whether or not this applies to this
		* tissue.
		*/		
		this.bubbles = (this.ppNitrogen > (pp * 2));
	}
	
	this.textDescription = function()
	{
		if (this.bubbles)
			return "!" + this.ppNitrogen + "!";
		else
			return this.ppNitrogen;
	}
}

function Model(surfacePPNitrogen, unitsDepthPerAtmos, halftimes, startingAmbPress, gasNitrogenFraction)
{
	this.unitsDepthPerAtmos = unitsDepthPerAtmos;
	this.gasNitrogenFraction = gasNitrogenFraction;
	this.lastAmbPressure = startingAmbPress;
	this.lastDiveTime = 0;
	
	this.compartments = [];
	for (var i =0; i < halftimes.length; i++)
	{
		this.compartments.push(new Compartment(surfacePPNitrogen, halftimes[i]));
	}

	this.processSample = function(newDepth, newTime)
	{
		// Whats the ambient pressure of the new depth
		var newAmbPress = newDepth / this.unitsDepthPerAtmos + atmosphericPressure;
		
		// Whats the average ambient pressure for the period we are sampling
		var avAmbPress = (this.lastAmbPressure + newAmbPress) / 2;
		
		// Convert the average ambient pressure to the equivalent nitrogen partial pressure
		var avNitroPP = avAmbPress * gasNitrogenFraction;
		
		// Work out the length of tie for the period we are sampling
		var sampleLength = newTime - this.lastDiveTime;
		
		// Ask the compartments to udpate their nitrogen levels
		for (var i=0; i<this.compartments.length; i++)
		{
			this.compartments[i].updateNitrogen(avNitroPP,sampleLength);
		}
		
		// Store the last values so we can reference them the next time we update
		this.lastAmbPressure = newAmbPress;
		this.lastDiveTime = newTime;
	}
	
	// Return the current ambient pressure we are modelling
	this.getAmbientNitrogenPP = function()
	{
		return this.lastAmbPressure * gasNitrogenFraction;
	}
	
	this.textDescription = function()
	{
		var result = "Nit Pressure: " + this.getAmbientNitrogenPP() + ", [";
		for (var i=0; i<this.compartments.length; i++)
			result += this.compartments[i].textDescription()+",";
		
		return result;
	}
}

// Model constants
var surfacePPNitrogen = 0.79;
var unitsDepthPerAtmosphere = 10;
var gasNitrogenFraction = .79;
var startingAmbPress = 1;
var maxDepth = 40;
var minDepth = 0;
var halftimes = [5,10,20,40,75];
var atmosphericPressure = 1;
var minutesPerClick = 1;

// Current state
var diveDepth = 0;
var diveTime = 0;

var model = new Model(surfacePPNitrogen, unitsDepthPerAtmosphere, halftimes, startingAmbPress, gasNitrogenFraction);

// Update the model every half second
var myVar = setInterval(updateModel, 1000);

function updateModel()
{
	model.processSample(diveDepth, diveTime);
	diveTime+=minutesPerClick;
	
	console.log("Dive time: " + diveTime + ", depth: " + diveDepth + " " + model.textDescription());
}



function animate()
{
	requestAnimationFrame(animate);
	
	// Animate stuff
	
	

	//c.clearRect(0,0,innerWidth, innerHeight);
	
	//for (var i=0; i<objectsArray.length; i++)
	//{
	//	objectsArray[i].update();
	//}
	
}

function moveUp()
{
	diveDepth--;
}

function moveDown()
{
	diveDepth++;
}

animate();