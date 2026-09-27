import SwiftUI

// MARK: - Palette (matches the Android app's dark glass theme)
extension Color {
    static let bg = Color(red: 0.06, green: 0.05, blue: 0.09)
    static let card = Color(red: 0.15, green: 0.13, blue: 0.25)
    static let hi = Color(red: 0.19, green: 0.16, blue: 0.30)
    static let coral = Color(red: 0.90, green: 0.28, blue: 0.30)
    static let teal = Color(red: 0.18, green: 0.75, blue: 0.44)
    static let lav = Color(red: 0.69, green: 0.42, blue: 1.0)
    static let amber = Color(red: 0.95, green: 0.76, blue: 0.31)
    static let muted = Color(red: 0.66, green: 0.62, blue: 0.75)
}

@main
struct MusicBGApp: App {
    var body: some Scene {
        WindowGroup { HubView().preferredColorScheme(.dark) }
    }
}

struct GameItem: Identifiable {
    let id = UUID()
    let name: String
    let tag: String
    let emoji: String
    let color: Color
    let dest: () -> AnyView
}

struct HubView: View {
    private let games: [GameItem] = [
        GameItem(name: "Tic-Tac-Toe", tag: "Two players, one screen", emoji: "#️⃣", color: .teal) { AnyView(TicTacToeView()) },
        GameItem(name: "Connect Four", tag: "Drop discs, line up four", emoji: "🔴", color: .coral) { AnyView(ConnectFourView()) },
        GameItem(name: "Memory Match", tag: "Flip cards, find pairs", emoji: "🧠", color: .lav) { AnyView(MemoryView()) },
        GameItem(name: "Reaction Duel", tag: "Tap on green", emoji: "⚡", color: .coral) { AnyView(ReactionView()) },
        GameItem(name: "Rock Paper Scissors", tag: "Duel a bot", emoji: "✊", color: .teal) { AnyView(RPSView()) },
        GameItem(name: "This or That", tag: "How alike are you?", emoji: "❤️", color: .lav) { AnyView(ThisOrThatView()) },
    ]
    private let cols = [GridItem(.flexible(), spacing: 14), GridItem(.flexible(), spacing: 14)]

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVGrid(columns: cols, spacing: 14) {
                    ForEach(games) { g in
                        NavigationLink { g.dest().navigationTitle(g.name) } label: { Tile(game: g) }
                    }
                }
                .padding(16)
                Text("Music_BG · native iOS build")
                    .font(.footnote).foregroundColor(.muted).padding(.bottom, 24)
            }
            .background(Color.bg.ignoresSafeArea())
            .navigationTitle("Music_BG")
        }
        .tint(.coral)
    }
}

struct Tile: View {
    let game: GameItem
    var body: some View {
        VStack(alignment: .leading) {
            Text(game.emoji).font(.system(size: 26))
                .frame(width: 52, height: 52)
                .background(game.color).clipShape(RoundedRectangle(cornerRadius: 16))
            Spacer(minLength: 8)
            Text(game.name).font(.headline).foregroundColor(.white)
            Text(game.tag).font(.caption).foregroundColor(.muted).lineLimit(2)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .aspectRatio(0.95, contentMode: .fit)
        .padding(16)
        .background(LinearGradient(colors: [.card, .bg], startPoint: .topLeading, endPoint: .bottomTrailing))
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }
}

// Shared "New game" button style.
struct PrimaryButton: View {
    let title: String
    let action: () -> Void
    var color: Color = .coral
    var body: some View {
        Button(action: action) {
            Text(title).font(.headline).foregroundColor(.white)
                .frame(maxWidth: .infinity).padding(.vertical, 14)
                .background(color).clipShape(RoundedRectangle(cornerRadius: 14))
        }
    }
}

// Wrap any game screen in the app background.
struct GameBackground<Content: View>: View {
    @ViewBuilder let content: Content
    var body: some View {
        ZStack { Color.bg.ignoresSafeArea(); ScrollView { VStack(spacing: 14) { content }.padding(16) } }
    }
}
