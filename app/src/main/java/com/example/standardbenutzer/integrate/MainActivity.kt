package com.example.standardbenutzer.integrate

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            editText.setText(drawView.getText())
        }
        drawView.setUpdatedBoundsListener( object : DrawView.UpdatedBoundsListener {
                override fun onBoundsUpdated(bounds : List<Double>) {
                    val upper = Math.max(bounds[0],bounds[1])
                    val lower = Math.min(bounds[0],bounds[1])
                    val result = (upper - lower).div(2).times(Math.exp(lower) + Math.exp(upper))
                    editText.setText("Integrate from %.2f to %.2f: %.6f".format(lower,upper,result))
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
