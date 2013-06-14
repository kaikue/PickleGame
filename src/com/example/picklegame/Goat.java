package com.example.picklegame;

import android.graphics.Bitmap;

public class Goat extends GameObject {
	
	//public int x, y, 
	public int width, height, index;
	//public Bitmap image;
	
	public Goat(Bitmap image, int x, int y, int width, int height) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	/*public boolean collides(int x1, int y1, int x2, int y2) {
		return x <= x2 && x1 <= x + width && y <= y2 && y1 <= y + height;
	}*/
	
	public boolean update(int scrollspeed) {
        x -= scrollspeed;
        if(x < -32) {
            return true;
        }
        return false;
	}
	
}
