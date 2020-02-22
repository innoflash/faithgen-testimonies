package net.faithgen.testimonies.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_testimony.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.sdk.comments.CommentsSettings
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.Response
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.interfaces.DialogListener
import net.faithgen.sdk.menu.Menu
import net.faithgen.sdk.menu.MenuFactory
import net.faithgen.sdk.menu.MenuItem
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.sdk.utils.Utils
import net.faithgen.testimonies.utils.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.adapters.ImagesAdapter
import net.faithgen.testimonies.dialogs.ImagesSliderDialog
import net.faithgen.testimonies.models.Testimony
import net.innoflash.iosview.recyclerview.RecyclerTouchListener
import net.innoflash.iosview.recyclerview.RecyclerViewClickListener

/**
 * This renders the testimony from the server
 */
class TestimonyActivity : FaithGenActivity(), RecyclerViewClickListener {
    private var testimony: Testimony? = null
    private var belongsToMe: Boolean = false

    private val faithGenAPI: FaithGenAPI by lazy {
        FaithGenAPI(this)
    }

    private val testimonyId: String by lazy {
        intent.getStringExtra(Constants.TESTIMONY_ID)
    }

    override fun getPageTitle(): String = "Loading"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testimony)
    }

    override fun onStart() {
        super.onStart()
        if (testimony === null)
            fetchTestimony()
    }

    override fun onStop() {
        super.onStop()
        faithGenAPI.cancelRequests()
    }

    /**
     * Creates the menu when the testimony is ready for rendering
     *
     * Some options are hidden is this user is not the author of the testimony
     */
    private fun initMenu() {
        belongsToMe = SDK.getUser() !== null && SDK.getUser().id.equals(testimony?.user?.id)

        val moreTestimonies: String by lazy {
            if (!belongsToMe)
                Constants.THEIR_TESTIMONIES
            else Constants.YOUR_TESTIMONIES
        }
        val menuItems: MutableList<MenuItem> = mutableListOf()
        menuItems.add(MenuItem(R.drawable.ic_share_24, Constants.SHARE))
        menuItems.add(MenuItem(R.drawable.ic_comments_24, Constants.COMMENTS))
        menuItems.add(MenuItem(R.drawable.ic_testimonies_24, moreTestimonies))
        if (!belongsToMe) {
            menuItems.add(MenuItem(R.drawable.ic_pencil_24, Constants.EDIT))
            menuItems.add(MenuItem(R.drawable.ic_trash_24, Constants.DELETE))
        }
        menuItems.add(MenuItem(R.drawable.ic_back_24, Constants.EXIT))

        val menu: Menu = MenuFactory.initializeMenu(this, menuItems)
        menu.setOnMenuItemListener(Constants.TESTIMONY_OPTIONS) { menuItem, position ->
            when (position) {
                0 -> Utils.shareText(
                    this@TestimonyActivity,
                    "${testimony?.user?.name}`s testimony\n${testimony?.date?.formatted}\n\n${testimony?.testimony}\n\nCourtesy of ${SDK.getMinistry().name}",
                    "${testimony?.user?.name}`s testimony"
                )
                1 -> SDK.openComments(
                    this@TestimonyActivity, CommentsSettings.Builder()
                        .setCategory(Constants.TESTIMONIES_URL)
                        .setItemId(testimonyId)
                        .setTitle(testimony?.title)
                        .build()
                )
                2 -> {
                    val intent = Intent(this@TestimonyActivity, UserTestimoniesActivity::class.java)
                    intent.putExtra(Constants.USER_ID, testimony?.user?.id)
                    intent.putExtra(Constants.USER_NAME, testimony?.user?.name)
                    startActivity(intent)
                }
                3 -> {
                    if (!belongsToMe) openUpdateTestimony()
                    else finish()
                }
                4 -> Dialogs.confirmDialog(
                    this@TestimonyActivity,
                    Constants.WARNING,
                    Constants.CONFIRM_DELETE,
                    object : DialogListener() {
                        override fun onYes() {
                            deleteTestimony()
                        }
                    })
                5 -> finish()
            }
        }
        setOnOptionsClicked { menu.show() }
    }

    /**
     * Prompts the user before deleting the testimony
     *
     * If it gets deleted the activity finishes
     */
    private fun deleteTestimony() {
        faithGenAPI.setMethod(Request.Method.DELETE)
            .setParams(null)
            .setFinishOnFail(true)
            .setProcess(Constants.DELETING_IMAGE)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    val response: Response<*>? =
                        GSONSingleton.instance.gson.fromJson(serverResponse, Response::class.java)
                    if (response!!.success)
                        Dialogs.showOkDialog(this@TestimonyActivity, response.message, true)
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(this@TestimonyActivity, errorResponse?.message, false)
                }
            })
            .request("${Constants.TESTIMONIES_URL}/$testimonyId")
    }

    /**
     * This prepares the testimony for update
     * and open the update activity
     */
    private fun openUpdateTestimony() {
        val stringifiedTestimony: String by lazy {
            GSONSingleton.instance.gson.toJson(testimony)
        }
        val intent = Intent(this, UpdateTestimonyActivity::class.java)
        intent.putExtra(Constants.TESTIMONY_ID, testimonyId)
        intent.putExtra(Constants.TESTIMONY, stringifiedTestimony)
        startActivity(intent)
    }

    /**
     * This launches an HTTP call to fetch the testimony
     * from the server using the initialized testimony id
     */
    private fun fetchTestimony() {
        faithGenAPI.setParams(null)
            .setFinishOnFail(true)
            .setProcess(Constants.FETCHING_TESTIMONY)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    testimony = GSONSingleton.instance.gson
                        .fromJson(serverResponse, Testimony::class.java)
                    renderTestimony()
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(this@TestimonyActivity, errorResponse?.message, true)
                }
            })
            .request("${Constants.TESTIMONIES_URL}/$testimonyId")
    }

    /**
     * This draws the menu
     * Draws the menu to the view and subviews on the activity
     * when its fetched from the server
     */
    private fun renderTestimony() {
        initMenu()

        Picasso.get()
            .load(testimony?.user?.picture)
            .placeholder(R.drawable.ic_user_100)
            .error(R.drawable.ic_user_100)
            .into(userImage)

        toolbar.pageTitle = testimony?.user?.name
        userName.text = testimony?.user?.name
        testimonyTitle.text = testimony?.title
        testimonySummary.text =
            "${testimony?.date?.formatted}\n${testimony?.date?.approx}\n${testimony?.comments_count} comments"
        testimonyBody.text = testimony?.testimony

        if (testimony?.resource === null)
            testimonyLink.visibility = View.GONE
        testimonyLink.setOnClickListener {
            Utils.openURL(
                this@TestimonyActivity,
                testimony?.resource
            )
        }

        if (belongsToMe && testimony!!.approved)
            approvedTestimony.visibility = View.VISIBLE

        if (belongsToMe && !testimony!!.approved)
            pendingTestimony.visibility = View.VISIBLE

        if (testimony?.images?.size === 0)
            tImages.visibility = View.GONE

        val adapter = ImagesAdapter(this@TestimonyActivity, testimony!!.images)
        testimonyImages.layoutManager = GridLayoutManager(this@TestimonyActivity, 2)
        testimonyImages.adapter = adapter
        testimonyImages.addOnItemTouchListener(RecyclerTouchListener(this, testimonyImages, this))
    }

    /**
     * This initialize image clicks events for the testimony images
     */
    override fun onClick(view: View?, position: Int) {
        val imagesSliderDialog: ImagesSliderDialog by lazy {
            ImagesSliderDialog(testimony!!, position, this)
        }
        imagesSliderDialog.show(supportFragmentManager, Constants.SLIDERS_TAG)
        imagesSliderDialog.setErrorListener(object : ImagesSliderDialog.ErrorListener {
            override fun onMessage(message: String, closeActivity: Boolean) {
                Dialogs.showOkDialog(this@TestimonyActivity, message, closeActivity)
            }
        })
    }

    override fun onLongClick(view: View?, position: Int) {
        //leave as is
    }
}
