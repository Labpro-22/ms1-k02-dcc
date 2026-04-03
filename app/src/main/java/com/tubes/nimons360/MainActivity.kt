package com.tubes.nimons360

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tubes.nimons360.map.MapFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MapFragment())
                .commit()
        }
    }
}
