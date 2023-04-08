package dev.reprator.dao

import dev.reprator.modals.LanguageModal
import org.ehcache.config.builders.*
import org.ehcache.config.units.*
import org.ehcache.impl.config.persistence.*
import java.io.*

class DAOLanguageFacadeCacheImpl(
    private val delegate: DAOLanguageFacade,
    storagePath: File
) : DAOLanguageFacade {

    companion object {
        private const val CACHE_LANGUAGE = "cacheLanguage"
    }

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerPersistenceConfiguration(storagePath))
        .withCache(
            CACHE_LANGUAGE,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Int::class.javaObjectType,
                LanguageModal::class.java,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(100, MemoryUnit.MB, true)
            )
        )
        .build(true)

    private val cacheLanguage = cacheManager.getCache(CACHE_LANGUAGE, Int::class.javaObjectType, LanguageModal::class.java)

    override suspend fun allLanguage(): List<LanguageModal> =
        delegate.allLanguage()

    override suspend fun language(id: Int): LanguageModal? =
        cacheLanguage[id]
            ?: delegate.language(id)
                .also { article -> cacheLanguage.put(id, article) }

    override suspend fun addNewLanguage(name: String): LanguageModal? =
        delegate.addNewLanguage(name)
            ?.also { article -> cacheLanguage.put(article.id, article) }

    override suspend fun editLanguage(id: Int, name: String): Boolean {
        cacheLanguage.put(id, LanguageModal(id, name))
        return delegate.editLanguage(id, name)
    }

    override suspend fun deleteLanguage(id: Int): Boolean {
        cacheLanguage.remove(id)
        return delegate.deleteLanguage(id)
    }

}