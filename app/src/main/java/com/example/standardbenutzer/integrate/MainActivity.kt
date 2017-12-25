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

    private var functionValuesTasks : MutableList<AsyncFunctionValues>? = null
    private var integrationTasks : MutableList<AsyncAdaptiveIntegration>? = null
    private val NUMBER_OF_TASKS = Runtime.getRuntime().availableProcessors()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        functionValuesTasks = mutableListOf()
        integrationTasks = mutableListOf()

        drawView.setUpdatedBoundsListener( object : DrawView.UpdatedBoundsListener {
                override fun onBoundsUpdated(bounds : List<Double>) {
                    val upper = Math.max(bounds[0], bounds[1])
                    val lower = Math.min(bounds[0], bounds[1])

                    if(evaluateFunction(txtFunction.text.toString())) {
                        startAsyncIntegration(lower,upper)
                    }
                }
            }
        )

        drawView.setUpdatedScreenListener(object : DrawView.UpdatedScreenListener{
            override fun onScreenUpdated() {
                startAsyncFuncCalc()
            }
        })

        txtFunction.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if(evaluateFunction(txtFunction.text.toString())) {
                        startAsyncFuncCalc()
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            }
        )
    }

    private fun evaluateFunction(input : String) : Boolean{
        try {
            var math = MathEval()
            math.setVariable("x", 1.0)
            math.evaluate(input)
        } catch(e:Exception){
            return false
        }
        return true
    }

    private fun startAsyncIntegration(a : Double, b : Double){
        integrationTasks?.forEach { it.cancel(true) }
        integrationTasks?.clear()

        val list = mutableListOf<Double>()

        for(i in 0..NUMBER_OF_TASKS-1){
            val adapt = AsyncAdaptiveIntegration()
            integrationTasks?.add(adapt)
            val a1 = a + (((b-a) / NUMBER_OF_TASKS) * i)
            val b1 = a + (((b-a) / NUMBER_OF_TASKS) * (i + 1))
            adapt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,txtFunction.text.toString(), a1, b1, 0.001, object : OnAdaptiveIntegrationCompleted {
                override fun onAdaptiveIntegrationCompleted(result: Double?, sumList : MutableList<Double>) {
                    if(sumList.count() == NUMBER_OF_TASKS) {
                        editText.setText("[%.2f..%.2f]: %.4f".format(a, b, sumList.stream().mapToDouble { it }.sum()))
                        list.clear()
                    }
                }
            },list)
        }
    }

    private fun startAsyncFuncCalc(){
        functionValuesTasks?.forEach { it.cancel(true) }
        functionValuesTasks?.clear()

        //-drawViewWidth.div(2)-leftXBorder..drawViewWidth.div(2)-leftXBorder)
        var a = -drawView.width.div(2) - drawView.getXStart()
        var b = drawView.width.div(2) - drawView.getXStart()

        val h = (b-a) / NUMBER_OF_TASKS

        val sequNumbers = mutableListOf<Int>()
        val fullArray = IntArray(b-a)

        for(i in 0 until NUMBER_OF_TASKS) {
            val a1 = a + h * i
            val b1 = a + h * (i+1)

            val values = AsyncFunctionValues()
            functionValuesTasks?.add(values)
            values.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, drawView.width, drawView.getScaleFactor(), txtFunction.text.toString(), a1, b1, object : OnFunctionCalculationCompleted {
                override fun onFunctionCalcCompleted(vars: IntArray, sequNumber : Int) {
                    sequNumbers.add(sequNumber)
                    for(i in 0 until h)
                        fullArray[i-a+sequNumber] = vars[i]
                    if(sequNumbers.count() == NUMBER_OF_TASKS)
                        drawView.updateFunction(fullArray)
                }
            })
        }
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
