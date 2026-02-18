import Foundation

struct DeviceConfig: Decodable {
    let apiURL: String
}

class DeviceConfigService {
    private let session: URLSession

    init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 60
        config.waitsForConnectivity = true
        self.session = URLSession(configuration: config)
    }

    func loadConfig(fromBaseURL baseURL: URL, completion: @escaping (Result<DeviceConfig, Error>) -> Void) {
        let configURL = baseURL.appendingPathComponent("config.json")
        var request = URLRequest(url: configURL)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let task = session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            guard let httpResponse = response as? HTTPURLResponse else {
                completion(.failure(NSError(domain: "DeviceConfig", code: -1, userInfo: ["error": "Invalid response"])))
                return
            }
            guard (200...299).contains(httpResponse.statusCode) else {
                completion(.failure(NSError(domain: "DeviceConfig", code: httpResponse.statusCode, userInfo: ["error": "HTTP error"])))
                return
            }
            guard let data = data else {
                completion(.failure(NSError(domain: "DeviceConfig", code: 0, userInfo: ["error": "No data"])))
                return
            }
            do {
                let cfg = try JSONDecoder().decode(DeviceConfig.self, from: data)
                completion(.success(cfg))
            } catch {
                completion(.failure(error))
            }
        }
        task.resume()
    }
}
