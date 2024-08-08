package tw.firemaples.onscreenocr.translate

import io.github.firemaples.language.Language
import io.github.firemaples.translate.Translate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil
import tw.firemaples.onscreenocr.utils.*

object MicrosoftApiTranslator : Translator {
    val logger: Logger = LoggerFactory.getLogger(MicrosoftApiTranslator::class.java)
    override fun translate(text: String, lang: String,
                           callback: (Boolean, String, Throwable?) -> Unit
    ) = GlobalScope.launch(threadTranslation) {
        logger.info("Microsoft key group: ${RemoteConfigUtil.microsoftTranslationKeyGroupId}")
        Translate.setSubscriptionKey(RemoteConfigUtil.microsoftTranslationKey)
        Translate.setUsingSSL(true)
        try {
            val result = Translate.execute(text, Language.fromString(lang))
            threadUI {
                callback(true, result, null)
            }
        } catch (e: Throwable) {
            logger.error("Translation failed", e)
            threadUI {
                val error = if (e.message?.contains("\"code\":403001") == true) {
                    TranslationErrorException(R.string.error_translation_service_exceeded_limit.asFormatString(R.string.service_microsoft_azure.asString()), e, ErrorType.ExceededDataLimit)
                } else {
                    TranslationErrorException(e.message, e)
                }
                callback(false, "", error)
            }
        }
    }
}