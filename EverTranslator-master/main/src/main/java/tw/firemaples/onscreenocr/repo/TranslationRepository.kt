package tw.firemaples.onscreenocr.repo

import android.content.Context
import android.os.Build
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.translator.TranslationLanguage
import tw.firemaples.onscreenocr.translator.TranslationProvider
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.utils.Utils

class TranslationRepository {
    private val context: Context by lazy { Utils.context }

    val selectedProviderTypeFlow: Flow<TranslationProviderType>
        get() = AppPref.asLiveData(AppPref::selectedTranslationProvider).asFlow()
            .map { TranslationProviderType.fromKey(it) }
            .flowOn(Dispatchers.Default)
    val selectedTranslationLangFlow: Flow<String>
        get() = AppPref.asLiveData(AppPref::selectedTranslationLang).asFlow()
            .flowOn(Dispatchers.Default)

    fun getAllProviders(): Flow<List<TranslationProvider>> = flow {
        val selected = AppPref.selectedTranslationProvider

        val result = TranslationProviderType.values()
            .filter { it.minSDKVersion <= Build.VERSION.SDK_INT }
            .sortedBy { it.index }
            .map {
                TranslationProvider.fromType(
                    context = context,
                    type = it,
                    selected = it.key == selected
                )
            }

        emit(result)
    }.flowOn(Dispatchers.Default)

    fun getSelectedProvider(): Flow<TranslationProvider> = flow {
        val type = TranslationProviderType.fromKey(AppPref.selectedTranslationProvider)

        emit(TranslationProvider.fromType(context, type, true))
    }.flowOn(Dispatchers.Default)

    fun setSelectedProvider(key: String): Flow<TranslationProvider> = flow {
        val type = TranslationProviderType.fromKey(key)
        AppPref.selectedTranslationProvider = type.key

        emit(TranslationProvider.fromType(context, type, true))
    }.flowOn(Dispatchers.Default)

    fun getTranslationLanguageList(providerKey: String): Flow<List<TranslationLanguage>> = flow {
        val type = TranslationProviderType.fromKey(providerKey)
        emit(Translator.getTranslator(type).supportedLanguages())
    }.flowOn(Dispatchers.Default)

    suspend fun setSelectedTranslationLang(langCode: String) {
        withContext(Dispatchers.Default) {
            AppPref.selectedTranslationLang = langCode
        }
    }

    fun getTranslationHint(providerKey: String): Flow<String?> = flow {
        val type = TranslationProviderType.fromKey(providerKey)
        emit(Translator.getTranslator(type).translationHint)
    }.flowOn(Dispatchers.Default)
}


