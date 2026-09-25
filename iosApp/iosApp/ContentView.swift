import SwiftUI
import UIKit
import UserNotifications
import ComposeApp

/// Hospeda a interface Compose Multiplatform (código Kotlin compartilhado com o Android).
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        return MainViewControllerKt.MainViewController(
            dbPath: dir.appendingPathComponent("financeflow.db").path,
            store: UserDefaultsStore(),
            notifier: IOSNotifier()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// Preferências do app (sessão, ocultar saldo...) no UserDefaults.
final class UserDefaultsStore: NSObject, KeyValueStore {
    private let defaults = UserDefaults.standard

    func getLong(key: String, fallback: Int64) -> Int64 {
        defaults.object(forKey: key) == nil ? fallback : Int64(defaults.integer(forKey: key))
    }

    func putLong(key: String, value: Int64) { defaults.set(Int(value), forKey: key) }

    func getBoolean(key: String, fallback: Bool) -> Bool {
        defaults.object(forKey: key) == nil ? fallback : defaults.bool(forKey: key)
    }

    func putBoolean(key: String, value: Bool) { defaults.set(value, forKey: key) }

    func getInt(key: String, fallback: Int32) -> Int32 {
        defaults.object(forKey: key) == nil ? fallback : Int32(defaults.integer(forKey: key))
    }

    func putInt(key: String, value: Int32) { defaults.set(Int(value), forKey: key) }
}

/// Notificações locais: alertas imediatos e lembretes agendados para as datas de vencimento.
final class IOSNotifier: NSObject, Notifier {
    private let center = UNUserNotificationCenter.current()
    private let prefix = "ff-"

    func post(items: [AppNotification]) {
        for item in items.prefix(5) {
            let content = UNMutableNotificationContent()
            content.title = item.title
            content.body = item.message
            content.sound = .default
            center.add(UNNotificationRequest(identifier: item.key, content: content, trigger: nil))
        }
    }

    func schedule(reminders: [Reminder]) {
        let items = Array(reminders.prefix(60)) // o iOS mantém no máximo 64 pendentes
        center.getPendingNotificationRequests { [center, prefix] requests in
            let old = requests.map { $0.identifier }.filter { $0.hasPrefix(prefix) }
            center.removePendingNotificationRequests(withIdentifiers: old)

            var utc = Calendar(identifier: .gregorian)
            utc.timeZone = TimeZone(identifier: "UTC")!
            for r in items {
                let day = Date(timeIntervalSince1970: TimeInterval(r.epochDay) * 86_400)
                var when = utc.dateComponents([.year, .month, .day], from: day)
                when.hour = Int(r.hour)
                when.minute = 0
                let content = UNMutableNotificationContent()
                content.title = r.title
                content.body = r.body
                content.sound = .default
                let trigger = UNCalendarNotificationTrigger(dateMatching: when, repeats: false)
                center.add(UNNotificationRequest(identifier: prefix + r.id, content: content, trigger: trigger))
            }
        }
    }
}
