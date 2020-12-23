package org.wordpress.android.ui.sitecreation.theme

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.ListPopupWindow
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.elevation.ElevationOverlayProvider
import kotlinx.android.synthetic.main.home_page_picker_preview_fragment.*
import org.wordpress.android.R
import org.wordpress.android.R.dimen
import org.wordpress.android.WordPress
import org.wordpress.android.ui.FullscreenBottomSheetDialogFragment
import org.wordpress.android.ui.PreviewModeMenuAdapter
import org.wordpress.android.ui.sitecreation.theme.HomePagePickerViewModel.PreviewUiState.Error
import org.wordpress.android.ui.sitecreation.theme.HomePagePickerViewModel.PreviewUiState.Loaded
import org.wordpress.android.ui.sitecreation.theme.HomePagePickerViewModel.PreviewUiState.Loading
import org.wordpress.android.util.AniUtils
import org.wordpress.android.util.ToastUtils
import org.wordpress.android.util.setVisible
import org.wordpress.android.viewmodel.wpwebview.WPWebViewViewModel.PreviewMode
import org.wordpress.android.viewmodel.wpwebview.WPWebViewViewModel.PreviewMode.DEFAULT
import org.wordpress.android.viewmodel.wpwebview.WPWebViewViewModel.PreviewMode.DESKTOP
import javax.inject.Inject

/**
 * Implements the Home Page Picker Design Preview UI
 */
class DesignPreviewFragment : FullscreenBottomSheetDialogFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: HomePagePickerViewModel
    private lateinit var previewModeSelector: ListPopupWindow
    private lateinit var elevationOverlayProvider: ElevationOverlayProvider

    private lateinit var template: String
    private lateinit var url: String

    private var previewMode: PreviewMode = DEFAULT

    companion object {
        const val DESIGN_PREVIEW_TAG = "DESIGN_PREVIEW_TAG"
        private const val DESIGN_PREVIEW_TEMPLATE = "DESIGN_PREVIEW_TEMPLATE"
        private const val DESIGN_PREVIEW_URL = "DESIGN_PREVIEW_URL"

        fun newInstance(template: String, url: String) = DesignPreviewFragment().apply {
            arguments = Bundle().apply {
                putString(DESIGN_PREVIEW_TEMPLATE, template)
                putString(DESIGN_PREVIEW_URL, url)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            template = it.getString(DESIGN_PREVIEW_TEMPLATE, "")
            url = it.getString(DESIGN_PREVIEW_URL, "")
        }

        elevationOverlayProvider = ElevationOverlayProvider(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_page_picker_preview_fragment, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(HomePagePickerViewModel::class.java)

        viewModel.previewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is Loading -> {
                    desktopPreviewHint.setVisible(false)
                    progressBar.setVisible(true)
                    webView.setVisible(false)
                    errorView.setVisible(false)
                    webView.settings.loadWithOverviewMode = previewMode === DESKTOP
                    webView.settings.useWideViewPort = previewMode !== DESKTOP
                    webView.setInitialScale(100)
                    webView.loadUrl(url)
                }
                is Loaded -> {
                    progressBar.setVisible(false)
                    webView.setVisible(true)
                    errorView.setVisible(false)
                    if (previewMode == DESKTOP) {
                        AniUtils.animateBottomBar(desktopPreviewHint, true)
                    }
                }
                is Error -> {
                    progressBar.setVisible(false)
                    webView.setVisible(false)
                    errorView.setVisible(true)
                    state.toast?.let { ToastUtils.showToast(requireContext(), it) }
                }
            }
        })

        backButton.setOnClickListener { closeModal() }

        chooseButton.setOnClickListener { viewModel.onPreviewChooseTapped() }

        previewTypeSelectorButton.setOnClickListener { showModeSelector() }

        webView.settings.userAgentString = WordPress.getUserAgent()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                viewModel.onPreviewLoaded(template)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                viewModel.onPreviewError()
            }
        }

        errorView.button.setOnClickListener { load() }
        load()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().applicationContext as WordPress).component().inject(this)
    }

    override fun closeModal() = viewModel.onDismissPreview()

    private fun load() = viewModel.onPreviewLoading(template)

    private fun showModeSelector() {
        previewTypeSelectorButton.post(Runnable {
            val popupWidth = resources.getDimensionPixelSize(dimen.web_preview_mode_popup_width)
            val popupOffset = resources.getDimensionPixelSize(dimen.margin_extra_large)
            previewModeSelector = ListPopupWindow(requireActivity())
            previewModeSelector.width = popupWidth
            previewModeSelector.setAdapter(PreviewModeMenuAdapter(requireActivity(), previewMode))
            previewModeSelector.setDropDownGravity(Gravity.END)
            previewModeSelector.anchorView = previewTypeSelectorButton
            previewModeSelector.horizontalOffset = -popupOffset
            previewModeSelector.verticalOffset = popupOffset
            previewModeSelector.isModal = true
            val elevatedPopupBackgroundColor = elevationOverlayProvider.compositeOverlayWithThemeSurfaceColorIfNeeded(
                    resources.getDimension(dimen.popup_over_toolbar_elevation)
            )
            previewModeSelector.setBackgroundDrawable(ColorDrawable(elevatedPopupBackgroundColor))
            previewModeSelector.setOnItemClickListener { parent, _, position, _ ->
                previewModeSelector.dismiss()
                val adapter = parent.adapter as PreviewModeMenuAdapter
                val selectedMode = adapter.getItem(position)
                if (selectedMode != previewMode) {
                    previewMode = selectedMode
                    load()
                }
            }
            previewModeSelector.show()
        })
    }
}
