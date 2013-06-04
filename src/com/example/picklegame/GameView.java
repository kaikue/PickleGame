package com.example.picklegame;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	
	class GameThread extends Thread {
		
		private Bitmap backgroundImage, wallImage;
		private boolean mRun = false;
		private SurfaceHolder mSurfaceHolder;
		private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;
        private int speedAdjust;
        private int scrollspeed;
        private int maxFallSpeed;
        private double gravity;
        private Player player;
        private ArrayList<Wall> terrain;
        private boolean updown;
        private int touchAction;
		
		public GameThread(SurfaceHolder surfaceHolder, Context context) {
			
            mSurfaceHolder = surfaceHolder;
            
            Resources res = context.getResources();

            backgroundImage = BitmapFactory.decodeResource(res, R.drawable.background);
            wallImage = BitmapFactory.decodeResource(res, R.drawable.wall);
            Bitmap playerImage = BitmapFactory.decodeResource(res, R.drawable.player);
            
            speedAdjust = 1; //shouldnt be needed but will use as currently on the fence between 2 styles
            scrollspeed = 4;
            maxFallSpeed = scrollspeed; //ONLY INITIALIZED ONCE REGARDLESS OF CHANGE IN SCROLLSPEED
            gravity = 0.05 * scrollspeed; //ONLY INITIALIZED ONCE REGARDLESS OF CHANGE IN SCROLLSPEED
            maxFallSpeed += speedAdjust; //manual adjustment since it is not relative to scrollspeed for now
            gravity += (0.05 * speedAdjust); //manual adjustment since it is not relative to scrollspeed for now
            player = new Player(playerImage, scrollspeed, speedAdjust);
            player.x = mCanvasWidth / 4;
            player.y = mCanvasHeight / 3;
            updown = false;
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
            
            canvas.drawBitmap(player.image, player.x, player.y, null);
            for(Wall wall : terrain) {
            	canvas.drawBitmap(wall.image, wall.x, wall.y - 4 * wall.height, null);
            	canvas.drawBitmap(wall.image, wall.x, wall.y + 4 * wall.height, null);
            }
            
        }
		
		private void update() {
			if(terrain == null) {
				terrain = new ArrayList<Wall>();
	            int x = 0;
	            for(int i = 0; i < mCanvasWidth / 32 + 3; i++) {
	            	Wall wall = new Wall(wallImage, x, mCanvasHeight - 64, wallImage.getWidth(), wallImage.getHeight(), i);
	            	terrain.add(wall);
	            	x += 32;
	            }
			}
			
			player.update(updown, gravity, maxFallSpeed);
			for(Wall wall : terrain) {
				wall.update(terrain, scrollspeed);
			}
		}
		
	}
	
	private GameThread thread;
	
	public GameView(Context context, AttributeSet attrs) {
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

	@Override
    public boolean onTouchEvent(MotionEvent event) {
		thread.touchAction = event.getAction();
		switch (thread.touchAction) {
		case MotionEvent.ACTION_UP:
			thread.updown = false;
			break;
		case MotionEvent.ACTION_DOWN:
			thread.updown = true;
			break;
		}
		//Log.d("PickleGame", "" + thread.updown);
			
		/*if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
			thread.updown = true;
		} else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
			thread.updown = false;
		}*/
        
    	return true;
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
