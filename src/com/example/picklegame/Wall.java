package com.example.picklegame;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Bitmap;

public class Wall extends GameObject {
	
	//public int x, y, 
	public int width, height, index;
	//public Bitmap image;
	
	public Wall(Bitmap image, int x, int y, int width, int height, int index) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.index = index;
	}
	
	public boolean collides(int x1, int y1, int x2, int y2) {
		return x <= x2 && x1 <= x + width && y <= y2 && y1 <= y + height;
	}
	
	public void update(ArrayList<Wall> terrain, int scrollspeed) {
        x -= scrollspeed;
        if(x < -32) {
            x += (32 * terrain.size());
            int othery = getNeighbor(terrain, index).y;
            y = othery + new Random().nextInt(64) - 32;
            if(y < -128) {
                y = -128;
            }
            if(y > 128) {
                y = 128;
            }
        }
	}
	
    private static Wall getNeighbor(ArrayList<Wall> terrain, int index) {
        int otherIndex = index - 1;
        if(index == 0) {
            otherIndex = terrain.size() - 1;
        }
        return terrain.get(otherIndex);
    }
}
