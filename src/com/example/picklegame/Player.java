package com.example.picklegame;

import android.graphics.Bitmap;
import android.util.Log;

public class Player extends GameObject {
	
	//public Bitmap image;
	//public int x, y;
	public double yVel, liftIncrement, maxRiseSpeed;
	
	public Player(Bitmap image, int scrollSpeed, int speedAdjust) {
		this.image = image;
		//y = 0;
		yVel = 0;
		liftIncrement = .1 * scrollSpeed; //ONLY INITIALIZED ONCE REGARDLESS OF CHANGE IN SCROLLSPEED
		maxRiseSpeed = .75 * scrollSpeed; //ONLY INITIALIZED ONCE REGARDLESS OF CHANGE IN SCROLLSPEED
		liftIncrement += (.1*speedAdjust); //manual adjustment since it is not relative to scrollspeed for now
		maxRiseSpeed += (.75*speedAdjust); //manual adjustment since it is not relative to scrollspeed for now
	}
	
	public void update(boolean updown, double gravity, int maxFallSpeed) {
		if(updown) {
			//Log.d("PickleGame", "Moving up");
			yVel += liftIncrement - gravity;
			if(yVel > maxRiseSpeed) {
				yVel = maxRiseSpeed;
		    }
		}
		else {
			//Log.d("PickleGame", "Moving down");
			yVel -= gravity;
			if(yVel < -maxFallSpeed) {
				yVel = -maxFallSpeed;
			}
		}
		y -= yVel;
	//Log.d("PickleGame", "updown: " + updown + " yvel: " + yVel);
	}
}
