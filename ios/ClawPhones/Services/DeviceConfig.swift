import Foundation

// Simple device config loader over HTTPS. Keeps networking code small and testable.
struct DeviceConfig: Decodable {
    let apiURL: String
}

class DeviceConfigService {
    func loadConfig(fromBaseURL baseURL: URL, completion: @escaping (Result<DeviceConfig, Error>) -> Void) {
        let configURL = baseURL.appendingPathComponent("config.json")
        let task = URLSession.shared.dataTask(with: configURL) { data, _, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            guard let data = data else {
                completion(.failure(NSError(domain: "DeviceConfig", code: 0, userInfo: ["error":"No data"])))
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
