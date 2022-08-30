package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.Transition
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.appview.BuildConfig
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.getFitTypeConverted
import com.newshunt.appview.common.ui.helper.getGradientDrawable
import com.newshunt.appview.common.ui.listeners.ShowReadMoreCallback
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.LayoutCollectionLandingBinding
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DateFormatter
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.BackgroundType2
import com.newshunt.dataentity.common.asset.CarouselProperties2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.ViralAsset
import com.newshunt.news.helper.toMinimizedCommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.news.model.entity.DisplayCardType
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.viral.model.entity.server.BackgroundOption
import com.newshunt.dhutil.BlurTransformation
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.helper.theme.ThemeType
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.di.DaggerCollectionsDetailsComponent2
import com.newshunt.news.di.DetailsModule2
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.TAG
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.sdk.network.image.Image
import java.io.Serializable
import java.util.*
import javax.inject.Inject

class CollectionLandingFragment2 : BaseSupportFragment(), Toolbar.OnMenuItemClickListener,
    DeeplinkableDetail {
    var postId: String? = null
    var parentStoryId: String? = null

    lateinit var progressbar: View
    lateinit var toolbar: Toolbar

    private var cardPosition = -1
    private var timeSpentEventId: Long = 0
    lateinit var section: String
    private val FLOATING_ERROR = 0.001f
    private val HEIGHT_CONSTRAINED_RATIO = "H,1:%.2f"

    @Inject
    lateinit var detailsViewModelF: DetailsViewModel.Factory

    @Inject
    lateinit var cardsViewModelF: CardsViewModel.Factory

    private lateinit var vm: DetailsViewModel
    private lateinit var cvm: CardsViewModel
    var card: CommonAsset? = null
    var firstCard: CommonAsset? = null
    private var loggedCollectionPreviewEvent = false;
    private lateinit var binding: LayoutCollectionLandingBinding

    private var currentPageReferrer: PageReferrer? = null
    private var referrerLead: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    private var referrer_raw: String? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val bundle = arguments
        if (bundle != null) {
            postId = bundle.getString(Constants.STORY_ID)
            parentStoryId = bundle.getString(Constants.PARENT_STORY_ID)
            timeSpentEventId = bundle.getLong(NewsConstants.TIMESPENT_EVENT_ID, 0L)
            cardPosition = bundle.getInt(NewsConstants.CARD_POSITION, -1)
            if (!CommonUtils.isEmpty(bundle.getString(Constants.REFERRER_RAW))) {
                referrer_raw = bundle.getString(Constants.REFERRER_RAW)
            }

            referrerLead = bundle.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            if (referrerLead == null) {
                referrerLead = PageReferrer()
            }
            referrerLead?.referrerAction = NhAnalyticsUserAction.CLICK
            currentPageReferrer = PageReferrer(referrerLead)
            referrerFlow = PageReferrer(referrerLead)


        }
        section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section

        if (arguments != null && arguments!!.getBoolean(NewsConstants.BUNDLE_FORCE_NIGHT_MODE,
                        false)) {
            val contextThemeWrapper = ContextThemeWrapper(activity, ThemeType.NIGHT.themeId)
            val localInflater = inflater.cloneInContext(contextThemeWrapper)
            binding = DataBindingUtil.inflate(localInflater, R.layout.layout_collection_landing, container, false)
        } else {
            binding = DataBindingUtil.inflate(inflater, R.layout.layout_collection_landing, container, false)
        }


        if (postId == null) {
            return null
        }

        DaggerCollectionsDetailsComponent2.builder().detailsModule2(DetailsModule2(
                CommonUtils.getApplication(),
                SocialDB.instance(),
                arguments?.getString(Constants.PAGE_ID) ?: "1",
                postId!!,
                timeSpentEventId, false,
                Constants.FETCH_LOCATION_DETAIL,
                sourceId = arguments?.getString(NewsConstants.SOURCE_ID),
                sourceType = arguments?.getString(NewsConstants.SOURCE_TYPE),
                lifecycleOwner = this, section = section,
                fragmentManager = activity?.supportFragmentManager,
                listLocation = Constants.FETCH_LOCATION_DETAIL,
                referrerFlow = referrerFlow ?: PageReferrer(referrerLead), fragment = this, fragmentName = "CollectionLandingFragment2")).build().inject(this)

        vm = ViewModelProviders.of(this, detailsViewModelF)[DetailsViewModel::class.java]
        if (currentPageReferrer != null) {
            vm.pageReferrer = currentPageReferrer!!
        }
        vm.detailCardScan.observe(viewLifecycleOwner, Observer {
            card = it.data
            loadContent()
        })

        cvm = ViewModelProviders.of(this, cardsViewModelF)[CardsViewModel::class.java]

        initActionBar(binding.root)
        return binding.root
    }

    private fun loadContent() {

        binding.title.text = card?.i_title()
        binding.playAllButton.text = card?.i_carouselProperties()?.actionButtonText
        Image.load(card?.i_carouselProperties()?.actionButtonIcon).into(binding.playAllButtonIcon)
        // setTextOrMakeViewGone(collection?.shortTitle, subtitleTv)

        var backgroundImageUrl = if (ThemeUtils.isNightMode()) card?.i_carouselProperties()?.nightModeBackgroundImageUrl else card?.i_carouselProperties()?.backgroundImageUrl
        if (!CommonUtils.isEmpty(backgroundImageUrl)) {
            backgroundImageUrl = ImageUrlReplacer.getQualifiedImageUrl(backgroundImageUrl, CommonUtils
                    .getDeviceScreenWidth(), CommonUtils.getDeviceScreenHeight() - CommonUtils.getDimension
            (R.dimen.collection_play_all_height_with_margin))
            Image.load(backgroundImageUrl, true).into(object : Image
            .ImageTarget() {
                override fun onResourceReady(resource: Any, transition: Transition<*>?) {
                    super.onResourceReady(resource, transition)
                    if (!isAdded) {
                        return
                    }
                    binding.collectionLandingBackground.background = BitmapDrawable(resources, resource as
                            Bitmap)
                }
            })
        }

        binding.playAllButtonCardview.setOnClickListener {
            playAll()
        }

        binding.cardview1.setOnClickListener {
            playAll()
        }

        if (card?.i_collectionItems()?.size ?: 0 > 0) {
            this.firstCard = card?.i_collectionItems()?.get(0)
            initFirstCard()
        }

        logCollectionViewEvent()
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

    }

    private fun logCollectionViewEvent() {
        card ?: return
        if (!loggedCollectionPreviewEvent && userVisibleHint) {

            val map = HashMap<NhAnalyticsEventParam, Any?>()
            val item = card ?: return
            AnalyticsHelper2.logCollectionViewEvent(item, referrerFlow, referrerLead, currentPageReferrer,
                    referrer_raw, map,
                    NhAnalyticsEventSection.NEWS)

        }
    }

    private fun logCollectionPlayEvent() {
        card ?: return
        if (!loggedCollectionPreviewEvent && userVisibleHint) {

            val map = HashMap<NhAnalyticsEventParam, Any?>()
            val item = card ?: return
            AnalyticsHelper2.logCollectionPlayEvent(item, referrerFlow, referrerLead, currentPageReferrer,
                    referrer_raw, map,
                    NhAnalyticsEventSection.NEWS)

        }
    }

    private fun playAll() {

        logCollectionPlayEvent()

        val intent = Intent()
        intent.action = Constants.CAROUSEL_DETAIL_ACTION
        intent.putExtra(Constants.PARENT_STORY_ID, postId)
        intent.putExtra(Constants.STORY_ID, firstCard?.i_id())
        intent.setPackage(AppConfig.getInstance()!!.packageName)
        intent.putExtra(NewsConstants.DH_SECTION, section)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = null))
    }

    private fun initFirstCard() {

        firstCard?.let {

            val displayTime = it.i_publishTime()
            var timeInfo: String? = Constants.EMPTY_STRING

            if (displayTime != null) {
                if (displayTime <= 0 || it.i_showTsAlways() && CommonUtils.isTimeExpired(displayTime, CardsBindUtils.getOldestListDisplayTimeGap())) {
                    timeInfo = Constants.EMPTY_STRING
                }
            }
            if (CommonUtils.isEmpty(it.i_tsString()))
                timeInfo = displayTime?.let { DateFormatter.getTimeAgoRoundedToMinute(it) }
            else
                timeInfo = Constants.EMPTY_STRING

            binding.sourceName.text = it.i_source()?.displayName

//            val countsText = CountsUtil.getCountsText(baseContentAsset,
//                    isPrimaryActionshare, timeInfo, null, BaseAssetUtil.isUrdu(baseContentAsset))
//            binding.counts =

            if (it.i_viral() != null) {
                initViralCard(binding.root, it)
            } else {
                initNewsOrVideoCard(binding.root, it)
            }
        }


    }

    private fun initNewsOrVideoCard(view: View, card: CommonAsset?) {
        binding.collectionItemImage.visibility = View.VISIBLE
        val width = getWidthOfCardView();
        val height = (width * 9 / 16f).toInt()
        val layoutparams = binding.collectionItemImage.layoutParams
        layoutparams.width = width
        layoutparams.height = if (height < getMaxHeightForMemeAndImage()) height else getMaxHeightForMemeAndImage()
        binding.collectionItemImage.layoutParams = layoutparams
        binding.collectionItemImage.setImageDrawable(CommonUtils.getDrawable(R.drawable.default_stry_detail_img))
        val dimensions = Pair(width, height)
        val imageLoc = card?.i_contentImageInfo()?.url ?: card?.i_thumbnailUrls()?.getOrNull(0)
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(imageLoc, width,
                height)
        if (!CommonUtils.isEmpty(qualifiedUrl)) {
            Image.load(qualifiedUrl).placeHolder(R.drawable.default_stry_detail_img).into(binding.collectionItemImage)
            binding.collectionItemImage.setFitType(FIT_TYPE.TOP_CROP)
        }
        manageTvPlayIcon(view, card)
    }

    private fun manageTvPlayIcon(view: View, card: CommonAsset?) {
        val videoPlayIcon = view.findViewById<NHImageView>(R.id.tv_play_icon)
        if (card?.i_videoAsset() != null) {
            videoPlayIcon.setVisibility(View.VISIBLE)
        } else {
            videoPlayIcon.setVisibility(View.GONE)
        }
    }


    fun initViralCard(view: View, card: CommonAsset?) {

        val nsfwfilter = view.findViewById<View>(R.id.nsfw_filter)
        binding.memeContent.visibility = View.VISIBLE
        //  view.findViewById<View>(com.newshunt.dhutil.R.id.show_content_view).setOnClickListener
        //  (this)
        //view.findViewById<View>(R.id.viral_social_container)?.visibility = View.GONE

        val showNsfwFilter = PreferenceManager.getPreference(GenericAppStatePreference
                .SHOW_NSFW_FILTER, true) && (card?.i_viral()?.nsfw ?: false)
        nsfwfilter?.visibility = if (showNsfwFilter) View.VISIBLE else View.GONE


        setupView(binding.memeContent, card?.i_viral(), showNsfwFilter,
                DisplayCardType.VH_NORMAL, false, false, null, false)

        val width = getWidthOfCardView()
        val layoutparams = binding.memeContent?.layoutParams
        layoutparams?.width = width
        layoutparams?.height = if (width < getMaxHeightForMemeAndImage()) width else getMaxHeightForMemeAndImage()
        binding.memeContent?.layoutParams = layoutparams

        val cardViewConstraintLayout = view.findViewById<ConstraintLayout>(R.id.cardview_constraintlayout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(cardViewConstraintLayout);
        constraintSet.connect(R.id.source_name, ConstraintSet.TOP, R.id.meme_content,
                ConstraintSet.BOTTOM, CommonUtils.getDimension(R.dimen
                .collection_landing_source_name_marginTop));
        constraintSet.applyTo(cardViewConstraintLayout);
    }

    fun setupView(memeParent: View,
                  viralAsset: ViralAsset?,
                  nsfw: Boolean,
                  cardType: DisplayCardType,
                  isCardInsideNewsDetail: Boolean,
                  isCardInsideMemeCarouselDetail: Boolean,
                  carouselProperties: CarouselProperties2?,
                  showExpandButton: Boolean) {

        viralAsset ?: return

        val txtMeme = memeParent.findViewById<TextView>(R.id.txt_meme_text)
        val backGround = memeParent.findViewById<NHImageView>(R.id.img_meme)
        val readMoreText = memeParent.findViewById<View>(R.id.read_more_text)
        val expandButton = memeParent.findViewById<ImageView>(R.id.expand_button)
        val isGradientBg = viralAsset.backgroundOption == null || CommonUtils.equals(viralAsset.backgroundOption
        !!.type,
                BackgroundOption.BackgroundType.GRADIENT)

        if (!nsfw) {
            setUpReadMoreText(txtMeme, isCardInsideNewsDetail, viralAsset.itemText ?: "",
                    object : ShowReadMoreCallback {
                        override fun showReadMore(show: Boolean) {
                            if (readMoreText != null) {
                                readMoreText.visibility = if (show) View.VISIBLE else View.GONE
                            }
                        }
                    })
        } else {
            readMoreText!!.visibility = View.GONE
        }
        backGround.setImageDrawable(null)
        val playIndicator = memeParent.findViewById<NHImageView>(R.id.item_play_indicator)

        if (nsfw) {
            txtMeme.visibility = View.GONE
        } else {
            txtMeme.visibility = View.VISIBLE
            txtMeme.text = if (viralAsset.itemText == null) Constants.EMPTY_STRING else viralAsset.itemText
            txtMeme.setTextColor(ViewUtils.getColor(viralAsset.itemTextColor, Color.WHITE))
        }


        if (viralAsset.backgroundOption == null) {
            return
        }

        viralAsset.aspectRatio?.let {
            val parent = backGround.parent as? ConstraintLayout ?: return
            val set = ConstraintSet()
            set.clone(parent)
            set.setDimensionRatio(backGround.id, CardsBindUtils.getAspectRatioString(it))
            set.applyTo(parent)

        }
        setBackground(backGround, viralAsset, nsfw)

    }

    fun setBackground(backGround: NHImageView, viral: ViralAsset?, showNsfwFilter: Boolean) {

        val backgroundOption = viral?.backgroundOption
        if (backgroundOption == null) {
            backGround.visibility = View.GONE
            return
        }
        backGround.visibility = View.VISIBLE
        when (backgroundOption.type) {
            BackgroundType2.BG_COLOR -> {
                backGround.setBackgroundColor(ViewUtils.getColor(backgroundOption.bgColor, Color.TRANSPARENT))
            }

            BackgroundType2.GRADIENT -> {
                Glide.with(backGround).applyDefaultRequestOptions(RequestOptions().dontTransform()).load(getGradientDrawable(backgroundOption)).into(backGround)
            }

            BackgroundType2.IMAGE_BG -> {
                backGround.setFitType(getFitTypeConverted(backgroundOption.fitType))
                val imageAspectRatio = backgroundOption.width / Math.max(backgroundOption.height.toFloat(), 1f)
                val width = CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R
                        .dimen.story_card_padding)
                val height = width / imageAspectRatio
                val url = ImageUrlReplacer.getQualifiedImageUrl(backgroundOption.imageUrl, width,
                        height.toInt())
                val requestOption = if (showNsfwFilter && viral.nsfw) {
                    RequestOptions().transform(BlurTransformation()).dontAnimate()
                } else {
                    RequestOptions().dontTransform()
                }
                Image.load(url).apply(requestOption).into(backGround)

            }
        }
    }


    fun setUpReadMoreText(txtMeme: TextView?,
                          isCardInsideNewsDetail: Boolean,
                          text: String,
                          readMoreCallback: ShowReadMoreCallback?) {
        if (readMoreCallback == null) {
            return
        }

        if (txtMeme == null) {
            return
        }

        readMoreCallback!!.showReadMore(false)

        if (isCardInsideNewsDetail || CommonUtils.isEmpty(text)) {
            return
        }
        txtMeme!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val txtMemeHeight = txtMeme!!.height
                val lineCount = txtMeme!!.lineCount
                val textSize = txtMeme!!.textSize
                val spacingBetweenLinesInTextView = 3 // in dp.
                var desiredHeight = (lineCount + 1) * (textSize + CommonUtils.getPixelFromDP(spacingBetweenLinesInTextView, CommonUtils
                        .getApplication()))
                //Add top and bottom margins.
                desiredHeight = desiredHeight + 2 * CommonUtils.getDimension(com.newshunt.dhutil.R.dimen.viral_txt_meme_margin)
                // Add top padding to the view.
                desiredHeight = desiredHeight + CommonUtils.getPixelFromDP(spacingBetweenLinesInTextView, CommonUtils.getApplication())

                Logger.i("MemeViewHelper",
                        "Desired Height : " + desiredHeight + " ," +
                                " Actual Height :" + txtMemeHeight)
                if (desiredHeight > txtMemeHeight) {
                    readMoreCallback!!.showReadMore(true)
                }

                if (txtMeme!!.text == null || CommonUtils.isEmpty(txtMeme!!.text.toString())) {
                    txtMeme!!.viewTreeObserver.removeOnPreDrawListener(this)
                } else if (lineCount > 0) {
                    txtMeme!!.viewTreeObserver.removeOnPreDrawListener(this)
                }

                return true
            }
        })
    }


    private fun getWidthOfCardView(): Int {
        return CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R.dimen
                .collection_landing_image_left_right_top) - 2 * CommonUtils.getDimension(R.dimen
                .collection_guideline_dimens)
    }

    fun getMaxHeightForMemeAndImage(): Int {
        return CommonUtils.getDeviceScreenHeight() - CommonUtils.getDimension(R.dimen.collection_landing_other_elements_height)
    }


    private fun setTextOrMakeViewGone(text: String?, view: TextView) {
        if (CommonUtils.isEmpty(text)) {
            view.visibility = View.GONE
        } else
            view.text = text
    }


    private fun initActionBar(parent: View) {
        val toolbar = parent.findViewById<Toolbar>(R.id.action_bar)
        val toolbarBackButtonContainer = toolbar.findViewById<RelativeLayout>(R.id
                .actionbar_back_button_layout)
        toolbarBackButtonContainer.setOnClickListener(View.OnClickListener {
            activity?.onBackPressed()
        })


        toolbar.inflateMenu(R.menu.menu_newsdetail)
        toolbar.setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.action_more_newsdetail) {
            card?.let {
                val intent = Intent(Constants.MENU_FRAGMENT_OPEN_ACTION)
                val postIds = ArrayList<String>()
                postIds.add(it.i_id())
                intent.putStringArrayListExtra(Constants.BUNDLE_POST_IDS, postIds)
                intent.putExtra(Constants.BUNDLE_MENU_CLICK_LOCATION, MenuLocation.DETAIL)
                intent.putExtra(NewsConstants.DH_SECTION, section)
                intent.putExtra(Constants.BUNDLE_LOCATION_ID, "Detail")
                intent.putExtra(Constants.REFERRER,currentPageReferrer)
                intent.putExtra(Constants.BUNDLE_STORY, it.toMinimizedCommonAsset() as? Serializable)
                /*TODO : Pass entity id here*/
                intent.putExtra(Constants.BUNDLE_ENTITY_ID, "")
                NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = null))
                return true
            }
            return false
        }
        return false
    }

    override fun deeplinkUrl(): String? {
        return card?.i_deeplinkUrl()
    }

}