package com.example.standardbenutzer.integrate

import android.os.AsyncTask
import android.os.Bundle

import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var inputExp : String? = null
    private var validFunction = false
    private var currentAsyncTasks : MutableList<AdaptiveIntegration>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        drawView.setUpdatedBoundsListener( object : DrawView.UpdatedBoundsListener {
                override fun onBoundsUpdated(bounds : List<Double>) {
                    if(validFunction) {
                        val upper = Math.max(bounds[0], bounds[1])
                        val lower = Math.min(bounds[0], bounds[1])

                        currentAsyncTasks?.forEach { it.cancel(true) }
                        currentAsyncTasks = mutableListOf()
                        val adapt = AdaptiveIntegration()
                        currentAsyncTasks?.add(adapt)

                        adapt.execute(inputExp,lower,upper,0.001)

                        updateViewWithValues()
                    }
                }
            }
        )

        txtFunction.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    updateViewWithValues()
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

    fun calculateFunctionValues(input : String, divFac : Double) : Array<List<Int>>{
        val xPoints : MutableList<Int> = mutableListOf()
        val yPoints : MutableList<Int> = mutableListOf()
        try {
            for (i in -drawView.width.div(2)..drawView.width.div(2)){
                var math = MathEval()
                math.setVariable("x",i.div(divFac))
                xPoints.add(i + drawView.width.div(2))
                yPoints.add(math.evaluate(input).toInt())
            }
            inputExp = input
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

    private fun updateViewWithValues(){
        var results = calculateFunctionValues(txtFunction.text.toString(),drawView.getScaleFactor().toDouble())
        if(results[0].count() > 0)
            drawView.updateFunction(results)
    }

    private inner class AdaptiveIntegration : AsyncTask<Any, Int, Double> {
        private var exp : String? = null
        private var a = 0.0
        private var b = 0.0
        private var err = 0.0

        constructor() : super()

        override fun doInBackground(vararg p0: Any?): Double {
            exp = p0[0] as String
            a = p0[1] as Double
            b = p0[2] as Double
            err = p0[3] as Double

            return adativeIntegration(a,b,err)
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)
            updateIntegratedValue("From %.2f to %.2f : %.2f".format(a,b,result))
        }

        private fun f(x : Double) : Double{
            var math = MathEval()
            math.setVariable("x",x)
            return math.evaluate(exp)
        }

        private fun adativeIntegration(a : Double, b : Double, err : Double) : Double{
            if(!isCancelled) {
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
            } else
                return -1.0
        }
    }
}
