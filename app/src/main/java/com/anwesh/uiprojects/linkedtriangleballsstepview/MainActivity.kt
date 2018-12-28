package com.anwesh.uiprojects.linkedtriangleballsstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.triangleballsstepview.TriangleBallsStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TriangleBallsStepView.create(this)
    }
}
