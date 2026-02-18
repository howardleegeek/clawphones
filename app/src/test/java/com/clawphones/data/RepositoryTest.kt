package com.clawphones.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

// In-memory DAO for tests
class InMemoryCacheDao : CacheDao {
  val storage = mutableMapOf<String, String>()
  override suspend fun getValue(key: String): String? = storage[key]
  override suspend fun insert(cache: DataCacheEntity) {
    storage[cache.key] = cache.value
  }
}

class CountingNetworkService : NetworkService {
  var calls = 0
  override suspend fun fetchData(): String {
    calls += 1
    return "net-data"
  }
}

class RepositoryTest {
  @Test
  fun readsFromCacheWhenPresent() = runBlocking {
    val dao = InMemoryCacheDao()
    dao.insert(DataCacheEntity("dataKey", "cached"))
    val net = CountingNetworkService()
    val repo = Repository(dao, net)

    val result = repo.getData()
    assertEquals("cached", result)
    // network should not be called
    assertEquals(0, net.calls)
  }

  @Test
  fun fetchesFromNetworkAndCaches() = runBlocking {
    val dao = InMemoryCacheDao()
    val net = CountingNetworkService()
    val repo = Repository(dao, net)

    val result = repo.getData()
    assertEquals("net-data", result)
    // network should be called exactly once
    assertEquals(1, net.calls)
    // data should be cached locally
    assertEquals("net-data", dao.storage["dataKey"])
  }
}
