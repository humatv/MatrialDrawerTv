package ir.hamedgramzi.tvmaterialdrawer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.holder.ColorHolder
import com.mikepenz.materialdrawer.holder.DimenHolder
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.mikepenz.materialdrawer.widget.MiniDrawerSliderView
import ir.hamedgramzi.tvmaterialdrawer.databinding.ActivityMainBinding
import ir.huma.humaleanbacklib.Util.DrawerManager


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var miniSliderView: MiniDrawerSliderView
    private lateinit var sliderView: MaterialDrawerSliderView
    private lateinit var manager: DrawerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            it.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            setContentView(it.root)
        }

        sliderView = MaterialDrawerSliderView(this).apply {
//            accountHeader = this@MainActivity.headerView
            headerView =
                LayoutInflater.from(this@MainActivity).inflate(R.layout.header_layout, null, false)
            headerHeight = DimenHolder.fromPixel(150)
            customWidth = ViewGroup.LayoutParams.MATCH_PARENT
            itemAdapter.add(
                PrimaryDrawerItem().apply {
                    nameRes = R.string.drawer_item_compact_header;
                    identifier = 1;
                    iconicsIcon = GoogleMaterial.Icon.gmd_brightness_5
                    selectedColor = ColorHolder.fromColor(Color.YELLOW)
                },
                PrimaryDrawerItem().apply {
                    nameText = "Item NOT in mini drawer";
                    identifier = 100;
                    iconicsIcon = GoogleMaterial.Icon.gmd_bluetooth
                },
                PrimaryDrawerItem().apply {
                    iconicsIcon = FontAwesome.Icon.faw_home
                }.withName(R.string.drawer_item_action_bar_drawer).withBadge("22")
                    .withBadgeStyle(BadgeStyle(Color.RED, Color.RED)).withIdentifier(2),
                PrimaryDrawerItem().apply {
                    iconicsIcon = FontAwesome.Icon.faw_gamepad
                }.withName(R.string.drawer_item_multi_drawer)
                    .withIdentifier(3),
                PrimaryDrawerItem()
                    .apply { iconicsIcon = FontAwesome.Icon.faw_eye }
                    .withName(R.string.drawer_item_non_translucent_status_drawer).withIdentifier(4),
                PrimaryDrawerItem().apply { iconicsIcon = GoogleMaterial.Icon.gmd_adb }
                    .withDescription("A more complex sample")
                    .withName(R.string.drawer_item_advanced_drawer)
                    .withIdentifier(5),
            )
            onDrawerItemClickListener = { v, item, position ->
                if (item is Nameable) {
                    Toast.makeText(
                        this@MainActivity,
                        item.name?.getText(this@MainActivity),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }
        }

        miniSliderView = MiniDrawerSliderView(this).apply {
            drawer = sliderView
        }
        manager = DrawerManager(this, sliderView, true)
        manager.build(R.id.crossfade_content)


    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return super.dispatchKeyEvent(event) ||
                manager.keyEvent(event)
    }

}