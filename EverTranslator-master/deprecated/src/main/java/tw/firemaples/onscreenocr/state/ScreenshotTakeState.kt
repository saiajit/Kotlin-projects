package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler
import tw.firemaples.onscreenocr.utils.ImageFile
import java.io.File

object ScreenshotTakeState : BaseState() {
    var manager: StateManager? = null

    override fun stateName(): StateName = StateName.ScreenshotTake

    override fun enter(manager: StateManager) {
        this.manager = manager

        val screenshotHandler = ScreenshotHandler.getInstance()
        if (screenshotHandler.isGetUserPermission) {
            screenshotHandler.setCallback(onScreenshotHandlerCallback)

            screenshotHandler.takeScreenshot(100)
        } else {
            screenshotHandler.getUserPermission()
        }
    }

    private val onScreenshotHandlerCallback = object : ScreenshotHandler.OnScreenshotHandlerCallback {
        override fun onScreenshotStart() {
            FirebaseEvent.logStartCaptureScreen()
            manager?.dispatchBeforeScreenshot()
        }

        override fun onScreenshotFinished(screenshotFile: ImageFile) {
            FirebaseEvent.logCaptureScreenFinished()
            manager?.apply {
                this.screenshotFile = screenshotFile
                dispatchScreenshotSuccess()
                enterState(OCRProcessState)
            }
        }

        override fun onScreenshotFailed(errorCode: Int, e: Throwable?) {
            FirebaseEvent.logCaptureScreenFailed(errorCode, e)
            manager?.dispatchScreenshotFailed(errorCode, e)
        }
    }
}