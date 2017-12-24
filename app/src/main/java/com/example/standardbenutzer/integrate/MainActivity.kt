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
                        integrationTasks?.forEach { it.cancel(true) }
                        integrationTasks?.clear()

                        val adapt = AsyncAdaptiveIntegration()
                        integrationTasks?.add(adapt)
                        adapt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,txtFunction.text.toString(), lower, upper, 0.001, object : OnAdaptiveIntegrationCompleted {
                            override fun onAdaptiveIntegrationCompleted(result: Double?) {
                                editText.setText("From %.2f to %.2f: %.4f".format(lower,upper,result))
                            }
                        })

                        functionValuesTasks?.forEach{it.cancel(true)}
                        functionValuesTasks?.clear()

                        val values = AsyncFunctionValues()
                        functionValuesTasks?.add(values)
                        values.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,drawView.width, drawView.getScaleFactor().toDouble(), txtFunction.text.toString(), object : OnFunctionCalculationCompleted {
                            override fun onFunctionCalcCompleted(vars: Array<List<Int>>?) {
                                if (vars!![0].count() > 0) {
                                    drawView.updateFunction(vars)
                                }
                            }
                        })
                    }
                }
            }
        )

        txtFunction.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if(evaluateFunction(txtFunction.text.toString())) {
                        functionValuesTasks?.forEach { it.cancel(true) }
                        functionValuesTasks?.clear()

                        val values = AsyncFunctionValues()
                        functionValuesTasks?.add(values)
                        values.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,drawView.width, drawView.getScaleFactor().toDouble(), txtFunction.text.toString(), object : OnFunctionCalculationCompleted {
                            override fun onFunctionCalcCompleted(vars: Array<List<Int>>?) {
                                if (vars!![0].count() > 0) {
                                    drawView.updateFunction(vars)
                                }
                            }
                        })
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
