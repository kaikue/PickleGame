package com.example.picklegame;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PickleGame extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // tell system to use the layout defined in our XML file
        setContentView(R.layout.main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pickle_game, menu);
        return true;
    }
}
