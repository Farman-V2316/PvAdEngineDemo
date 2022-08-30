/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view.customview;

import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.common.model.entity.model.Status;
import com.newshunt.dataentity.common.model.entity.model.StatusError;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.model.service.BaseService;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Provides common list view which can be reused with following features
 * 1. Single column list view / multi column staggered grid view
 * 2. Pull to refresh option
 * 3. Infinite scroll option to load next pages
 * 4. Taking care of no connectivity, loading failure errors
 * 5. onItemClickListener for items
 *
 * @author arun.babu
 */
public class NHListView extends RelativeLayout {
  private static final int SPAN_COUNT = 1;

  private RecyclerView recyclerView;
  public SwipeRefreshLayout swipeRefreshLayout;
  private ProgressBar pbInitialize;

  private RelativeLayout rlInfiniteScrollLayout;
  private ProgressBar pbInfiniteScroll;
  private LinearLayout tvRetryView;

  private RecyclerView.LayoutManager layoutManager;
  private Adapter adapter;
  private EndlessScrollListener endlessScrollListener;
  private boolean isFromSwipeToRefresh;


  private boolean isBackGroundRefresh;


  public NHListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public NHListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public NHListView(Context context) {
    super(context);
    init();
  }

  public Adapter getAdapter() {
    return adapter;
  }

  public void setAdapter(Adapter adapter) {
    this.adapter = adapter;
    recyclerView.setAdapter(adapter);
  }

  public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
    this.layoutManager = layoutManager;
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(true);
  }

  private void init() {
    final RelativeLayout root =
        (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.nhlistview, this, true);
    layoutManager = new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);

    recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(layoutManager);
    endlessScrollListener = new EndlessScrollListener(this);
    recyclerView.setOnScrollListener(endlessScrollListener);
    recyclerView.setHasFixedSize(true);

    swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
    swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light);
    swipeRefreshLayout.setOnRefreshListener(new RefreshListener(this));
    swipeRefreshLayout.setEnabled(true);
    swipeRefreshLayout.setRefreshing(true);

    pbInitialize = (ProgressBar) root.findViewById(R.id.init_progress_bar);
    pbInitialize.setVisibility(VISIBLE);

    pbInfiniteScroll = (ProgressBar) root.findViewById(R.id.footer_progress);
    pbInfiniteScroll.setVisibility(View.GONE);

    tvRetryView = (LinearLayout) root.findViewById(R.id.refresh_layout);
    tvRetryView.setVisibility(View.GONE);

    rlInfiniteScrollLayout = (RelativeLayout) root.findViewById(
        R.id.infinite_scroll_layout);
    rlInfiniteScrollLayout.setVisibility(View.GONE);

    tvRetryView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        rlInfiniteScrollLayout.setVisibility(View.GONE);
        adapter.loadNextPage();
      }
    });
  }

  private void showLoadFirstPageProgressBar() {
    pbInitialize.setVisibility(View.VISIBLE);
    swipeRefreshLayout.setVisibility(View.GONE);
    rlInfiniteScrollLayout.setVisibility(View.GONE);
  }

  public void enableBottomMargin(boolean enableBottomMargin) {
    int bottom = enableBottomMargin ? CommonUtils.getDimension(R.dimen.book_list_bottom_bar_height) +
        CommonUtils.getDimension(R.dimen.infinite_scroll_layout_margin) :
        CommonUtils.getDimension(R.dimen.infinite_scroll_layout_margin);

    ((LayoutParams) rlInfiniteScrollLayout.getLayoutParams()).bottomMargin = bottom;
  }

  /**
   * Model for Retrofit service interface for server response
   *
   * @param <FIRST_PAGE_RESPONSE>
   * @param <NEXT_PAGE_RESPONSE>
   */
  public interface Model<FIRST_PAGE_RESPONSE, NEXT_PAGE_RESPONSE> {

    /**
     * Callback to execute rest API for fetching first page
     *
     * @param path
     * @param callback
     */
    void executeFirstPageAPI(String path, Callback<ApiResponse<FIRST_PAGE_RESPONSE>> callback);

    /**
     * Callback to execute rest API for fetching next page
     *
     * @param path
     * @param callback
     */
    void executeNextPageAPI(String path, Callback<ApiResponse<NEXT_PAGE_RESPONSE>> callback);
  }

  /**
   * Presenter for server response to UI communication.
   *
   * @param <FIRST_PAGE_RESPONSE>
   * @param <NEXT_PAGE_RESPONSE>
   */
  public interface Presenter<FIRST_PAGE_RESPONSE, NEXT_PAGE_RESPONSE> {

    /**
     * Helper for getting REST api through interfaces
     */
    Model getModel();

    /**
     * Process first page response of the API
     *
     * @param firstPageResponse
     */
    void processFirstPageResponse(FIRST_PAGE_RESPONSE firstPageResponse, int uniqueId);

    /**
     * Process next page response of the API
     *
     * @param nextPageResponse
     */
    void processNextPageResponse(NEXT_PAGE_RESPONSE nextPageResponse, int uniqueId);

    /**
     * Error on the Response.
     *
     * @param status
     */
    void onErrorResponse(Status status, int uniqueId);

    /**
     * Error on the next page response
     */
    void onNextPageError(Status status, int uniqueId);

    /**
     * To enabled cache for the first page request
     *
     * @param firstPagePath
     * @return
     */
    FIRST_PAGE_RESPONSE getCachedResponse(String firstPagePath);
  }

  /**
   * For Swipe refresh in listview
   */
  public static class RefreshListener implements SwipeRefreshLayout.OnRefreshListener {

    private final NHListView listView;

    public RefreshListener(NHListView listView) {
      this.listView = listView;
    }

    @Override
    public void onRefresh() {
      listView.endlessScrollListener.reset();
      listView.isFromSwipeToRefresh = true;
      // Bug 14527 : Happening due to recycler view library update.
      // if the swipe is done from pull to refresh then we should set the nextpage url to null
      // because we are loading always from the first page.
      listView.adapter.setNextPagePath(null);
      listView.adapter.loadFirstPage();

    }
  }

  /**
   * Adapter which calling modules shall implement.
   */
  public static abstract class Adapter<FIRST_PAGE_RESPONSE, NEXT_PAGE_RESPONSE>
      extends RecyclerView.Adapter {

    NHListView listView = null;
    Context context = null;
    private Model model;
    private Presenter presenter;
    private String firstPagePath;
    private String nextPagePath;

    public Adapter(NHListView listView, Context context) {
      this.listView = listView;
      this.context = context;
    }

    public void init(String firstPagePath) {
      this.firstPagePath = firstPagePath;
      listView.endlessScrollListener.reset();
      if (CommonUtils.isEmpty(firstPagePath)) {
        return;
      }

      presenter = getPresenter();
      if (presenter != null) {
        model = presenter.getModel();
      } else {
        model = null;
      }
      loadFirstPage();
    }

    public void setNextPagePath(String nextPagePath) {
      this.nextPagePath = nextPagePath;
    }

    protected abstract Presenter getPresenter();

    protected abstract int getUniqueId();

    protected void onFirstPageLoadStarted() {
    }

    public void loadFirstPage() {
      boolean showProgress = true;
      presenter.getCachedResponse(firstPagePath);
      onFirstPageLoadStarted();
      listView.setAdapter(listView.adapter);
      notifyDataSetChanged();
      if (!listView.isFromSwipeToRefresh && showProgress) {
        listView.showLoadFirstPageProgressBar();
      }

      //TODO(arun.babu) Implementation to be moved to model
      BaseService<FIRST_PAGE_RESPONSE> service = new BaseService<FIRST_PAGE_RESPONSE>() {
        @Override
        protected void handleResponse(FIRST_PAGE_RESPONSE response, Response apiResponse, int
            uniqueId) {
          listView.swipeRefreshLayout.setRefreshing(false);
          listView.swipeRefreshLayout.setVisibility(View.VISIBLE);
          listView.pbInitialize.setVisibility(GONE);
          //resetting the isFromSwipeToRefresh boolean.
          listView.isFromSwipeToRefresh = false;

          if (null == presenter) {
            return;
          }
          presenter.processFirstPageResponse(response, uniqueId);
          notifyDataSetChanged();
          listView.endlessScrollListener.onListUpdate();
        }

        @Override
        protected void handleError(Status status, int uniqueId) {
          listView.swipeRefreshLayout.setRefreshing(false);
          listView.swipeRefreshLayout.setVisibility(View.GONE);
          listView.pbInitialize.setVisibility(GONE);
          //resetting the isFromSwipeToRefresh boolean.
          listView.isFromSwipeToRefresh = false;

          if (null == presenter) {
            return;
          }
          presenter.onErrorResponse(status, uniqueId);
        }

        @Override
        protected void execute(Callback<ApiResponse<FIRST_PAGE_RESPONSE>> callback) {
          if (null == model) {
            return;
          }

          model.executeFirstPageAPI(firstPagePath, callback);
        }
      };

      service.request(getUniqueId());
      listView.swipeRefreshLayout.setRefreshing(true);
    }

    private void loadNextPage() {

      //Error layout visibility
      listView.rlInfiniteScrollLayout.setVisibility(View.VISIBLE);
      listView.pbInfiniteScroll.setVisibility(View.VISIBLE);
      listView.tvRetryView.setVisibility(View.GONE);

      //TODO(arun.babu) Implementation to be moved to model
      BaseService<NEXT_PAGE_RESPONSE> service = new BaseService<NEXT_PAGE_RESPONSE>() {

        @Override
        protected void handleResponse(NEXT_PAGE_RESPONSE response, Response apiResponse,
                                      int uniqueId) {
          listView.swipeRefreshLayout.setRefreshing(false);
          listView.rlInfiniteScrollLayout.setVisibility(View.GONE);
          if (null == presenter) {
            return;
          }

          presenter.processNextPageResponse(response, uniqueId);
          notifyDataSetChanged();
          // This is to make sure to call load next page when there is next page url and all the
          // items in the list are visible
          listView.endlessScrollListener.onListUpdate();
        }

        @Override
        protected void handleError(Status status, int uniqueId) {
          listView.swipeRefreshLayout.setRefreshing(false);
          //Error layout visibility
          listView.rlInfiniteScrollLayout.setVisibility(View.VISIBLE);
          listView.pbInfiniteScroll.setVisibility(View.GONE);
          StatusError statusError = status.getCodeType();
          TextView textView = (TextView) listView.tvRetryView.findViewById(R.id.error_message);
          listView.tvRetryView.setVisibility(View.VISIBLE);
          switch (statusError) {
            case NETWORK_ERROR:
              textView.setText(R.string.error_no_connection);
              break;
            default:
              textView.setText(R.string.no_content_found);
          }
          presenter.onNextPageError(status, uniqueId);
        }

        @Override
        protected void execute(Callback<ApiResponse<NEXT_PAGE_RESPONSE>>
                                   callback) {
          if (null == model) {
            return;
          }

          model.executeNextPageAPI(nextPagePath, callback);
        }
      };
      service.request(getUniqueId());
    }
  }

  private static class EndlessScrollListener extends RecyclerView.OnScrollListener {

    private final NHListView listView;
    // The minimum amount of items to have below your current scroll position before loading more.
    private final int visibleThreshold = 2;
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true;  // True if still waiting for the last set of data to load.

    public EndlessScrollListener(NHListView listView) {
      this.listView = listView;
    }

    private void reset() {
      firstVisibleItem = 0;
      visibleItemCount = 0;
      totalItemCount = 0;
      previousTotal = 0;
      loading = true;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);
      onListUpdate();
    }

    private void onListUpdate() {
      // TODO(Santosh.kulkarni) need to check logic and remove try catch
      try {
        visibleItemCount = listView.recyclerView.getChildCount();
        totalItemCount = listView.adapter.getItemCount();
        firstVisibleItem = findFirstVisibleItemPosition();

        if (loading && totalItemCount > previousTotal) {
          loading = false;
          previousTotal = totalItemCount;
        }

        if (CommonUtils.isEmpty(listView.adapter.nextPagePath)) {
          return;
        }

        if (!loading &&
            (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
          previousTotal = totalItemCount;
          listView.adapter.loadNextPage();
          loading = true;
        }
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    }

    private int findFirstVisibleItemPosition() {
      if (listView.layoutManager instanceof StaggeredGridLayoutManager) {
        int[] firstPos = new int[SPAN_COUNT];
        return ((StaggeredGridLayoutManager) listView.layoutManager).findFirstVisibleItemPositions(
            firstPos)[0];
      } else if (listView.layoutManager instanceof LinearLayoutManager) {
        return ((LinearLayoutManager) listView.layoutManager).findFirstVisibleItemPosition();
      }

      return 0;
    }
  }

  public boolean isBackGroundRefresh() {
    return isBackGroundRefresh;
  }

  public void setBackGroundRefresh(boolean isBackGroundRefresh) {
    this.isBackGroundRefresh = isBackGroundRefresh;
  }

  public void enableSwipeRefresh(boolean enable) {
    if (swipeRefreshLayout != null) {
      swipeRefreshLayout.setEnabled(enable);
    }
  }

}