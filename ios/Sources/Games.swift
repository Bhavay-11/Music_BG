import SwiftUI

// MARK: - Tic-Tac-Toe
struct TicTacToeView: View {
    @State private var b = Array(repeating: "", count: 9)
    @State private var xTurn = true
    private let lines = [[0,1,2],[3,4,5],[6,7,8],[0,3,6],[1,4,7],[2,5,8],[0,4,8],[2,4,6]]
    private let cols = Array(repeating: GridItem(.flexible(), spacing: 8), count: 3)

    private var winner: String? {
        for l in lines { let a = b[l[0]]; if !a.isEmpty && a == b[l[1]] && a == b[l[2]] { return a } }
        return nil
    }
    private var status: String {
        if let w = winner { return "\(w) wins! 🎉" }
        if !b.contains("") { return "Draw" }
        return "\(xTurn ? "X" : "O")'s turn"
    }
    var body: some View {
        GameBackground {
            Text(status).font(.title2.bold()).foregroundColor(winner != nil ? .teal : .white)
            LazyVGrid(columns: cols, spacing: 8) {
                ForEach(0..<9, id: \.self) { i in
                    Button { tap(i) } label: {
                        Text(b[i]).font(.system(size: 40, weight: .bold))
                            .foregroundColor(b[i] == "X" ? .coral : .lav)
                            .frame(maxWidth: .infinity).aspectRatio(1, contentMode: .fit)
                            .background(Color.hi).clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
            }
            PrimaryButton(title: "New game") { b = Array(repeating: "", count: 9); xTurn = true }
        }
    }
    private func tap(_ i: Int) {
        if !b[i].isEmpty || winner != nil { return }
        b[i] = xTurn ? "X" : "O"; xTurn.toggle()
    }
}

// MARK: - Connect Four
struct ConnectFourView: View {
    static let R = 6, C = 7
    @State private var b = Array(repeating: "", count: 42)
    @State private var red = true
    private let cols = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)

    private func winner(_ g: [String]) -> String {
        let dirs = [(0, 1), (1, 0), (1, 1), (1, -1)]
        for r in 0..<Self.R { for c in 0..<Self.C {
            let v = g[r * Self.C + c]; if v.isEmpty { continue }
            for (dr, dc) in dirs {
                var k = 1
                while k < 4 {
                    let nr = r + dr * k, nc = c + dc * k
                    if nr < 0 || nr >= Self.R || nc < 0 || nc >= Self.C || g[nr * Self.C + nc] != v { break }
                    k += 1
                }
                if k == 4 { return v }
            }
        }}
        return ""
    }
    private var status: String {
        let w = winner(b)
        if w == "R" { return "Red wins! 🎉" }
        if w == "Y" { return "Yellow wins! 🎉" }
        if !b.contains("") { return "Draw" }
        return red ? "Red's turn" : "Yellow's turn"
    }
    var body: some View {
        GameBackground {
            Text(status).font(.title2.bold()).foregroundColor(winner(b).isEmpty ? .white : .teal)
            LazyVGrid(columns: cols, spacing: 6) {
                ForEach(0..<42, id: \.self) { i in
                    Circle()
                        .fill(b[i] == "R" ? Color.coral : b[i] == "Y" ? Color.amber : Color.hi)
                        .aspectRatio(1, contentMode: .fit)
                        .onTapGesture { drop(i % Self.C) }
                }
            }
            .padding(8).background(Color.card).clipShape(RoundedRectangle(cornerRadius: 16))
            PrimaryButton(title: "New game") { b = Array(repeating: "", count: 42); red = true }
        }
    }
    private func drop(_ col: Int) {
        if !winner(b).isEmpty { return }
        var r = Self.R - 1
        while r >= 0 {
            if b[r * Self.C + col].isEmpty { b[r * Self.C + col] = red ? "R" : "Y"; red.toggle(); return }
            r -= 1
        }
    }
}

// MARK: - Memory Match
private struct MemCard: Identifiable { let id: Int; let emoji: String; var up = false; var done = false }

struct MemoryView: View {
    private static let emojis = ["🎵", "❤️", "🎮", "🌙", "⭐", "🎧", "🔥", "🍀"]
    @State private var cards: [MemCard] = MemoryView.freshDeck()
    @State private var first: Int? = nil
    @State private var busy = false
    @State private var moves = 0
    private let cols = Array(repeating: GridItem(.flexible(), spacing: 10), count: 4)

    private static func freshDeck() -> [MemCard] {
        (emojis + emojis).shuffled().enumerated().map { MemCard(id: $0.offset, emoji: $0.element) }
    }
    private var won: Bool { cards.allSatisfy { $0.done } }

    var body: some View {
        GameBackground {
            Text(won ? "Solved in \(moves) moves! 🎉" : "Moves: \(moves)")
                .font(.title2.bold()).foregroundColor(won ? .teal : .white)
            LazyVGrid(columns: cols, spacing: 10) {
                ForEach(cards.indices, id: \.self) { i in
                    let c = cards[i]
                    Text(c.up || c.done ? c.emoji : "")
                        .font(.system(size: 26))
                        .frame(maxWidth: .infinity).aspectRatio(1, contentMode: .fit)
                        .background(c.done ? Color.card : Color.hi)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .onTapGesture { tap(i) }
                }
            }
            PrimaryButton(title: "Shuffle & restart") {
                cards = MemoryView.freshDeck(); first = nil; busy = false; moves = 0
            }
        }
    }
    private func tap(_ i: Int) {
        if busy || cards[i].up || cards[i].done { return }
        cards[i].up = true
        if let f = first {
            moves += 1
            if cards[f].emoji == cards[i].emoji {
                cards[f].done = true; cards[i].done = true; first = nil
            } else {
                busy = true
                let a = f, b = i
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) {
                    cards[a].up = false; cards[b].up = false; first = nil; busy = false
                }
            }
        } else { first = i }
    }
}

// MARK: - Reaction Duel
struct ReactionView: View {
    private enum Phase { case idle, wait, go, result, soon }
    @State private var phase: Phase = .idle
    @State private var startAt = Date()
    @State private var best = 0
    @State private var last = 0
    @State private var work: DispatchWorkItem?

    private var color: Color {
        switch phase { case .go: return .teal; case .wait: return .coral; case .soon: return .amber; default: return .card }
    }
    private var headline: String {
        switch phase {
        case .idle: return "Tap to start"
        case .wait: return "Wait…"
        case .go: return "TAP!"
        case .result: return "\(last) ms"
        case .soon: return "Too soon!"
        }
    }
    var body: some View {
        GameBackground {
            Text(best > 0 ? "Best: \(best) ms" : "Beat your best reaction time")
                .font(.headline).foregroundColor(.muted)
            ZStack { color; Text(headline).font(.system(size: 40, weight: .bold)).foregroundColor(.white) }
                .frame(height: 220).clipShape(RoundedRectangle(cornerRadius: 24))
                .onTapGesture { tapped() }
            Text("Pass the phone and see who's quicker.").font(.footnote).foregroundColor(.muted)
        }
        .onDisappear { work?.cancel() }
    }
    private func tapped() {
        switch phase {
        case .idle, .result, .soon:
            phase = .wait
            let w = DispatchWorkItem { startAt = Date(); phase = .go }
            work = w
            DispatchQueue.main.asyncAfter(deadline: .now() + Double.random(in: 1.2...3.6), execute: w)
        case .wait:
            work?.cancel(); phase = .soon
        case .go:
            last = Int(Date().timeIntervalSince(startAt) * 1000)
            if best == 0 || last < best { best = last }
            phase = .result
        case .result:
            break
        }
    }
}

// MARK: - Rock Paper Scissors
struct RPSView: View {
    private let moves = [("🪨", "Rock"), ("📄", "Paper"), ("✂️", "Scissors")]
    @State private var result = "Choose your move"
    @State private var faces = "✊  vs  ✊"
    @State private var w = 0, l = 0, d = 0

    private func beats(_ a: Int, _ b: Int) -> Bool { (a == 0 && b == 2) || (a == 1 && b == 0) || (a == 2 && b == 1) }
    var body: some View {
        GameBackground {
            Text("You \(w) · Bot \(l) · Draws \(d)").font(.headline).foregroundColor(.muted)
            Text(faces).font(.system(size: 44)).padding(.vertical, 10)
            Text(result).font(.title2.bold()).foregroundColor(result.hasPrefix("You win") ? .teal : .white)
            HStack(spacing: 10) {
                ForEach(0..<3, id: \.self) { i in
                    Button { play(i) } label: {
                        VStack { Text(moves[i].0).font(.system(size: 30)); Text(moves[i].1).font(.caption).foregroundColor(.white) }
                            .frame(maxWidth: .infinity).padding(.vertical, 16)
                            .background(Color.hi).clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                }
            }
        }
    }
    private func play(_ i: Int) {
        let bot = Int.random(in: 0..<3)
        faces = "\(moves[i].0)  vs  \(moves[bot].0)"
        if i == bot { d += 1; result = "Draw!" }
        else if beats(i, bot) { w += 1; result = "You win! 🎉" }
        else { l += 1; result = "Bot wins" }
    }
}

// MARK: - This or That
struct ThisOrThatView: View {
    private let prompts = [("Beach holiday", "Mountain escape"), ("Morning person", "Night owl"),
        ("Movie night in", "Night out"), ("Coffee", "Tea"), ("Sweet", "Savoury"), ("Cats", "Dogs"),
        ("Plan it out", "Go with the flow"), ("City lights", "Countryside")]
    @State private var i = 0
    @State private var p1: Int? = nil
    @State private var matches = 0
    @State private var answered = 0

    private var pct: Int { answered == 0 ? 0 : matches * 100 / answered }
    var body: some View {
        GameBackground {
            if i >= prompts.count {
                Text("\(pct)%").font(.system(size: 64, weight: .bold)).foregroundColor(.coral)
                Text("You matched on \(matches) of \(answered)").foregroundColor(.muted)
                PrimaryButton(title: "Play again") { i = 0; p1 = nil; matches = 0; answered = 0 }
            } else {
                Text("Round \(i + 1)/\(prompts.count) · \(pct)% in sync").font(.headline).foregroundColor(.muted)
                Text(p1 == nil ? "Player 1, pick one:" : "Player 2, pick one:").font(.title3.bold()).foregroundColor(.white)
                choice(prompts[i].0, 0)
                Text("or").foregroundColor(.muted)
                choice(prompts[i].1, 1)
            }
        }
    }
    private func choice(_ text: String, _ side: Int) -> some View {
        Button { pick(side) } label: {
            Text(text).font(.title3.bold()).foregroundColor(.white)
                .frame(maxWidth: .infinity, minHeight: 90)
                .background(side == 0 ? Color.lav : Color.coral)
                .clipShape(RoundedRectangle(cornerRadius: 22))
        }
    }
    private func pick(_ side: Int) {
        if let a = p1 { answered += 1; if a == side { matches += 1 }; p1 = nil; i += 1 }
        else { p1 = side }
    }
}
