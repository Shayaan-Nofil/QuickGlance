package com.m_abdullah.quickglance

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class offlinesync : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}