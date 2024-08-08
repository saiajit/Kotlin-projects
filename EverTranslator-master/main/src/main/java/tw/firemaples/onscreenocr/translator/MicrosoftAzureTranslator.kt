package tw.firemaples.onscreenocr.translator

import io.github.firemaples.language.Language
import io.github.firemaples.translate.Translate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.firstPart

object MicrosoftAzureTranslator : Translator {
    private val errorRegex = "\"code\":(\\d+)".toRegex()
    private const val errorOutOfQuota = 403001

    override val type: TranslationProviderType
        get() = TranslationProviderType.MicrosoftAzure

    override suspend fun supportedLanguages(): List<TranslationLanguage> {
        val langCodeList =
            context.resources.getStringArray(R.array.microsoft_translationLangCode_iso6391)
        val langNameList = context.resources.getStringArray(R.array.microsoft_translationLangName)

        val selectedLangCode = selectedLangCode(langCodeList)

        return (langCodeList.indices).map { i ->
            val code = langCodeList[i]
            val name = langNameList[i]

            TranslationLanguage(
                code = code,
                displayName = name,
                selected = code == selectedLangCode
            )
        }
    }

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult {
        if (!isLangSupport()) {
            return TranslationResult.SourceLangNotSupport(type)
        }

        if (text.isBlank()) {
            return TranslationResult.TranslatedResult(result = "", type)
        }

        val targetLangCode = supportedLanguages().firstOrNull { it.selected }?.code
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("The selected translation language is not found"))

        if (AppPref.selectedOCRLang.firstPart() == targetLangCode) {
            return TranslationResult.TranslatedResult(result = text, type)
        }

        val targetLang = targetLangCode.let { Language.fromString(it) }

        Translate.setSubscriptionKey(RemoteConfigManager.microsoftTranslationKey)

        return try {
            val result = withContext(Dispatchers.IO) {
                Translate.execute(text, targetLang)
            }

            TranslationResult.TranslatedResult(result, type)
        } catch (e: Exception) {
            var error: Throwable? = null
            val message = e.message
            if (!message.isNullOrBlank()) {
                error = when (val code =
                    errorRegex.find(message)?.groupValues?.getOrNull(1)?.toIntOrNull()) {
                    errorOutOfQuota ->
                        Error(
                            message = context.getString(
                                R.string.error_microsoft_translation_out_of_quota,
                                code.toString(),
                                RemoteConfigManager.microsoftTranslationKeyGroupId
                            ),
                            type = ErrorType.OutOfQuota,
                            cause = e,
                        )
                    else -> null
                }
            }
            TranslationResult.TranslationFailed(error ?: e)
        }
    }

    enum class ErrorType {
        OutOfQuota,
    }

    data class Error(
        override val message: String?,
        val type: ErrorType,
        override val cause: Throwable?
    ) : Exception(message, cause)
}
