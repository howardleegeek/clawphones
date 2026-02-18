package com.clawphones.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RepositoryTest {

  // Simple fake DAO to test Repository behavior without Android framework
  class FakeCacheDao(private val initial: MutableMap<String, String> = mutableMapOf()) : CacheDao {
    override suspend fun getValue(key: String): String? {
      return initial[key]
    }

    override suspend fun insert(entry: DataCacheEntity) {
      initial[entry.key] = entry.value
    }

    // helper for test assertions
    fun getValueSync(key: String): String? = initial[key]
  }

  class FakeNetworkService(private val value: String = "networkValue") : NetworkService {
    var called = false
        private set
    override suspend fun fetchData(): String {
      called = true
      return value
    }
  }

  @Test
  fun readsFromCacheNoNetwork() = runBlocking {
    val dao = FakeCacheDao(mutableMapOf("dataKey" to "cachedValue"))
    val net = FakeNetworkService("shouldNotCall")
    val repo = Repository(dao, net)

    val result = repo.getData()
    assertEquals("cachedValue", result)
    assertFalse(net.called)
  }

  @Test
  fun fetchesFromNetworkAndCaches() = runBlocking {
    val dao = FakeCacheDao(mutableMapOf())
    val net = FakeNetworkService("networkValue")
    val repo = Repository(dao, net)

    val result = repo.getData()
    assertEquals("networkValue", result)
    // verify that value is cached
    assertEquals("networkValue", (dao as FakeCacheDao).getValueSync("dataKey"))
  }
}
