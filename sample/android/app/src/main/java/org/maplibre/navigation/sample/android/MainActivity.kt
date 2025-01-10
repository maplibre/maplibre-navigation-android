package org.maplibre.navigation.sample.android

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.maplibre.android.MapLibre
import org.maplibre.navigation.sample.android.core.CoreOnlyFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        //TODO: move this to application calls
        MapLibre.getInstance(applicationContext)

        supportFragmentManager.beginTransaction()
            .add(R.id.flFragmentContainer, CoreOnlyFragment())
            .commit()
    }
}