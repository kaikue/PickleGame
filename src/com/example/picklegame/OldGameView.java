package com.example.picklegame;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OldGameView extends SurfaceView implements SurfaceHolder.Callback {
	
	class GameThread extends Thread {
		
		private Bitmap backgroundImage, playerImage, wallImage;
		private boolean mRun = false;
		private SurfaceHolder mSurfaceHolder;
		private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;
        private int playerX, playerY, speed;
        private int maxDist, h, wallWidth, wallHeight;
        private ArrayList<Wall> wallsTop, wallsBottom;
		
		public GameThread(SurfaceHolder surfaceHolder, Context context) {
			
            mSurfaceHolder = surfaceHolder;
            
            Resources res = context.getResources();

            backgroundImage = BitmapFactory.decodeResource(res, R.drawable.background);
            playerImage = BitmapFactory.decodeResource(res, R.drawable.player);
            wallImage = BitmapFactory.decodeResource(res, R.drawable.wall);
            
            playerX = 50;
            playerY = 50;
            speed = 10;
            maxDist = 100;
            wallWidth = wallImage.getWidth();
            wallHeight = wallImage.getHeight();
        }
		
		public void setRunning(boolean b) {
            mRun = b;
        }
		
		/* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // don't forget to resize the background image
                backgroundImage = Bitmap.createScaledBitmap(backgroundImage, width, height, true);
            }
        }
        
		@Override
	    public void run() {
	        while (mRun) {
	            Canvas c = null;
	            try {
	                c = mSurfaceHolder.lockCanvas(null);
	                synchronized (mSurfaceHolder) {
	                    update();
	                    doDraw(c);
	                }
	            } finally {
	                // do this in a finally so that if an exception is thrown
	                // during the above, we don't leave the Surface in an
	                // inconsistent state
	                if (c != null) {
	                    mSurfaceHolder.unlockCanvasAndPost(c);
	                }
	            }
	        }
	    }
		
		private void doDraw(Canvas canvas) {
            // Draw the background image. Operations on the Canvas accumulate
            // so this is like clearing the screen.
            canvas.drawBitmap(backgroundImage, 0, 0, null);

            canvas.drawBitmap(playerImage, playerX, playerY, null);
            for(Wall wallTop : wallsTop) {
            	canvas.drawBitmap(wallImage, wallTop.x, wallTop.y, null);
            }
            for(Wall wallBottom : wallsBottom) {
            	canvas.drawBitmap(wallImage, wallBottom.x, wallBottom.y, null);
            }
            
        }
		
		private void update() {
			if(wallsTop == null) {
				wallsTop = new ArrayList<Wall>();
	            wallsBottom = new ArrayList<Wall>();
	            h = maxDist / 2;
	            for(int x = 0; x < mCanvasWidth; x += wallWidth) {
	            	addWalls(x);
	            }
			}
			for(int i = 0; i < wallsTop.size(); i++) {
				wallsTop.get(i).x -= speed;
				wallsBottom.get(i).x -= speed;
			}
			if(wallsTop.get(0).x + wallWidth < 0) {
				wallsTop.remove(0);
				wallsBottom.remove(0);
				addWalls(mCanvasWidth);
			}
		}
		
		private void addWalls(int x) {
			x = x / wallWidth * wallWidth;
			if(new Random().nextBoolean()) {
        		h += 15;
        	}
        	else {
        		h -= 15;
        	}
        	h = Math.max(0, h);
        	h = Math.min(maxDist, h);
        	wallsTop.add(new Wall(wallImage, x, h, wallWidth, wallHeight, 0));
        	wallsBottom.add(new Wall(wallImage, x, mCanvasHeight + h - wallHeight - maxDist, wallWidth, wallHeight, 0));
        	}
	}
	
	private GameThread thread;
	
	public OldGameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new GameThread(holder, context);

        setFocusable(true); // make sure we get key events
    }
	
	public GameThread getThread() {
        return thread;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        thread.setSurfaceSize(width, height);
    }
    
    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
