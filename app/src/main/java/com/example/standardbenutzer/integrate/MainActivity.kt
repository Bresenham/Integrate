package com.example.standardbenutzer.integrate

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlEngine
import org.apache.commons.jexl3.JexlExpression
import org.apache.commons.jexl3.MapContext

class MainActivity : AppCompatActivity() {

    private var jexl : JexlEngine? = null
    private var jexlExpression : JexlExpression? = null
    private var validFunction = false
    private var containsE = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        jexl = JexlBuilder().create()

        drawView.setUpdatedBoundsListener( object : DrawView.UpdatedBoundsListener {
                override fun onBoundsUpdated(bounds : List<Double>) {
                    if(validFunction) {
                        val upper = Math.max(bounds[0], bounds[1])
                        val lower = Math.min(bounds[0], bounds[1])
                        AdaptiveIntegration().execute(jexlExpression,containsE,lower,upper,0.001)
                    }
                }
            }
        )

        txtFunction.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    drawView.updateFunction(calculateFunctionValues(txtFunction.text.toString()))
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            }
        )
    }

    fun updateIntegratedValue(calc : String){
        editText.setText(calc)
    }

    fun calculateFunctionValues(input : String) : Array<List<Int>>{
        val xPoints : MutableList<Int> = mutableListOf()
        val yPoints : MutableList<Int> = mutableListOf()
        try {
            jexlExpression = jexl?.createExpression(input)
            val jexlContext = MapContext()
            containsE = input.contains("e")
            if(containsE)
                jexlContext.set("e",Math.E)
            for (i in 0..drawView.width){
                xPoints.add(i + drawView.width.div(2))
                jexlContext.set("x", i.toDouble())
                var result = jexlExpression?.evaluate(jexlContext)
                var intRes = result as Double
                yPoints.add(intRes.toInt())
            }
            validFunction = true
        } catch(e : Exception){
            System.out.println(e.message)
            validFunction = false
        }
        return arrayOf(xPoints,yPoints)
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

    private inner class AdaptiveIntegration : AsyncTask<Any, Int, Double> {
        private var containsE = false
        private var jexlExpression : JexlExpression? = null
        private var a = 0.0
        private var b = 0.0
        private var err = 0.0

        constructor() : super()

        private fun f(x : Double) : Double{
            var jexlContext = MapContext()
            jexlContext.set("x",x)
            if(containsE)
                jexlContext.set("e",Math.E)
            var result = jexlExpression?.evaluate(jexlContext)
            var res = result as Double
            return res
        }

        private fun adativeIntegration(a : Double, b : Double, err : Double) : Double{
            var h = b.minus(a)
            val s0 = h * ((1.0 / 6.0) * f(a) + (4.0 / 6.0) * f((a + b) / 2) + (1.0 / 6.0) * f(b))
            h = (b - a).div(2)
            var s1 = 0.0
            for (i in 0..1) {
                var a_1 = a + i.times(h)
                var b_1 = b + (i + 1).times(h)
                s1 += h * ((1.0 / 6.0) * f(a_1) + (4.0 / 6.0) * f((a_1 + b_1) / 2) + (1.0 / 6.0) * f(b_1))
            }
            var e = Math.abs(s1 - s0)

            if (e <= err)
                return s1
            else {
                return adativeIntegration(a, (a + b).div(2), err) + adativeIntegration((a + b).div(2), b, err)
            }
        }

        override fun doInBackground(vararg p0: Any?): Double {
            jexlExpression = p0[0] as JexlExpression?
            containsE = p0[1] as Boolean
            a = p0[2] as Double
            b = p0[3] as Double
            err = p0[4] as Double

            return adativeIntegration(a,b,err)
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)
            updateIntegratedValue("From $a to $b : %.2f".format(result))
        }
    }
}
