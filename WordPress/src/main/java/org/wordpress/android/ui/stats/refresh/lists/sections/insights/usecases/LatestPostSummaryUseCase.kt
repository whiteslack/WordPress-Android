package org.wordpress.android.ui.stats.refresh.lists.sections.insights.usecases

import kotlinx.coroutines.experimental.CoroutineDispatcher
import org.wordpress.android.R
import org.wordpress.android.R.string
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.stats.InsightsLatestPostModel
import org.wordpress.android.fluxc.store.InsightsStore
import org.wordpress.android.fluxc.store.StatsStore.InsightsTypes.LATEST_POST_SUMMARY
import org.wordpress.android.modules.UI_THREAD
import org.wordpress.android.ui.stats.refresh.lists.NavigationTarget.AddNewPost
import org.wordpress.android.ui.stats.refresh.lists.NavigationTarget.SharePost
import org.wordpress.android.ui.stats.refresh.lists.NavigationTarget.ViewPostDetailStats
import org.wordpress.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.StatelessUseCase
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem.Link
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem.Title
import javax.inject.Inject
import javax.inject.Named

class LatestPostSummaryUseCase
@Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    private val insightsStore: InsightsStore,
    private val latestPostSummaryMapper: LatestPostSummaryMapper
) : StatelessUseCase<InsightsLatestPostModel>(LATEST_POST_SUMMARY, mainDispatcher) {
    override suspend fun loadCachedData(site: SiteModel) {
        val dbModel = insightsStore.getLatestPostInsights(site)
        dbModel?.let { onModel(it) }
    }

    override suspend fun fetchRemoteData(site: SiteModel, forced: Boolean) {
        val response = insightsStore.fetchLatestPostInsights(site, forced)
        val model = response.model
        val error = response.error

        when {
            error != null -> onError(
                    error.message ?: error.type.name
            )
            else -> onModel(model)
        }
    }

    override fun buildModel(model: InsightsLatestPostModel): List<BlockListItem> {
        val items = mutableListOf<BlockListItem>()
        items.add(Title(string.stats_insights_latest_post_summary))
        items.add(latestPostSummaryMapper.buildMessageItem(model))
        if (model.hasData()) {
            items.add(
                    latestPostSummaryMapper.buildColumnItem(
                            model.postViewsCount,
                            model.postLikeCount,
                            model.postCommentCount
                    )
            )
            if (model.dayViews.isNotEmpty()) {
                items.add(latestPostSummaryMapper.buildBarChartItem(model.dayViews))
            }
        }
        items.add(buildLink(model))
        return items
    }

    private fun InsightsLatestPostModel.hasData() =
            this.postViewsCount > 0 || this.postCommentCount > 0 || this.postLikeCount > 0

    private fun buildLink(model: InsightsLatestPostModel?): Link {
        return when {
            model == null -> Link(R.drawable.ic_create_blue_medium_24dp, R.string.stats_insights_create_post) {
                navigateTo(AddNewPost)
            }
            model.hasData() -> Link(text = R.string.stats_insights_view_more) {
                navigateTo(
                        ViewPostDetailStats(
                                model.siteId,
                                model.postId.toString(),
                                model.postTitle,
                                model.postURL
                        )
                )
            }
            else -> Link(R.drawable.ic_share_blue_medium_24dp, R.string.stats_insights_share_post) {
                navigateTo(SharePost(model.postURL, model.postTitle))
            }
        }
    }
}
