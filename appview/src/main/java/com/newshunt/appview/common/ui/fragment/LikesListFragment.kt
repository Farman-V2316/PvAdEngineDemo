package com.newshunt.appview.common.ui.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.newshunt.appview.R
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.model.apis.InteractionAPI
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.LikeAsset
import com.newshunt.dataentity.common.asset.LikeListPojo
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.LikeMultiValueResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.getFormattedCountForLikesAndComments
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.scan
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.Priority
import com.newshunt.sdk.network.image.Image
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by karthik.r on 2019-09-24.
 */
class LikesListFragment : BaseSupportFragment() {

    @Inject
    lateinit var likesViewModelF: LikesViewModel.Factory
    private lateinit var vm: LikesViewModel
    lateinit var postId: String
    var likeType: LikeType? = null
    private lateinit var likelistView: RecyclerView
    private lateinit var likesAdapter: LikesAdapter
    private lateinit var emptyLikes: NHTextView
    private lateinit var progressBar: ProgressBar
    private var pageReferrer: PageReferrer? = null
    private var guestCount: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.likes_list_fragment, container, false)
        likelistView = view.findViewById(R.id.likes_list)
        emptyLikes = view.findViewById(R.id.empty_likes)
        progressBar = view.findViewById(R.id.progress_bar)

        postId = arguments?.getString(Constants.BUNDLE_POST_ID) ?: ""
        pageReferrer = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        likeType = LikeType.fromName(arguments?.getString(Constants.BUNDLE_LIKE_TYPE))
        guestCount = arguments?.getInt(Constants.BUNDLE_GUEST_COUNT)

        DaggerLikesListComponent2.builder()
                .likesModule(LikesModule(
                        CommonUtils.getApplication(),
                        SocialDB.instance(),
                        postId, likeType)).build()
                .inject(this)
        vm = ViewModelProviders.of(this, likesViewModelF)[LikesViewModel::class.java]
        vm.likes.observe(viewLifecycleOwner, Observer {
            if (it.count != null) {
                likesAdapter.setTotalCount(it.count!!)
            }

            progressBar.visibility = View.GONE
            if (it.count == 0) {
                emptyLikes.visibility = View.VISIBLE
                likelistView.visibility = View.GONE
            }
            else {
                emptyLikes.visibility = View.GONE
                likelistView.visibility = View.VISIBLE
            }

            likesAdapter.nextPageUrl = it.likesNextPageUrl
            if (likeType == null) {
                likesAdapter.setGuestCountItems(it.guestUserCount)
                if (likesAdapter.itemCount > 0) {
                    emptyLikes.visibility = View.GONE
                    likelistView.visibility = View.VISIBLE
                }
            }
            likesAdapter.updateData(it.data)
        })

        vm.nextLikes.observe(viewLifecycleOwner, Observer {
            likesAdapter.nextPageUrl = it.likesNextPageUrl
            likesAdapter.appendData(it.data)
        })

        likesAdapter = LikesAdapter(vm, pageReferrer)
        likelistView.adapter = likesAdapter
        likelistView.layoutManager = LinearLayoutManager(this.activity)

        if (likeType == null) {
            likesAdapter.setGuestCountItems(guestCount)
        }

        vm.loadFirstPage()
        return view
    }
}

class LikesAdapter(val vm: LikesViewModel, val referrer: PageReferrer?) : RecyclerView
.Adapter<RecyclerView.ViewHolder>() {

    private val items = ArrayList<LikeAsset>()

    private var totalCount = 0
    private var guestUserCount: Int? = null
    var nextPageUrl: String? = null

    fun setTotalCount(count: Int) {
        totalCount = count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == DetailCardType.GUEST_DETAIL_FOOTER.index) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.detail_guest_vh, parent,
                    false)
            return GuestListViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.liked_detail_item, parent,
                    false)
            return LikesViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == items.size)
            return DetailCardType.GUEST_DETAIL_FOOTER.index
        else
            return DetailCardType.DETAIL_LIKES.index
    }

    override fun getItemCount(): Int {
        return items.size + (if ((guestUserCount ?: 0) > 0) 1 else 0)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position >= items.size - 3) {
            // Load more
            if (nextPageUrl != null) {
                vm.loadNextPage(nextPageUrl!!)
            }
        }

        if (holder is LikesViewHolder) {
            val item = items[position]
            val size = CommonUtils.getDimension(R.dimen.profile_circle_x)
            Image.load(ImageUrlReplacer.getQualifiedImageUrl(item.actionableEntity?.entityImageUrl
                    ?: "", size, size))
                    .placeHolder(R.drawable.ic_default_profile)
                    .into(holder.profileimage)
            holder.displayName.text = item.actionableEntity?.displayName ?: ""
            val handle = item.actionableEntity?.handle
            if (handle != null) {
                holder.handleName.text = Constants.AT_SYMBOL + handle
            }
            holder.description.text = item.description ?: ""

            holder.view.setOnClickListener { v ->
                CommonNavigator.launchProfileActivity(holder.view.context,
                        UserBaseProfile().apply {
                            this.name = item.actionableEntity?.displayName ?: ""
                            this.handle = item.actionableEntity?.handle ?: ""
                            this.profileImage = item.actionableEntity?.entityImageUrl
                                    ?: ""

                        }, PageReferrer(NewsReferrer.LIKE_CAROUSEL))
            }
        }

        // guest footer
        if (holder is GuestListViewHolder) {
            val guestUserCountInt = guestUserCount
            if (guestUserCountInt != null && guestUserCountInt > 0) {
                holder.guestcount.text = CommonUtils.getString(R.string.follower_guest_user_count,
                        getFormattedCountForLikesAndComments(guestUserCountInt.toLong()))
            }
            else {
                holder.guestcount.text = Constants.EMPTY_STRING
            }

            // imageurl from response
            Image.load("")
                    .placeHolder(R.color.empty_image_color)
                    .into(holder.guestUserIcon1)
            Image.load("")
                    .placeHolder(R.color.empty_image_color)
                    .into(holder.guestUserIcon2)
        }
    }

    fun setGuestCountItems(guestUserCount: Int?) {
        this.guestUserCount = guestUserCount
        if (itemCount > 0) {
            notifyItemChanged(itemCount - 1)
        }
    }


    fun updateData(data: List<LikeAsset>?) {
        if (data == null) {
            return
        }

        val diffCallback = LikesAdapterDiffUtilCallback(this.items, data)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.items.clear()
        this.items.addAll(data)
        diffResult.dispatchUpdatesTo(this)
    }

    fun appendData(data: List<LikeAsset>?) {
        if (data == null) {
            return
        }

        val newList = ArrayList<LikeAsset>()
        newList.addAll(this.items)
        data.forEach {
            newList.add(it)
        }

        val diffCallback = LikesAdapterDiffUtilCallback(this.items, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.items.clear()
        this.items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}

class LikesAdapterDiffUtilCallback(private val mOldItemList: List<LikeAsset>,
                                   private val mNewItemList: List<LikeAsset>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return mOldItemList.size
    }

    override fun getNewListSize(): Int {
        return mNewItemList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val postItemOld = mOldItemList[oldItemPosition] as? LikeAsset ?: return false
        val postItemNew = mNewItemList[newItemPosition] as? LikeAsset ?: return false
        return postItemNew.actionableEntity?.handle == postItemOld.actionableEntity?.handle
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val postItemOld = mOldItemList[oldItemPosition] as? LikeAsset ?: return false
        val postItemNew = mNewItemList[newItemPosition] as? LikeAsset ?: return false
        return postItemNew == postItemOld
    }
}

class LikesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val profileimage = view.findViewById<NHImageView>(R.id.profile_image)
    val displayName = view.findViewById<NHTextView>(R.id.display_name)
    val handleName = view.findViewById<NHTextView>(R.id.handle_name)
    val description = view.findViewById<NHTextView>(R.id.description)
}

class GuestListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val guestcount = view.findViewById<NHTextView>(R.id.guest_title)
    val guestUserIcon1 = view.findViewById<ImageView>(R.id.user_profile_image_1)
    val guestUserIcon2 = view.findViewById<ImageView>(R.id.user_profile_image_2)
}

@Component(modules = [LikesModule::class])
interface LikesListComponent2 {
    fun inject(component: LikesListFragment)
}

class LikesViewModel(context: Application,
                     private val socialDB: SocialDB,  // should be inherited
                     private val likesUsecase: MediatorUsecase<Bundle, LikeMultiValueResponse>,
                     private val nextLikesUsecase: MediatorUsecase<Bundle, MultiValueResponse<LikeAsset>>,
                     private val postId: String,
                     private val likeType: LikeType?) : AndroidViewModel(context) {

    val likes: LiveData<LikeListPojo> = likesUsecase.data().scan(LikeListPojo())
    { acc, t ->
        if (t.isSuccess) {
            acc.copy(data = t.getOrNull()?.rows,
                    count = t.getOrNull()?.count,
                    guestUserCount = t.getOrNull()?.guestUserCount,
                    likesNextPageUrl = t.getOrNull()?.nextPageUrl,
                    tsData = System.currentTimeMillis())
        } else {
            acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    }

    fun loadFirstPage() {
        val bundle = Bundle()
        bundle.putString(Constants.BUNDLE_POST_ID, postId)
        bundle.putInt(Constants.BUNDLE_START, 0)
        likesUsecase.execute(bundle)
    }

    fun loadNextPage(nextPageUrl: String) {
        val bundle = Bundle()
        bundle.putString(Constants.CONTENT_URL, nextPageUrl)
        nextLikesUsecase.execute(bundle)
    }

    val nextLikes: LiveData<LikeListPojo> = nextLikesUsecase.data().scan(LikeListPojo()) { acc, t ->
        if (t.isSuccess) {
            acc.copy(data = t.getOrNull()?.rows,
                    count = t.getOrNull()?.count,
                    likesNextPageUrl = t.getOrNull()?.nextPageUrl,
                    tsData = System.currentTimeMillis())
        } else {
            acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    }

    class Factory @Inject constructor(private val app: Application,
                                      private val readLikesUsecase: ReadLikesUsecase,
                                      private val readNextLikesUsecase: ReadNextLikesUsecase,
                                      @Named("postId") val postId: String,
                                      @Named("likeType") val likeType: LikeType?) :
            ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LikesViewModel(
                    app,
                    SocialDB.instance(),
                    readLikesUsecase.toMediator2(true),
                    readNextLikesUsecase.toMediator2(true),
                    postId, likeType
            ) as T
        }
    }
}


@Module
class LikesModule(private val app: Application, // should be inherited
                  private val socialDB: SocialDB,  // should be inherited
                  private val postId: String,  // should be inherited
                  private val likeType: LikeType?) {
    @Provides
    fun app() = app

    @Provides
    @Named("postId")
    fun postId(): String = postId

    @Provides
    @Named("likeType")
    fun likeType(): LikeType? = likeType

    @Provides
    fun fetchDao() = socialDB.fetchDao()

    @Provides
    fun followDao() = socialDB.followEntityDao()

    @Provides
    fun followRepo() = FollowRepo(followDao())

    @Provides
    fun voteDao() = socialDB.voteDao()

    @Provides
    fun interactionDao() = socialDB.interactionsDao()

    @Provides
    fun api() = RestAdapterContainer.getInstance()
            .getRestAdapter(CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSocialFeaturesBaseUrl()),
                    Priority.PRIORITY_HIGHEST, "")
            .create(InteractionAPI::class.java)
}


// TODO : Once we get first page URL in Second Chunk response, we can remove this Usecase and use
//  only the one based on URL.
class ReadLikesUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("likeType") private val likeType: LikeType?,
                    private val api: InteractionAPI) :
        BundleUsecase<LikeMultiValueResponse> {

    override fun invoke(p1: Bundle): Observable<LikeMultiValueResponse> {
        val likeTypeStr: String? = likeType?.name
        val count = 10
        return api.getLikesForPost("UNIFIED", postId, "POST", 0, count, likeTypeStr).map {
            it.data
        }
    }
}

class ReadNextLikesUsecase
@Inject constructor(private val api: InteractionAPI) : BundleUsecase<MultiValueResponse<LikeAsset>> {

    override fun invoke(p1: Bundle): Observable<MultiValueResponse<LikeAsset>> {

        val url = p1.getString(Constants.CONTENT_URL)
        if (url != null) {
            return api.getNextLikesForPost(url).map {
                it.data
            }
        }

        return Observable.just(MultiValueResponse())
    }
}
