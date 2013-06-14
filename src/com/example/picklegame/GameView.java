package com.example.picklegame;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.LinearLayout;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	
	class GameThread extends Thread {
		
		private Bitmap backgroundImage, wallImage, breadImage, goatImage;
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
        private ArrayList<Bread> collectables;
        private ArrayList<Goat> enemies;
        private boolean updown;
        private int touchAction;
        private boolean playing;
		private double randomNum;
        private boolean collisionCheck1;
        private boolean collisionCheck2;
        private boolean justStarted;
        private int halfHeight = 0;
        private double breadSpawnChancePercent = 20; //out of 1000 not 100
        private double goatSpawnChancePercent = 7; //out of 1000 not 100
        private TextView tv = new TextView(getContext());
        private LinearLayout layout = new LinearLayout(getContext());
        private int score = 0;
		
		public GameThread(SurfaceHolder surfaceHolder, Context context) {
			
            mSurfaceHolder = surfaceHolder;
            
            Resources res = context.getResources();

            backgroundImage = BitmapFactory.decodeResource(res, R.drawable.background);
            wallImage = BitmapFactory.decodeResource(res, R.drawable.wall);
            breadImage = BitmapFactory.decodeResource(res, R.drawable.pickup);
            goatImage = BitmapFactory.decodeResource(res, R.drawable.enemy);
            Bitmap playerImage = BitmapFactory.decodeResource(res, R.drawable.player);
            
            speedAdjust = 1; //shouldnt be needed but will use as currently on the fence between 2 styles
            scrollspeed = 4;
            maxFallSpeed = scrollspeed; //ONLY INITIALIZED ONCE REGARDLESS OF CHANGE IN SCROLLSPEED
            gravity = 0.05 * scrollspeed; //ONLY INITIALIZED ONCE REGARDLESS OF CHANGE IN SCROLLSPEED
            maxFallSpeed += speedAdjust; //manual adjustment since it is not relative to scrollspeed for now
            gravity += (0.05 * speedAdjust); //manual adjustment since it is not relative to scrollspeed for now
            player = new Player(playerImage, scrollspeed, speedAdjust);
            Log.d("PickleGame", "player y 1: " + "" + player.y);
            player.x = mCanvasWidth / 4;
            //player.y = (int)(mCanvasHeight / 2.0);
            Log.d("PickleGame", "player y 2: " + "" + player.y);
            updown = false;
            playing = true;
            justStarted = true;
            
            tv.setText("Score: 0");
            layout.addView(tv);
            
            //int breadLuckTimer = (int)(40 / (scrollspeed/4)); //UPDATED MANUALLY WHEN SCROLLSPEED IS CHANGED
            //int sheepLuckTimer = (int)(70 / (scrollspeed/4)); //UPDATED MANUALLY WHEN SCROLLSPEED IS CHANGED
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
	            
	            layout.measure(c.getWidth(), c.getHeight());
	            layout.layout(0, 0, c.getWidth(), c.getHeight());
	        }
	    }
		
		private void doDraw(Canvas canvas) {
            // Draw the background image. Operations on the Canvas accumulate
            // so this is like clearing the screen.
            canvas.drawBitmap(backgroundImage, 0, 0, null);
            
            canvas.drawBitmap(player.image, player.x, player.y + halfHeight, null);
            if (terrain != null) {
	            for(Wall wall : terrain) {
	            	canvas.drawBitmap(wall.image, wall.x, (wall.y - 4 * wall.height) + halfHeight, null);
	            	canvas.drawBitmap(wall.image, wall.x, (wall.y + 4 * wall.height) + halfHeight, null);
	            }
            }
            
            if (collectables != null) {
	            for(Bread bread : collectables) {
	            	canvas.drawBitmap(bread.image, bread.x, (bread.y) + halfHeight, null);
	            }
            }
            if (enemies != null) {
	            for(Goat goat : enemies) {
	            	canvas.drawBitmap(goat.image, goat.x, (goat.y) + halfHeight, null);
	            }
            }
            layout.draw(canvas);
            
            if(!playing) {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setColor(Color.BLACK);
                paint.setTextSize(36);
            	canvas.drawText("Touch to restart", 60, halfHeight, paint);
            }
        }
		
		private boolean getCollision(GameObject obj1, GameObject obj2) {
		//if(obj1.boundingBox.intersects(obj2.boundingBox))
			//Log.d("PickleGame", "x1: " + obj1.x + "x2: " + obj2.x + "y1: " + obj1.y + "y2: " + obj2.y);
			if ( ((obj1.x > obj2.x && obj2.x + obj2.image.getWidth() > obj1.x) || (obj2.x > obj1.x && obj1.x + obj1.image.getWidth() > obj2.x))
			&& ((obj1.y > obj2.y && obj2.y + obj2.image.getHeight() > obj1.y) || (obj2.y > obj1.y && obj1.y + obj1.image.getHeight() > obj2.y)) ) {
				return true;
			}
				
			return false;
		}
		
		private void update() {
			if(playing) {
				if (justStarted == true) {
					justStarted = false;
					halfHeight = (int)(mCanvasHeight / 2);
					restart();
				}
				if(terrain == null) {
					terrain = new ArrayList<Wall>();
					collectables = new ArrayList<Bread>();
					enemies = new ArrayList<Goat>();
		            int x = 0;
		            for(int i = 0; i < mCanvasWidth / 32 + 3; i++) {
		            	Wall wall = new Wall(wallImage, x, 0, wallImage.getWidth(), wallImage.getHeight(), i);
		            	terrain.add(wall);
		            	x += 32;
		            }
				}
				if (collectables == null) {
					collectables = new ArrayList<Bread>();
				}
				if (enemies == null) {
					enemies = new ArrayList<Goat>();
				}
				
				player.update(updown, gravity, maxFallSpeed);
				for(Wall wall : terrain) {
					wall.update(terrain, scrollspeed);
				}
				for (int i = 0; i < collectables.size(); i++) {
					if (collectables.get(i).update(scrollspeed) == true) {
						collectables.remove(i);
					}
				}
				
				for (int i = 0; i < enemies.size(); i++) {
					if (enemies.get(i).update(scrollspeed) == true) {
						enemies.remove(i);
					}
				}
				
				randomNum = (int)(Math.random() * 1001);
				if (randomNum < breadSpawnChancePercent) {
					Bread bread = new Bread(breadImage, mCanvasWidth + 32,((-halfHeight / 2) + ((int)(Math.random() * (halfHeight)))), breadImage.getWidth(), breadImage.getHeight());
					collectables.add(bread);
				}
				randomNum = (int)(Math.random() * 1001);
				if (randomNum < goatSpawnChancePercent) {
					Goat goat = new Goat(goatImage, mCanvasWidth + 32, ((-halfHeight / 2) + ((int)(Math.random() * (halfHeight)))), goatImage.getWidth(), goatImage.getHeight());
					enemies.add(goat);
				}
				
				
				//check for collisions
				for (Wall wall : terrain) {
					player.y -= 4 * wall.height;
					collisionCheck1 = getCollision(player,wall);
					player.y += 8 * wall.height;
					collisionCheck2 = getCollision(player,wall);
					player.y -= 4 * wall.height;
					if (collisionCheck1 || collisionCheck2) {
						//Log.d("PickleGame", "HEY" + new Random().nextInt(64));
						playing = false;
					}
				}
				for (int i = 0; i < collectables.size(); i++) {
					collisionCheck1 = getCollision(player,collectables.get(i));
					if (collisionCheck1 == true) {
						collectables.remove(i);
						score += 500;
						tv.setText("Score: " + score);
						//Log.d("PickleGame", "oh no!");
					}
				}
				for (int i = 0; i < enemies.size(); i++) {
					collisionCheck1 = getCollision(player,enemies.get(i));
					if (collisionCheck1 == true) {
						playing = false;
					}
				}
				//Log.d("PickleGame", "" + collectables.size());
			}
		}
		
		private void restart() {
			terrain = null;
			collectables = null;
			enemies = null;
			player.x = mCanvasWidth / 4;
            player.y = 0;
            player.yVel = 0;
            updown = false;
            playing = true;
            tv.setText("Score: 0");
            score = 0;
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
			if(!thread.playing) {
				thread.restart();
			}
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
