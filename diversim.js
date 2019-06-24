var canvas = document.querySelector('canvas');

canvas.width = 600;
canvas.height = 500;

var c = canvas.getContext('2d');

var stage, output;

stage = new createjs.Stage("diverCanvas");
// For mobile devices.
createjs.Touch.enable(stage);

stage.mouseMoveOutside = true;

var circle = new createjs.Shape();
circle.graphics.beginFill("red").drawCircle(0, 0, 50);

var label = new createjs.Text("drag me", "bold 14px Arial", "#FFFFFF");
label.textAlign = "center";
label.y = -7;

var dragger = new createjs.Container();
dragger.x = 500;
dragger.y = 100;
dragger.addChild(circle, label);
stage.addChild(dragger);

dragger.on("pressmove",function(evt) {
    // currentTarget will be the container that the event listener was added to:
    //evt.currentTarget.x = evt.stageX;
    evt.currentTarget.y = evt.stageY;
    // make sure to redraw the stage to show the change:
    stage.update();
});

stage.update();

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

function GraphParams (x, y, width, height, minGraphValue, maxGraphValue)
{
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
	this.minGraphValue = minGraphValue;
	this.maxGraphValue = maxGraphValue;
}

function Compartment (ppNitrogen, halftime, graphParams)
{
	this.ppNitrogen = ppNitrogen;
	this.halftime = halftime;
	this.graphParams = graphParams;
	this.graphElement;
	this.ppReadingText;

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

	this.init = function()
	{
	    // Setup the bar graph
	    this.graphElement = new createjs.Shape();
	    this.ppReadingText = new createjs.Text();
	    this.ppReadingText.x = this.graphParams.x + border;
        stage.addChild(this.graphElement);
        stage.addChild(this.ppReadingText);
	}

	this.update = function()
	{
		var percent = this.ppNitrogen / (this.graphParams.maxGraphValue - this.graphParams.minGraphValue);
		var fillStyle;

		if (this.bubbles)
			fillStyle = 'rgba(255,0,0,0.5)';
		else
			fillStyle = 'rgba(0,0,255,0.5)';

        this.graphElement.graphics.clear().beginFill(fillStyle).drawRect
            (this.graphParams.x,
            this.graphParams.y + this.graphParams.height,
            this.graphParams.width,
            -this.graphParams.height * percent).endFill();

        this.ppReadingText.text = this.ppNitrogen.toFixed(3);
        this.ppReadingText.y = this.graphParams.height - this.graphParams.height*percent;
	}
	
	this.textDescription = function()
	{
		if (this.bubbles)
			return "!" + this.ppNitrogen + "!";
		else
			return this.ppNitrogen;
	}
}

function Model(surfacePPNitrogen, unitsDepthPerAtmos, halftimes, startingAmbPress, gasNitrogenFraction, graphParams)
{
	this.unitsDepthPerAtmos = unitsDepthPerAtmos;
	this.gasNitrogenFraction = gasNitrogenFraction;
	this.lastAmbPressure = startingAmbPress;
	this.lastDiveTime = 0;

	// Spaces between compartment bars
	this.graphBuffer = border * 2;
	this.depth = 0;

	// How to divide the screen between the compartments and the diver display
	this.divider = 2;

	this.graphParamsPane = new GraphParams
	    (graphParams.x,
	    graphParams.y,
	    graphParams.width / this.divider,
	    graphParams.height,
	    graphParams.minGraphValue,
	    graphParams.maxGraphValue);
	
	this.graphParamsDiver = new GraphParams
	    (graphParams.x + graphParams.width / this.divider,
	    graphParams.y,
	    graphParams.width / this.divider,
	    graphParams.height,
	    graphParams.minGraphValue,
	    graphParams.maxGraphValue);

	// How much space to increment each graph by
	var graphSpace = (this.graphParamsPane.width / halftimes.length) + (this.graphBuffer / halftimes.length);

	this.compartments = [];
	for (var i =0; i < halftimes.length; i++)
	{
		var compartmentParams = new GraphParams
			(this.graphParamsPane.x + graphSpace*i,
			this.graphParamsPane.y,
			graphSpace - this.graphBuffer,
			this.graphParamsPane.height - border,
			this.graphParamsPane.minGraphValue,
			this.graphParamsPane.maxGraphValue);

		var compart = new Compartment(surfacePPNitrogen, halftimes[i], compartmentParams);
		compart.init();
		this.compartments.push(compart);
	}

    // Outlines of the graph and diver panes
    //c.fillStyle = 'rgba(0,0,0)';
    var panel1 = new createjs.Shape();
    panel1.graphics.drawRect(this.graphParamsPane.x, this.graphParamsPane.y, this.graphParamsPane.width, this.graphParamsPane.height - border);
    var panel2 = new createjs.Shape();
    panel2.graphics.drawRect(this.graphParamsDiver.x, this.graphParamsDiver.y, this.graphParamsDiver.width, this.graphParamsDiver.height - border);
    stage.addChild(panel1);
    stage.addChild(panel2);

/*    var paneHeight = this.graphParamsPane.y + this.graphParamsPane.height
    var yCoord = paneHeight - (paneHeight * (this.getAmbientNitrogenPP() / (this.graphParamsPane.maxGraphValue - this.graphParamsPane.minGraphValue)));

    //this.getAmbientNitrogenPP() / this.graphParamsPane.maxGraphValue - this.graphParamsPane.minGraphValue + this.graphParamsPane.y;

    c.beginPath();
    c.moveTo(this.graphParamsPane.x, yCoord);
    c.lineTo(this.graphParamsPane.x+this.graphParamsPane.width, yCoord);
    c.stroke();*/

    //c.fillText(this.graphParamsPane.x + " " + this.graphParamsPane.y + " " + this.graphParamsPane.width + " " + this.graphParamsPane.height, this.graphParamsPane.x,this.graphParamsPane.y +border);
    //c.fillText(this.getAmbientNitrogenPP() + " " + yCoord, this.graphParamsPane.x,this.graphParamsPane.y +border*2);
    c.fillText("Depth: " + this.depth + ", Time: " + this.lastDiveTime,this.graphParamsPane.x,this.graphParamsPane.y);

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
		
		// Ask the compartments to update their nitrogen levels
		for (var i=0; i<this.compartments.length; i++)
		{
			this.compartments[i].updateNitrogen(avNitroPP,sampleLength);
		}
		
		// Store the last values so we can reference them the next time we update
		this.lastAmbPressure = newAmbPress;
		this.depth = newDepth;
		this.lastDiveTime = newTime;
	}
	
	// Return the current ambient pressure we are modelling
	this.getAmbientNitrogenPP = function()
	{
		return this.lastAmbPressure * gasNitrogenFraction;
	}
	
	this.update = function()
	{
		// Ask the compartments to redraw
		for (var i=0; i<this.compartments.length; i++)
		{
			this.compartments[i].update();
		}
    }

	this.draw = function()
	{
		// Ask the compartments to redraw
		for (var i=0; i<this.compartments.length; i++)
		{
			this.compartments[i].draw();
		}

        // Outlines of the graph and diver panes
        c.fillStyle = 'rgba(0,0,0)';
        c.rect(this.graphParamsPane.x, this.graphParamsPane.y, this.graphParamsPane.width, this.graphParamsPane.height - border);
        c.stroke();
        c.rect(this.graphParamsDiver.x, this.graphParamsDiver.y, this.graphParamsDiver.width, this.graphParamsDiver.height - border);
        c.stroke();

        // Draw where the ambient pressure is
        var paneHeight = this.graphParamsPane.y + this.graphParamsPane.height
        var yCoord = paneHeight - (paneHeight * (this.getAmbientNitrogenPP() / (this.graphParamsPane.maxGraphValue - this.graphParamsPane.minGraphValue)));

        //this.getAmbientNitrogenPP() / this.graphParamsPane.maxGraphValue - this.graphParamsPane.minGraphValue + this.graphParamsPane.y;

        c.beginPath();
        c.moveTo(this.graphParamsPane.x, yCoord);
        c.lineTo(this.graphParamsPane.x+this.graphParamsPane.width, yCoord);
        c.stroke();

        //c.fillText(this.graphParamsPane.x + " " + this.graphParamsPane.y + " " + this.graphParamsPane.width + " " + this.graphParamsPane.height, this.graphParamsPane.x,this.graphParamsPane.y +border);
        //c.fillText(this.getAmbientNitrogenPP() + " " + yCoord, this.graphParamsPane.x,this.graphParamsPane.y +border*2);
        c.fillText("Depth: " + this.depth + ", Time: " + this.lastDiveTime,this.graphParamsPane.x,this.graphParamsPane.y);
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
var minutesPerUpdate = 1;

// Current state
var diveDepth = 0;
var diveTime = 0;

// Graphics
var border = 10;

var model = new Model(surfacePPNitrogen, unitsDepthPerAtmosphere, halftimes, startingAmbPress, gasNitrogenFraction, new GraphParams
	(border,  	// X
	border,	// Y
    canvas.width - border*2,	// width
	canvas.height - border,	// height
	0,		// minGraphValue
	4));	// maxGraphValue

// Update the model every half second
var myVar = setInterval(updateModel, 100);

function updateModel()
{
	model.processSample(diveDepth, diveTime);
	diveTime+=minutesPerUpdate;
	
	//console.log("Dive time: " + diveTime + ", depth: " + diveDepth + " " + model.textDescription());
}

function tick(event)
{
	//requestAnimationFrame(animate);
	
	// Animate stuff
	//c.clearRect(0,0,canvas.width, canvas.height);

//console.log("fktemp");

	model.update();
	stage.update(event);
}

function moveUp()
{
	diveDepth-=5;
}

function moveDown()
{
	diveDepth+=5;
}


createjs.Ticker.timingMode = createjs.Ticker.RAF_SYNCHED;
createjs.Ticker.framerate = 30;
createjs.Ticker.on("tick", tick);
//animate();