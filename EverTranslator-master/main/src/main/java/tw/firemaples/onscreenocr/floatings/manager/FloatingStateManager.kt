package tw.firemaples.onscreenocr.floatings.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.dialog.showErrorDialog
import tw.firemaples.onscreenocr.floatings.main.MainBar
import tw.firemaples.onscreenocr.floatings.result.ResultView
import tw.firemaples.onscreenocr.floatings.screenCircling.ScreenCirclingView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor
import tw.firemaples.onscreenocr.translator.MicrosoftAzureTranslator
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.TranslationResult
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import java.io.IOException
import kotlin.reflect.KClass

object FloatingStateManager {
    private val logger: Logger by lazy { Logger(FloatingStateManager::class) }
    private val context: Context by lazy { Utils.context }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val currentStateFlow = MutableStateFlow<State>(State.Idle)
    val currentState: State
        get() = currentStateFlow.value

    private val mainBar: MainBar by lazy { MainBar(context) }
    private val screenCirclingView: ScreenCirclingView by lazy {
        ScreenCirclingView(context).apply {
            onAreaSelected = { parent, selected ->
                this@FloatingStateManager.onAreaSelected(parent, selected)
            }
        }
    }
    private val resultView: ResultView by lazy {
        ResultView(context).apply {
            onUserDismiss = {
                this@FloatingStateManager.backToIdle()
            }
        }
    }

    val showingStateChangedFlow = MutableStateFlow(false)
    val isMainBarAttached: Boolean
        get() = mainBar.attached

    private var selectedOCRLang: String = Constants.DEFAULT_OCR_LANG
    private val selectedOCRProvider: TextRecognitionProviderType get() = AppPref.selectedOCRProvider
    private var parentRect: Rect? = null
    private var selectedRect: Rect? = null
    private var croppedBitmap: Bitmap? = null

    fun showMainBar() {
        if (isMainBarAttached) return
        mainBar.attachToScreen()
        scope.launch {
            showingStateChangedFlow.emit(true)
        }
    }

    private fun hideMainBar() {
        if (!isMainBarAttached) return
        mainBar.detachFromScreen()
        scope.launch {
            showingStateChangedFlow.emit(false)
        }
    }

    private fun arrangeMainBarToTop() {
        mainBar.detachFromScreen()
        mainBar.attachToScreen()
    }

    fun detachAllViews() {
        backToIdle()
        scope.launch {
            hideMainBar()
            FloatingView.detachAllFloatingViews()
        }
    }

    fun startScreenCircling() = stateIn(State.Idle::class) {
        if (!Translator.getTranslator().checkEnvironment(scope)) {
            return@stateIn
        }

        logger.debug("startScreenCircling()")
        changeState(State.ScreenCircling)
        FirebaseEvent.logStartAreaSelection()
        screenCirclingView.attachToScreen()
        arrangeMainBarToTop()
    }

    private fun onAreaSelected(parentRect: Rect, selectedRect: Rect) =
        stateIn(State.ScreenCircling::class, State.ScreenCircled::class) {
            logger.debug("onAreaSelected(), parentRect: $parentRect, selectedRect: $selectedRect, size: ${selectedRect.width()}x${selectedRect.height()}")
            if (currentState != State.ScreenCircled) {
                changeState(State.ScreenCircled)
            }
            this@FloatingStateManager.selectedRect = selectedRect
            this@FloatingStateManager.parentRect = parentRect
        }

    fun cancelScreenCircling() = stateIn(State.ScreenCircling::class, State.ScreenCircled::class) {
        logger.debug("cancelScreenCircling()")
        changeState(State.Idle)
        screenCirclingView.detachFromScreen()
    }

    fun startScreenCapturing(selectedOCRLang: String) = stateIn(State.ScreenCircled::class) {
        this@FloatingStateManager.selectedOCRLang = selectedOCRLang
        val parent = parentRect ?: return@stateIn
        val selected = selectedRect ?: return@stateIn
        logger.debug("startScreenCapturing(), parentRect: $parent, selectedRect: $selected")
        changeState(State.ScreenCapturing)
        mainBar.detachFromScreen()
        screenCirclingView.detachFromScreen()

        delay(100L)

        try {
            FirebaseEvent.logStartCaptureScreen()
            val croppedBitmap =
                ScreenExtractor.extractBitmapFromScreen(parentRect = parent, cropRect = selected)
            this@FloatingStateManager.croppedBitmap = croppedBitmap
            FirebaseEvent.logCaptureScreenFinished()

            mainBar.attachToScreen()

            startRecognition(croppedBitmap, parent, selected)
        } catch (t: TimeoutCancellationException) {
            logger.debug(t = t)
            showError(context.getString(R.string.error_capture_screen_timeout))
            FirebaseEvent.logCaptureScreenFailed(t)
        } catch (t: Throwable) {
            logger.debug(t = t)
            showError(t.message ?: context.getString(R.string.error_unknown_error_capturing_screen))
            FirebaseEvent.logCaptureScreenFailed(t)
        }
//        screenCirclingView.detachFromScreen() // To test circled area
    }

    private fun startRecognition(croppedBitmap: Bitmap, parent: Rect, selected: Rect) =
        stateIn(State.ScreenCapturing::class) {
            changeState(State.TextRecognizing)
            try {
                resultView.startRecognition()
                val recognizer = TextRecognizer.getRecognizer(selectedOCRProvider)
                FirebaseEvent.logStartOCR(recognizer.name)
                val result = withContext(Dispatchers.Default) {
                    recognizer.recognize(
                        TextRecognizer.getLanguage(selectedOCRLang, selectedOCRProvider)!!,
                        croppedBitmap
                    )
                }
                logger.debug("On text recognized: $result")
//                croppedBitmap.recycle() // to be used in the text editor view
                FirebaseEvent.logOCRFinished(recognizer.name)
                resultView.textRecognized(result, parent, selected, croppedBitmap)
                startTranslation(result)
            } catch (e: Exception) {
                val error =
                    if (e.message?.contains(Constants.errorInputImageIsTooSmall) == true) {
                        context.getString(R.string.error_selected_area_too_small)
                    } else
                        e.message
                            ?: context.getString(R.string.error_an_unknown_error_found_while_recognition_text)

                logger.warn(t = e)
                showError(error)
                FirebaseEvent.logOCRFailed(
                    TextRecognizer.getRecognizer(selectedOCRProvider).name, e
                )
            }
        }

    fun startTranslation(recognitionResult: RecognitionResult) =
        stateIn(State.TextRecognizing::class, State.ResultDisplaying::class) {
            try {
                changeState(State.TextTranslating)

                val translator = Translator.getTranslator()

                resultView.startTranslation(translator.type)

                FirebaseEvent.logStartTranslationText(
                    recognitionResult.result,
                    recognitionResult.langCode,
                    translator
                )

                val translationResult = translator
                    .translate(recognitionResult.result, recognitionResult.langCode)

                when (translationResult) {
                    TranslationResult.OuterTranslatorLaunched -> {
                        FirebaseEvent.logTranslationTextFinished(translator)
                        backToIdle()
                    }
                    is TranslationResult.SourceLangNotSupport -> {
                        FirebaseEvent.logTranslationSourceLangNotSupport(
                            translator, recognitionResult.langCode,
                        )
                        showResult(
                            Result.SourceLangNotSupport(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                                providerType = translationResult.type,
                            )
                        )
                    }
                    TranslationResult.OCROnlyResult -> {
                        FirebaseEvent.logTranslationTextFinished(translator)
                        showResult(
                            Result.OCROnly(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                            )
                        )
                    }
                    is TranslationResult.TranslatedResult -> {
                        FirebaseEvent.logTranslationTextFinished(translator)
                        showResult(
                            Result.Translated(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                                translatedText = translationResult.result,
                                providerType = translationResult.type,
                            )
                        )
                    }
                    is TranslationResult.TranslationFailed -> {
                        FirebaseEvent.logTranslationTextFailed(translator)
                        val error = translationResult.error

                        if (error is MicrosoftAzureTranslator.Error) {
                            FirebaseEvent.logMicrosoftTranslationError(error)
                        }

                        if (error is IOException) {
                            showError(context.getString(R.string.error_can_not_connect_to_translation_server))
                        } else {
                            FirebaseEvent.logException(error)
                            showError(
                                error.localizedMessage
                                    ?: context.getString(R.string.error_unknown)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.warn(t = e)
                FirebaseEvent.logException(e)
                showError(e.message ?: "Unknown error found while translating")
            }
        }

    private fun showResult(result: Result) =
        stateIn(State.TextTranslating::class) {
            logger.debug("showResult(), $result")
            changeState(State.ResultDisplaying)

            resultView.textTranslated(result)
        }

    private fun showError(error: String) {
        scope.launch {
            changeState(State.ErrorDisplaying(error))
            context.showErrorDialog(error)
            backToIdle()
        }
    }

    private fun backToIdle() =
        scope.launch {
            if (currentState != State.Idle) changeState(State.Idle)
            croppedBitmap?.recycle()
            resultView.backToIdle()
            showMainBar()
        }

    private fun stateIn(
        vararg states: KClass<out State>,
        block: suspend CoroutineScope.() -> Unit
    ) {
        if (states.contains(currentState::class)) {
            scope.launch { block.invoke(this) }
        } else logger.error(t = IllegalStateException("The state should be in ${states.toList()}, current is $currentState"))
    }

    private fun changeState(newState: State) {
        val allowedNextStates = when (currentState) {
            State.Idle -> arrayOf(State.ScreenCircling::class)
            State.ScreenCircling -> arrayOf(State.Idle::class, State.ScreenCircled::class)
            State.ScreenCircled -> arrayOf(State.Idle::class, State.ScreenCapturing::class)
            State.ScreenCapturing ->
                arrayOf(
                    State.Idle::class, State.TextRecognizing::class, State.ErrorDisplaying::class
                )
            State.TextRecognizing ->
                arrayOf(
                    State.Idle::class, State.TextTranslating::class, State.ErrorDisplaying::class
                )
            State.TextTranslating ->
                arrayOf(
                    State.ResultDisplaying::class, State.ErrorDisplaying::class, State.Idle::class
                )
            State.ResultDisplaying -> arrayOf(State.Idle::class, State.TextTranslating::class)
            is State.ErrorDisplaying -> arrayOf(State.Idle::class)
        }

        if (allowedNextStates.contains(newState::class)) {
            logger.debug("Change state $currentState > $newState")
            currentStateFlow.value = newState
        } else {
            logger.error("Change state from $currentState to $newState is not allowed")
        }
    }
}

sealed class State {
    override fun toString(): String {
        return this::class.simpleName ?: super.toString()
    }

    object Idle : State()
    object ScreenCircling : State()
    object ScreenCircled : State()
    object ScreenCapturing : State()
    object TextRecognizing : State()
    object TextTranslating : State()
    object ResultDisplaying : State()
    data class ErrorDisplaying(val error: String) : State()
}

sealed class Result(
    open val ocrText: String,
    open val boundingBoxes: List<Rect>,
) {
    data class Translated(
        override val ocrText: String,
        override val boundingBoxes: List<Rect>,
        val translatedText: String,
        val providerType: TranslationProviderType,
    ) : Result(ocrText, boundingBoxes)

    data class SourceLangNotSupport(
        override val ocrText: String,
        override val boundingBoxes: List<Rect>,
        val providerType: TranslationProviderType,
    ) : Result(ocrText, boundingBoxes)

    data class OCROnly(
        override val ocrText: String,
        override val boundingBoxes: List<Rect>,
    ) : Result(ocrText, boundingBoxes)
}
