package com.prosocial.digitread

import android.os.Bundle
import android.app.Activity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class MainActivity : Activity() {
    /** Called when the activity is first created.  */
    var label : TextView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        label = findViewById(R.id.predictionText)

//        var activityImage : ImageView = findViewById(R.id.myImage)

        val clear: Button = findViewById(R.id.clearButton)
        val canv : MyThing = findViewById(R.id.myCanvas)
        clear.setOnClickListener { canv.reset() }
    }

}