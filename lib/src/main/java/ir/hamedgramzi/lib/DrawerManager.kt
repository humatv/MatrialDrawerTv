package ir.huma.humaleanbacklib.Util

import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.view.CrossFadeSlidingPaneLayout
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.withChecked
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.mikepenz.materialdrawer.widget.MiniDrawerSliderView
import ir.hamedgramzi.lib.R
import kotlin.math.roundToInt

class DrawerManager(
    val activity: FragmentActivity,
    val result: MaterialDrawerSliderView,
    var isRtl: Boolean = false
) {
    private val TAG = "DrawerManager"
    private lateinit var miniResult: MiniDrawerSliderView
    lateinit var crossFader: Crossfader<*>

    var miniDrawerBackColor: Int? = null;
    var useMiniDrawer: Boolean = true
    var fireOnClick = false;
    var miniDrawerWidth =
        activity.resources.getDimension(R.dimen.material_mini_drawer_item).roundToInt()
    var drawerWidth =
        activity.resources.getDimension(R.dimen.material_drawer_width).roundToInt()

    var isCrossFadeChanging = false

    fun build(crossfadeContentResLayout: Int) {
        if (useMiniDrawer) {
            miniResult = result.miniDrawer!!

            //get the widths in px for the first and second panel
            val firstWidth = drawerWidth
            val secondWidth = miniDrawerWidth
            miniResult.enableSelectedMiniDrawerItemBackground = true
            result.headerDivider = false
            //create and build our crossfader (see the MiniDrawer is also builded in here, as the build method returns the view to be used in the crossfader)
            //the crossfader library can be found here: https://github.com/mikepenz/Crossfader
            crossFader = Crossfader<CrossFadeSlidingPaneLayout>()
                .withContent(activity.findViewById<View>(crossfadeContentResLayout))
                .withFirst(result, firstWidth)
                .withSecond(miniResult, secondWidth)
                .build()
            miniResult.crossFader = CrossfadeWrapper(crossFader)
            var paddingTop = 0
            if (result.headerHeight != null) {
                paddingTop += result.headerHeight!!.asPixel(activity)
            }
            miniResult.setPadding(0, paddingTop, 0, 0)
            if (miniDrawerBackColor != null) {
                crossFader.getSecond().setBackgroundColor(miniDrawerBackColor!!)
            }
            //define a shadow (this is only for normal LTR layouts if you have a RTL app you need to define the other one
            crossFader.crossFadeSlidingPaneLayout
                .setShadowResourceLeft(R.drawable.material_drawer_shadow_left)
            crossFader.withPanelSlideListener(object : SlidingPaneLayout.PanelSlideListener {
                override fun onPanelSlide(panel: View, slideOffset: Float) {
                    isCrossFadeChanging = true
                }

                override fun onPanelOpened(panel: View) {
                    isCrossFadeChanging = false
                }

                override fun onPanelClosed(panel: View) {
                    isCrossFadeChanging = false
                }

            })

        }
    }

    fun shouldGetKeyEvent(): Boolean {
        return crossFader.isCrossFaded || isCrossFadeChanging
    }

    var lastFocus: View? = null;
    fun keyEvent(event: KeyEvent?): Boolean {

        var foc = activity.currentFocus;

        var right = KeyEvent.KEYCODE_DPAD_RIGHT;
        var left = KeyEvent.KEYCODE_DPAD_LEFT;

        if (isRtl) {
            val temp = right;
            right = left;
            left = temp;
        }

        if (event?.action == KeyEvent.ACTION_UP) {
            if (crossFader.isCrossFaded) {
                return true
            }
            return false;
        }

        if (foc != null && foc != lastFocus && result.miniDrawer?.recyclerView != foc && result.miniDrawer?.recyclerView?.parent != foc && !crossFader.isCrossFaded()) {
            if (lastFocus != null && foc::class.java == lastFocus!!::class.java) {
                lastFocus = foc;
                return false;
            }
        }
        lastFocus = foc;

        var position = result.selectedItemPosition;
        var item = result.adapter.getItem(position);

        if (event?.keyCode == left) {
            if (!crossFader.isCrossFaded()) {
                crossFader.crossFade()
                return true;
            }
        } else if (event?.keyCode == right) {
            if (crossFader.isCrossFaded()) {
                if (item == null) {
                    Log.d(TAG, "keyEvent: item is null!!")
                } else if (item is SwitchDrawerItem) {
                    item.withChecked(!item.isChecked)
                    result.adapter.notifyAdapterItemChanged(position);
                } else if (item is ToggleDrawerItem) {
                    item.withChecked(!item.isChecked)
                    result.adapter.notifyAdapterItemChanged(position);
                } else {
                    result.adapter.viewClickListener?.onClick(
                        (if (result.recyclerView.getChildAt(
                                position
                            ) == null
                        ) foc else result.recyclerView.getChildAt(position))!!,
                        position,
                        result.adapter,
                        item!!
                    )
                }

                crossFader.crossFade()
                return true;
            }
        } else if (crossFader.isCrossFaded() && event?.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            while (position + 1 < result.adapter.itemCount) {
                if (result.adapter.getItem(position + 1) is DividerDrawerItem || result.adapter.getItem(
                        position + 1
                    ) is SpaceDrawerItem
                ) {
                    position++;
                } else {
                    val fire: Boolean =
                        if (result.adapter.getItem(position + 1)?.tag != null && result.adapter.getItem(
                                position + 1
                            )?.tag is Boolean
                        ) (result.adapter.getItem(position + 1)?.tag as Boolean) else (fireOnClick && result.adapter.getItem(
                            position - 1
                        )?.isSelectable == true)
                    result.setSelectionAtPosition(position + 1, fire)
                    result.recyclerView.scrollToPosition(position + 1)
                    break
                }
            }
            return true;

        } else if (crossFader.isCrossFaded() && event?.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            while (position - 1 >= 0) {
                var upItem = result.adapter.getItem(position - 1)
                if (upItem is DividerDrawerItem || upItem is SpaceDrawerItem || upItem is ContainerDrawerItem) {
                    position--;
                } else {
                    val fire: Boolean =
                        if (result.adapter.getItem(position - 1)?.tag != null
                            && result.adapter.getItem(position - 1)?.tag is Boolean
                        )
                            (result.adapter.getItem(position - 1)?.tag as Boolean)
                        else (fireOnClick && result.adapter.getItem(position - 1)?.isSelectable == true)
                    result.setSelectionAtPosition(position - 1, fire)
                    result.recyclerView.scrollToPosition(position - 1)
                    break
                }
            }

            return true;
        } else if (crossFader.isCrossFaded() && (event?.keyCode == KeyEvent.KEYCODE_ENTER || event?.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || event?.keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
            if (item is SwitchDrawerItem) {
                item.withChecked(!item.isChecked)
                result.adapter.notifyAdapterItemChanged(position);
            } else if (item is ToggleDrawerItem) {
                item.withChecked(!item.isChecked)
                result.adapter.notifyAdapterItemChanged(position);
            } else {

                result.adapter.viewClickListener?.onClick(
                    (if (result.recyclerView.getChildAt(
                            position
                        ) == null
                    ) foc else result.recyclerView.getChildAt(position))!!,
                    position,
                    result.adapter,
                    item!!
                )
            }

            return true;

        } else if (event?.keyCode == KeyEvent.KEYCODE_BACK || event?.keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (crossFader.isCrossFaded()) {
                crossFader.crossFade()
                return true
            }
            return false;

        }
        return false;
    }
//
//    fun replaceFragment(fragment: Fragment, tag: String = fragment.javaClass.simpleName) {
//        if (frameFragmentRes != null) {
//
//            if (activity.supportFragmentManager.fragments.size > 0) {
//                val tx = activity.supportFragmentManager.beginTransaction()
//                tx.remove(activity.supportFragmentManager.fragments[0])
//                tx.commit()
//            }
//            val tx = activity.supportFragmentManager.beginTransaction()
//            tx.replace(frameFragmentRes!!, fragment, tag)
//            tx.commitNowAllowingStateLoss()
//        } else {
//            throw RuntimeException("You must fill frameFragmentRes for replace fragment in it")
//        }
//    }

}