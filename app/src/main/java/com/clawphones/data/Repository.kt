package com.clawphones.data

interface NetworkService {
  suspend fun fetchData(): String
}

class Repository(private val dao: CacheDao, private val network: NetworkService) {

  suspend fun getData(): String {
    val cached = dao.getValue("dataKey")
    if (cached != null) return cached

    val networkValue = network.fetchData()
    dao.insert(DataCacheEntity("dataKey", networkValue))
    return networkValue
  }
}
